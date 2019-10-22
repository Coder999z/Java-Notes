
* [List接口](#list%E6%8E%A5%E5%8F%A3)
* [ArrayList](#arraylist)
  * [底层实现（1\.8）](#%E5%BA%95%E5%B1%82%E5%AE%9E%E7%8E%B018)
    * [核心函数分析](#%E6%A0%B8%E5%BF%83%E5%87%BD%E6%95%B0%E5%88%86%E6%9E%90)
  * [迭代器](#%E8%BF%AD%E4%BB%A3%E5%99%A8)
  * [特征](#%E7%89%B9%E5%BE%81)
* [Vector](#vector)
  * [简介](#%E7%AE%80%E4%BB%8B)
  * [底层实现](#%E5%BA%95%E5%B1%82%E5%AE%9E%E7%8E%B0)
* [总结ArrayList和Vector的区别](#%E6%80%BB%E7%BB%93arraylist%E5%92%8Cvector%E7%9A%84%E5%8C%BA%E5%88%AB)

### List接口

> - List是有序的Collection，此接口可以精准的控制每个元素的插入位置。用户能够使用索引来访问List中的元素，类似于数组，每个元素都有自己的索引，第一个元素的索引为0。
> - 实现该接口的常用类有LinkedList，ArrayLIst，Vector，Stack等。

### ArrayList
简介

> -  ArrayList是一个数组队列，实现了可变大小的数组，它存储Object，可以存储null，它是线程不安全的。
> - 实现RandomAccess接口，提供随机访问功能，可以通过元素的下标索引来快速获取元素对象。
> - 实现了Serializable接口，支持序列化。
> - 实现Cloneable接口，实现了clone()
> - 它是线程不安全的

#### 底层实现（1.8）

> **三种构造方式**
>
> - 指定长度构造：指定的长度不得小于0否则抛异常，大于0则初始化Object[]，等于0的情况和空参构造一样
>
> ```java
>     public ArrayList(int initialCapacity) {
>         if (initialCapacity > 0) {
>             this.elementData = new Object[initialCapacity];
>         } else if (initialCapacity == 0) {
>             this.elementData = EMPTY_ELEMENTDATA;
>         } else {
>             throw new IllegalArgumentException("Illegal Capacity: "+
>                                                initialCapacity);
>         }
>     }
> ```
>
> - 空参构造：将存储数据的Object[]初始化为一个空的数组（不为Null只是数组中无数据没有指定长度），在首次add时进行初始化。默认长度为10，在jdk1.7以前无参构造就是直接初始化长度为10的Obj[]。
>
> ```java
>     public ArrayList() {
>         this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
>     }
> ```
>
> - 参数为集合的构造函数，将集合转换成对象数组，并赋值给ArrayList中存储数据的Object[]，如果集合大小为空则将Object[]设置为空数组，在首次添加时初始化。
>
> ```java
>     public ArrayList(Collection<? extends E> c) {
>         elementData = c.toArray();
>         if ((size = elementData.length) != 0) {
>             // c.toArray might (incorrectly) not return Object[] (see 6260652)
>             if (elementData.getClass() != Object[].class)
>                 elementData = Arrays.copyOf(elementData, size, Object[].class);
>         } else {
>             // replace with empty array.
>             this.elementData = EMPTY_ELEMENTDATA;
>         }
>     }
> ```

##### 核心函数分析
> - add()
>
> ```java
> public boolean add(E e) {
>     //确保数组有合适的大小
>     ensureCapacityInternal(size + 1);
>     elementData[size++] = e;
>     return true; }
> 
> public void add(int index, E element) {
> 		//检查插入位置索引是否合法
>         rangeCheckForAdd(index);
> 		
>         ensureCapacityInternal(size + 1);  // Increments modCount!!
>         //将elementData中index插入位置以后的所有节点的下标+1
>         System.arraycopy(elementData, index, elementData, index + 1,
>                          size - index);
>         elementData[index] = element;
>         size++;
>     }
> ```
>
> ```java
> private void ensureCapacityInternal(int minCapacity) {
>     // 判断数组是否初始化长度
>     if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) { 
>         // 默认值与最小容量取较大值
>         minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity); 
>     }
>     ensureExplicitCapacity(minCapacity); 
> }
> 
> private void ensureExplicitCapacity(int minCapacity) {
>     modCount++;
>     if (minCapacity - elementData.length > 0)
>         grow(minCapacity); 
> }
> 
> private void grow(int minCapacity) {
>     int oldCapacity = elementData.length; // 旧容量
>     int newCapacity = oldCapacity + (oldCapacity >> 1); // 新容量为旧容量的1.5倍
>     if (newCapacity - minCapacity < 0) // 新容量小于最少容量，将新容量设置为最小容量。例如初始化长度为1，扩容后依然是1。
>         newCapacity = minCapacity;
>     if (newCapacity - MAX_ARRAY_SIZE > 0) // 新容量大于最大容量
>         newCapacity = hugeCapacity(minCapacity); // 指定新容量
>     // 拷贝扩容
>     elementData = Arrays.copyOf(elementData, newCapacity);  //长度超过最大长度时调用的方法 private static int hugeCapacity(int minCapacity) {
>     if (minCapacity < 0) // overflow
>         throw new OutOfMemoryError();
>     return (minCapacity > MAX_ARRAY_SIZE) ?
>         Integer.MAX_VALUE :
>         MAX_ARRAY_SIZE;
> }
> ```
>
> 1. 当数组是由默认空参或者长度设置为0生成的空数组，在首次添加数据时，会被设定成默认长度10，而后的每次扩容按照规则1.5倍扩容。
> 2. 当扩大后容量大于ArrayList定义的最大容量时，调用hugeCapacity方法，判断如果溢出Integer最大值会显示负数则抛出OutOfMemoryError，否则比较扩大前容量是否大于ArrayList最大容量，若大于则设置为Integer的最大容量，不大于则设置为Array List的最大容量。（其实ArrayList最大容量就是Integer最大容量-8）
>
> - get()
>
> ```java
>     public E get(int index) {
>     	//检查输入的索引是否合法
>         rangeCheck(index);
>         return elementData(index);
>     }
>     private void rangeCheck(int index) {
>         if (index >= size)
>             throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
>     }
>     E elementData(int index) {
>     	//去存储数据的数组中由下表获取
>         return (E) elementData[index];
>     }
> ```
>
> - set()
>
> ```java
>     public E set(int index, E element) {
>     	//检查输入的索引是否合法
>         rangeCheck(index);
>         //数组对应下标值的替换
>         E oldValue = elementData(index);
>         elementData[index] = element;
>         return oldValue;
>     }
> ```
>
> - remove()
>
> ```java
>  public E remove(int index) {
>         // 检查索引是否合法
>         rangeCheck(index);
>         modCount++;
>         E oldValue = elementData(index);
>         // 需要移动的元素的个数
>         int numMoved = size - index - 1;
>         if (numMoved > 0)
>             System.arraycopy(elementData, index+1, elementData, index,
>                              numMoved);
>         // 赋值为空，有利于进行GC
>         elementData[--size] = null; 
>         // 返回旧值
>         return oldValue;
>     }
> ```
>
> remove会将移除点之后的所有元素向前移动一位，并采用复制数组的方式建立新数组，并将最后一位设置为null有利于GC，如果移除的正好是最后一位则直接将它设置为Null
>
> - grow()
>   ArrayList的扩容函数，很简单
>
> ```java
> private void grow(int minCapacity) {
>         // overflow-conscious code
>         int oldCapacity = elementData.length;
>         // 扩容为原长度的1.5倍
>         int newCapacity = oldCapacity + (oldCapacity >> 1);
>         if (newCapacity - minCapacity < 0)
>             newCapacity = minCapacity;
>         if (newCapacity - MAX_ARRAY_SIZE > 0)
>             newCapacity = hugeCapacity(minCapacity);
>         // minCapacity is usually close to size, so this is a win:
>         // 将原数组复制到新指定长度的数组中，并将elementData 指向新数组
>         elementData = Arrays.copyOf(elementData, newCapacity);
>     }
> ```

#### 迭代器
> ```java
>     private class Itr implements Iterator<E> {
>         int cursor;       // 索引光标
>         int lastRet = -1; // 最后返回的节点索引
>         int expectedModCount = modCount;
> 
>         public boolean hasNext() {
>         	//当前索引光标等于size时表示没有下一个了
>             return cursor != size;
>         }
> 
>         @SuppressWarnings("unchecked")
>         public E next() {
>         	//检查modCount，fail-fast机制
>             checkForComodification();
>             int i = cursor;
>             if (i >= size)
>                 throw new NoSuchElementException();
>             Object[] elementData = ArrayList.this.elementData;
>             if (i >= elementData.length)
>                 throw new ConcurrentModificationException();
>             //光标向前移一位
>             cursor = i + 1;
>             return (E) elementData[lastRet = i];
>         }
> 
>         public void remove() {
>             if (lastRet < 0)
>                 throw new IllegalStateException();
>             checkForComodification();
> 
>             try {
>                 ArrayList.this.remove(lastRet);
>                 cursor = lastRet;
>                 lastRet = -1;
>                 expectedModCount = modCount;
>             } catch (IndexOutOfBoundsException ex) {
>                 throw new ConcurrentModificationException();
>             }
>         }
>       }
> ```


#### 特征
> 1. 读取速度快，每个元素有对应的索引，底层实现是数组。
> 2. 插入和删除较慢，时间复杂度不固定，如果插入（删除）中间部分的数据则需要移动大量元素，越靠近末尾需要移动的越少。


### Vector
#### 简介
> Vector和ArrayList十分类似，最大的区别在于它是线程安全的，在操作集合的方法上使用synchronized关键字。
>
> - Vector是一个数组队列，实现了可变大小的数组，它存储Object，可以存储null，它是线程安全的。
> - 实现RandomAccess接口，提供随机访问功能，可以通过元素的下标索引来快速获取元素对象。
> - 实现了Serializable接口，支持序列化。
> - 实现Cloneable接口，实现了clone()

#### 底层实现
> 这里只列出与ArrayList不同的地方。

> - 核心成员变量
>
> > - 
>
> ```java
> //存储数据的Object数组
> protected Object[] elementData;
> //元素数量即Vector的size
> protected int elementCount;
> //每次扩容时的增量大小
> protected int capacityIncrement;
> //序列化id
> private static final long serialVersionUID = -2767605614048989439L;
> ```
>
> - 构造方法
>
> ```java
> //无参构造，默认初始化长度为10，与jdk1.7以前的ArrayList空参构造相同
> public Vector() {
>         this(10);
>     }
> //指定长度的Vector构造方法，增量设置为0，后续使用grow()进行扩容
> public Vector(int initialCapacity) {
> 		//调用另一个构造
>         this(initialCapacity, 0);
>     }
>  
>  //指定长度和扩容增量的构造方法
> public Vector(int initialCapacity, int capacityIncrement) {
>         super();
>         if (initialCapacity < 0)
>             throw new IllegalArgumentException("Illegal Capacity: "+
>                                                initialCapacity);
>         this.elementData = new Object[initialCapacity];
>         this.capacityIncrement = capacityIncrement;
>     }
> ```
>
> - grow函数
>   长度不足时扩容的函数，如果指定了增量的大小，并且大于0则按照增量增加，否则按照默认的扩容策略为每次增长一倍。
>
> ```java
> private void grow(int minCapacity) {
>         // overflow-conscious code
>         int oldCapacity = elementData.length;
>         int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
>                                          capacityIncrement : oldCapacity);
>         if (newCapacity - minCapacity < 0)
>             newCapacity = minCapacity;
>         if (newCapacity - MAX_ARRAY_SIZE > 0)
>             newCapacity = hugeCapacity(minCapacity);
>         elementData = Arrays.copyOf(elementData, newCapacity);
>     }
> ```
>
> - 其他常用函数概述
>   操作列表的函数都被synchronized关键字修饰了，所以它是线程安全的List。
>   add(int index, E element)在指定位置插入的原理是数组的复制，插入点后续的所有元素下标+1，删除操作也是类似。

### 总结ArrayList和Vector的区别

> 1. 最大的区别在于Vector是线程安全的，但是同步的同时也影响了性能，在实现中使用了synchronized关键字修饰方法。
> 2. 构造方法上的区别，Vector可以指定扩容时的增量大小，ArrayList不能。
> 3. 扩容策略的区别，ArrayList每次增长为原来的1.5倍，Vector的默认增长为原先的2倍