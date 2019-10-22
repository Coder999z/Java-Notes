
* [Enumeration](#enumeration)
  * [接口源码](#%E6%8E%A5%E5%8F%A3%E6%BA%90%E7%A0%81)
* [iterator](#iterator)
  * [接口源码](#%E6%8E%A5%E5%8F%A3%E6%BA%90%E7%A0%81-1)
* [fail\-fast](#fail-fast)
  * [概述](#%E6%A6%82%E8%BF%B0)
  * [示例](#%E7%A4%BA%E4%BE%8B)
  * [原理](#%E5%8E%9F%E7%90%86)

# Enumeration
## 接口源码
> 根据以下源码和注释可以知道，此接口和iterator功能是相同的，并且在源码注解中也是推荐使用iterator代替它使用，Enumeration提供的两个函数只有读的操作，只有在Vector和Hashtale有它们的实现。
>
> ```java
> /**
>  * 推荐使用iterator
>  * NOTE: The functionality of this interface is duplicated by the Iterator
>  * interface.  In addition, Iterator adds an optional remove operation, and
>  * has shorter method names.  New implementations should consider using
>  * Iterator in preference to Enumeration.
>  *
>  * @see     java.util.Iterator
>  * @see     java.io.SequenceInputStream
>  * @see     java.util.Enumeration#nextElement()
>  * @see     java.util.Hashtable
>  * @see     java.util.Hashtable#elements()
>  * @see     java.util.Hashtable#keys()
>  * @see     java.util.Vector
>  * @see     java.util.Vector#elements()
>  *
>  * @author  Lee Boynton
>  * @since   JDK1.0
>  */
> public interface Enumeration<E> {
> 	
>     boolean hasMoreElements();
>     
>     E nextElement();
> }
> ```

# iterator
## 接口源码
> 从以下源码中可以知道，iterator代替了enumeration。提供了对元素的读和修改方法。
>
> ```java
> /**
>  * An iterator over a collection.  {@code Iterator} takes the place of
>  * {@link Enumeration} in the Java Collections Framework.  Iterators
>  * differ from enumerations in two ways:
>  * @param <E> the type of elements returned by this iterator
>  * @see Collection
>  * @see ListIterator
>  * @see Iterable
>  * @since 1.2
>  */
> public interface Iterator<E> {
>     
>     boolean hasNext();
>     
>     E next();
> 
>     default void remove() {
>         throw new UnsupportedOperationException("remove");
>     }
> 
>     default void forEachRemaining(Consumer<? super E> action) {
>         Objects.requireNonNull(action);
>         while (hasNext())
>             action.accept(next());
>     }
> }
> ```

# fail-fast
## 概述
> fail-fast是Java集合中的一种错误机制，当多线程对同一个集合进行操作时，就有可能出现fail-fast事件。但是它只能被用来检测错误，JDK并不保证fail-fast机制一定会发生

## 示例
> 线程T1使用Iterator进行遍历ArrayList时，线程T2对该ArrayList进行了插入或者删除操作，代码中抛出了java.util.ConcurrentModificationException。即产生了fail-fast事件。

## 原理

> Iterator在不同类中的实现不同，这里拿ArrayList进行示例。ArrayList的Iterator时在父类AbstractList中实现的
>
> ```java
> public E next() {
> 			//这个函数实现了fail-fast机制
>             checkForComodification();
>             try {
>                 int i = cursor;
>                 E next = get(i);
>                 lastRet = i;
>                 cursor = i + 1;
>                 return next;
>             } catch (IndexOutOfBoundsException e) {
>                 checkForComodification();
>                 throw new NoSuchElementException();
>             }
>  }
> 
> public void remove() {
>             if (lastRet < 0)
>                 throw new IllegalStateException();
>              //这个函数实现了fail-fast机制
>             checkForComodification();
> 
>             try {
>                 AbstractList.this.remove(lastRet);
>                 if (lastRet < cursor)
>                     cursor--;
>                 lastRet = -1;
>                 expectedModCount = modCount;
>             } catch (IndexOutOfBoundsException e) {
>                 throw new ConcurrentModificationException();
>             }
>         }        
> 
> final void checkForComodification() {
> 			//每次获取下一个元素之前都会判断，当前迭代器保存的expectedModCount和list中的modCount是否相等，不相等就抛出异常
>             if (modCount != expectedModCount)
>                 throw new ConcurrentModificationException();
>         }
> ```
>
> 回顾一下之前分析的ArrayList源码，只要是涉及到集合中元素个数的函数（add，remove，clear等）都会改变modCount的值。
> 那么结合上一个示例完整分析一遍fail-fast的出现流程：
>
> 1. 新建一个ArrayList名称为list，并向list中添加了若干数据。
> 2. 线程T1启动，创建了一个Iterator对象，Iterator中的属性expectedModCount = modCount
> 3. 线程T1调用Iterator中的next()函数遍历list，还未遍历结束，cpu执行时间结束，暂时被挂起
> 4. 线程T2开始运行，向list中添加了一个元素，触发了modCount++，线程T2运行结束
> 5. 线程T1继续运行，继续遍历list中的元素，此时执行了checkForComodification()函数，将Iterator中的expectedModCount 属性和list中的modCount进行对比，发现不相同，则抛出了ConcurrentModificationException，完成了fail-fast机制。