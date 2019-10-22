
* [WeakHashMap](#weakhashmap)
  * [概述](#%E6%A6%82%E8%BF%B0)
  * [简单科普JVM中的引用](#%E7%AE%80%E5%8D%95%E7%A7%91%E6%99%AEjvm%E4%B8%AD%E7%9A%84%E5%BC%95%E7%94%A8)
    * [强引用](#%E5%BC%BA%E5%BC%95%E7%94%A8)
    * [软引用](#%E8%BD%AF%E5%BC%95%E7%94%A8)
    * [弱引用](#%E5%BC%B1%E5%BC%95%E7%94%A8)
    * [虚引用](#%E8%99%9A%E5%BC%95%E7%94%A8)
  * [WeakHashMap的数据结构](#weakhashmap%E7%9A%84%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84)
  * [源码分析](#%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90)
    * [属性](#%E5%B1%9E%E6%80%A7)
    * [clear()](#clear)
    * [expungeStaleEntries()](#expungestaleentries)
    * [hash()](#hash)
    * [resize()](#resize)
  * [总结](#%E6%80%BB%E7%BB%93)

# WeakHashMap
## 概述
>  WeakHashMap和HashMap一样是一个散列表，并且键值都可以为null（key只能有一个null），它也同样有扩容机制，数据结构也基本相同，最大的区别在于WeakHashMap使用的是**弱键**，当一个键在不再被引用时将会在下次GC时被回收，并且WeakHashMap也会将其对应节点中移除。对了，它是线程不安全的

## 简单科普JVM中的引用
> 在JDK 1.2之后，Java对引用的概念进行了扩充，将引用分为强引用（StrongReference）、 软引用（Soft Reference）、 弱引用（Weak Reference）、 虚引用（PhantomReference）4种，这4种引用强度依次逐渐减弱。
### 强引用

> ```java
> //以下的这行代码就是一个强引用，obj指向堆内存中hashCode为22927a81的对象
> Object obj = new Object(); //假设该对象的hashCode为22927a81
> //obj2指针指向obj指向的对象22927a81
> Object obj2 = obj;
> //此时将obj设置为null，并手动GC，打印的obj2仍然是22927a81，说明此对象没有被GC。
> obj = null;
> System.gc();
> System.out.println(obj2); //22927a81
> ```
>
> 强引用只要引用还在，JVM宁可抛出OOM（OutOfMemory）异常也不会将它回收。

### 软引用
> 软引用使用的是java.lang.ref.SoftReference类实现。

> 用来描述一些还有用但并非必须的对象。对于软引用关联着的对象，在系统将要发生内存溢出异常之前，将会把这些对象列进回收范围之中进行第二次回收。如果，这次回收没有足够的内存，才会抛出内存溢出异常，虚拟机在抛出 OutOfMemoryError 之前会保证所有的软引用（持有的对象）已被清除，此外，没有任何约束保证软引用（持有的对象）将在某个特定的时间点被清除，或者确定一组不同的软引用（持有的对象）被清除的顺序。不过，虚拟机的具体实现会倾向于不清除最近创建或最近使用过的软引用。在JDK1.2之后，提供了SoftReference类来实现软引用。软引用非常适合用来做高速缓存，当系统内存不足的时候，缓存中的内容是可以被释放的。

> 提供了两种构造实现。
>
> ```java
> //referent 需要设置成软引用的对象
> public SoftReference(T referent) {
>         super(referent);
>         this.timestamp = clock;
> }
> // ReferenceQueue 在软引用被GC后会将SoftReference对象放入此队列中
> public SoftReference(T referent, ReferenceQueue<? super T> q) {
>         super(referent, q);
>         this.timestamp = clock;
> }
> ```

### 弱引用
> 弱引用也是用来描述非必须对象的，它的强度比软引用更低，被设置成弱引用的对象在失去引用后，下一次GC就会将其回收。
>
> 它也提供了两个构造方法，和弱引用相同。
>
> 举个例子
>
> ```java
>     ReferenceQueue queue = new ReferenceQueue<>();
>     String str = new String("referent");
>     WeakReference reference = new WeakReference(str,queue);
>     System.gc();
>     System.out.println(reference.get()); 	//referent
>     System.out.println(queue.poll());		//null
>     str = null;
>     System.gc();
>     System.out.println(reference.get());	//null
>     System.out.println(queue.poll());		//java.lang.ref.WeakReference@22927a81	
> ```

### 虚引用
> 也称为幽灵引用或者幻影引用，它是最弱的一种引用关系。一个对象是否有虚引用的存在，完全不会对其生命周期构成影响，也无法通过虚引用来取得一个对象实例。为一个对象设置虚引用关联的唯一目的就是能在这个对象被收集器回收时收到一个系统通知。在JDK1.2之后，提供了PhantomReference类来实现虚引用。虚引用与软引用和弱引用的一个区别在于：虚引用必须和引用队列（ReferenceQueue）联合使用。当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会在回收对象的内存之前，把这个虚引用加入到与之关联的引用队列中。程序可以通过判断引用队列中是否已经加入了虚引用，来了解。

> 从一下源码中可以得知，虚引用无法通过虚引用来取得对象实例。它的构造方法只有一个，必须配合队列使用。
>
> ```java
> public T get() {
>         return null;
>     }
> 
>     public PhantomReference(T referent, ReferenceQueue<? super T> q) {
>         super(referent, q);
>     }
> ```

## WeakHashMap的数据结构
> 它的数据结构和HashMap在1.8版本以前相同，是数组+链表的结构
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190416192312606.png)

## 源码分析
> 由于大部分和HashMap相同，在此只介绍不同处。

### 属性

> ```java
> 	private static final int DEFAULT_INITIAL_CAPACITY = 16;
>     private static final int MAXIMUM_CAPACITY = 1 << 30;
>     private static final float DEFAULT_LOAD_FACTOR = 0.75f;
>     Entry<K,V>[] table;
>     private int size;
>     private int threshold;
>     private final float loadFactor;
> 	//上面的属性已经很熟悉了不再赘述
> 	//queue是用来存储被GC的WeakReference对象
>     private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
>     //WeakHashMap也实现了fail-fast机制
>     int modCount;
> ```

### clear()
> 实现思路和其他Map相同，都是将table数组逐个索引设置为null，这里多了一项操作是将queue出队列操作。
>
> ```java
> public void clear() {
>         // clear out ref queue. We don't need to expunge entries
>         // since table is getting cleared.
>         while (queue.poll() != null)
>             ;
> 
>         modCount++;
>         Arrays.fill(table, null);
>         size = 0;
> 
>         // Allocation of array may have caused GC, which may have caused
>         // additional entries to go stale.  Removing these entries from the
>         // reference queue will make them eligible for reclamation.
>         while (queue.poll() != null)
>             ;
>     }
> ```

### expungeStaleEntries()
> 故名思意，删除过期的条目，此方法在多处被调用，如：对Map的所有查询、删除操作，获得Map中节点数量，扩容等。
>
> ```java
>     private void expungeStaleEntries() {
>     	//循环将queue中的元素出队列。被GC的WeakReference会被存在队列中
>         for (Object x; (x = queue.poll()) != null; ) {
>             synchronized (queue) {
>                 @SuppressWarnings("unchecked")
>                 	//将WeakReference向下转型成Entry对象。Entry是WeakHashMap内部类继承了WeakReference
>                     Entry<K,V> e = (Entry<K,V>) x;
>                 //计算对应下标
>                 int i = indexFor(e.hash, table.length);
> 				//遍历链表删除已经被GC的key对应的Entry对象。
>                 Entry<K,V> prev = table[i];
>                 Entry<K,V> p = prev;
>                 while (p != null) {
>                     Entry<K,V> next = p.next;
>                     if (p == e) {
>                         if (prev == e)
>                             table[i] = next;
>                         else
>                             prev.next = next;
>                         // Must not null out e.next;
>                         // stale entries may be in use by a HashIterator
>                         e.value = null; // Help GC
>                         size--;
>                         break;
>                     }
>                     prev = p;
>                     p = next;
>                 }
>             }
>         }
>     }
> 
> private static class Entry<K,V> extends WeakReference<Object> implements Map.Entry<K,V> { ..........  }
> ```

### hash()
> 十分眼熟，其实和jdk1.8之前HashMap的hash()一样的
>
> ```java
>     final int hash(Object k) {
>         int h = k.hashCode();
>         h ^= (h >>> 20) ^ (h >>> 12);
>         return h ^ (h >>> 7) ^ (h >>> 4);
>     }
> ```

### resize()
> 重点解释一下代码中添加分割线①处。
> 根据英文的注解可以大概知道，分割线以下是对扩容后有没有浪费空间的判断，主要是为了防止在扩容时清理GC的key导致的map的size很小，但是table的length很大的浪费空间情况。
> 这种情况是如何出现的呢？
>
> 1. 例如在当前长度（size）12，阈值（threshold）12的情况下，此时调用了put方法成功插入了一个Entry，此时的size变为13触发resize
> 2. 执行到②调用getTable()，在getTable()中调用了expungeStaleEntries()，对GC的键对应的Entry进行了清理。
> 3. 假设清理完的size仍然为13，程序继续执行扩容复制操作知道①处的if判断为true进行计算新的阈值，完成扩容，
> 4. 假设清理完的size为5，程序继续执行扩容复制操作知道①处的if判断显然小于原阈值12，那么执行else中的代码再次清理GC的Entry，并将Entry由扩容后的newTab复制给原table中。这么做的目的是为了防止空间的浪费，如果按照假设的情况，table扩容为32而其中只使用了5
>
> ```java
> 	//在调用时传入新长度，原先的两倍
>     void resize(int newCapacity) {
>     	//原数组
>     	//②
>         Entry<K,V>[] oldTable = getTable();
>         //原长度
>         int oldCapacity = oldTable.length;
>         if (oldCapacity == MAXIMUM_CAPACITY) {
>             threshold = Integer.MAX_VALUE;
>             return;
>         }
> 
>         Entry<K,V>[] newTable = newTable(newCapacity);
>         //将原数组中的Entry复制到新的数组中
>         transfer(oldTable, newTable);
>         table = newTable;
> 
>         /*
>          * If ignoring null elements and processing ref queue caused massive
>          * shrinkage, then restore old table.  This should be rare, but avoids
>          * unbounded expansion of garbage-filled tables.
>          */
>          //--------------------------------------------------------------------①
>         if (size >= threshold / 2) {
>             threshold = (int)(newCapacity * loadFactor);
>         } else {
>             expungeStaleEntries();
>             transfer(newTable, oldTable);
>             table = oldTable;
>         }
>     }
> 
> 	//将原数组中的Entry复制到新的数组中，比较简单就不注释了
>     private void transfer(Entry<K,V>[] src, Entry<K,V>[] dest) {
>         for (int j = 0; j < src.length; ++j) {
>             Entry<K,V> e = src[j];
>             src[j] = null;
>             while (e != null) {
>                 Entry<K,V> next = e.next;
>                 Object key = e.get();
>                 if (key == null) {
>                     e.next = null;  // Help GC
>                     e.value = null; //  "   "
>                     size--;
>                 } else {
>                     int i = indexFor(e.hash, dest.length);
>                     e.next = dest[i];
>                     dest[i] = e;
>                 }
>                 e = next;
>             }
>         }
>     }
> 
> ```


## 总结
> WeakHashMap最大的特色在于key使用了弱引用，能够随着GC清理map中的entry，根据它的特性可以选择用在需要缓存的场景。