
* [HashTable](#hashtable)
  * [数据结构](#%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84)
  * [核心源码](#%E6%A0%B8%E5%BF%83%E6%BA%90%E7%A0%81)
    * [属性](#%E5%B1%9E%E6%80%A7)
    * [构造函数](#%E6%9E%84%E9%80%A0%E5%87%BD%E6%95%B0)
    * [存储结点](#%E5%AD%98%E5%82%A8%E7%BB%93%E7%82%B9)
    * [put()函数](#put%E5%87%BD%E6%95%B0)
    * [rehash()](#rehash)
  * [总结](#%E6%80%BB%E7%BB%93)
    * [HashMap和Hashtable有何不同](#hashmap%E5%92%8Chashtable%E6%9C%89%E4%BD%95%E4%B8%8D%E5%90%8C)

> 1. 在上两篇中已经详细介绍了HashMap的源码，理解了HashMap后再来理解HashTable也就轻松了很多，它们十分相似，只要找出不同点就能快速学习HashTable的底层实现。
> 2. 在Hashtable的注释中已经表明，Hashtable已经被废弃了，如果要在多线程中保证线程安全性请使用ConcurrentHashMap，在此只是简单介绍它与HashMap的对比
> 3. HashMap详细解析博客传送门：https://blog.csdn.net/weixin_43184769/article/details/89302728

# HashTable
## 数据结构
> 和Hash Map一样，HashTable也是散列表，底层实现也是数组+链表。但是HashMap在jdk1.8中引入了红黑树，底层是数组+链表+红黑树的组合
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/2019041521102559.png)

## 核心源码

### 属性
> ```java
> //存储链表的数组
> private transient Entry<?,?>[] table;
> //HashMap中的Entry数量
> private transient int count;
> //触发扩容的阈值
> private int threshold;
> //填充因子
> private float loadFactor;
> private transient int modCount = 0;
> //序列化id
> private static final long serialVersionUID = 1421746759512286392L;
> private transient volatile Set<K> keySet;
> private transient volatile Set<Map.Entry<K,V>> entrySet;
> private transient volatile Collection<V> values;
> 
> ```

### 构造函数
> 这里只指出和HashMap不同的地方。
>
> 1. 空参构造。默认长度为11，HashMap中空参构造不会初始化长度，只有在首次添加时会设置成默认的16
>
> ```java
> public Hashtable() {
>         this(11, 0.75f);
>     }
> ```
### 存储结点
> 这里和HashMap在jdk1.8之前一样使用的是Entry，但是和Node只是名称上的区别而已
>
> ```java
> private static class Entry<K,V> implements Map.Entry<K,V> {
>         final int hash;
>         final K key;
>         V value;
>         Entry<K,V> next;
> }
> ```

### put()函数
> 代码对参数的验证已经去掉了，保留核心代码。
> 这里与HashMap不同处在于：
>
> 1. 对hashCode计算索引位置的算法不同，HashTable使用的是将hashCode与整数的最大值进行&运算，再和table长度取模。HashMap是将hashCode的高16位和低16位进行^运算，然后和（table.length - 1)进行&运算。相比之下HashMap在取索引上的运算效率更高，使用了&代替取模运算。
> 2. 对新节点的插入方式不同，Hashtable使用的是头插法，HashMap使用的是尾插法。
> 3. 不允许key和value为空
>
> ```java
>     public synchronized V put(K key, V value) {
>     	if (value == null) {
>             throw new NullPointerException();
>         }
>         // Makes sure the key is not already in the hashtable.
>         Entry<?,?> tab[] = table;
>         int hash = key.hashCode();
>         //对key的hash值进行处理，类似与HashMap中的hash()，这里没有封装，使用的是取模运算
>         int index = (hash & 0x7FFFFFFF) % tab.length;
>         @SuppressWarnings("unchecked")
>         Entry<K,V> entry = (Entry<K,V>)tab[index];
>         //循环遍历链表中的节点，寻找有没有相同的Key进行覆盖
>         for(; entry != null ; entry = entry.next) {
>             if ((entry.hash == hash) && entry.key.equals(key)) {
>                 V old = entry.value;
>                 entry.value = value;
>                 return old;
>             }
>         }
> 		//没有相同的key则加入新节点
>         addEntry(hash, key, value, index);
>         return null;
>     }
> 
> 	private void addEntry(int hash, K key, V value, int index) {
>         modCount++;
> 
>         Entry<?,?> tab[] = table;
>         if (count >= threshold) {
>             // 如果HashTable长度超过阈值则扩容
>             rehash();
> 
>             tab = table;
>             hash = key.hashCode();
>             index = (hash & 0x7FFFFFFF) % tab.length;
>         }
> 
>         // 创建一个新节点，使用头插法，插入链表头部
>         @SuppressWarnings("unchecked")
>         Entry<K,V> e = (Entry<K,V>) tab[index];
>         tab[index] = new Entry<>(hash, key, value, e);
>         count++;
>     }
> ```

### rehash()
>  扩大Hashtable容量的函数，与HashMap中的resize()
>
> 1. HashMap中扩容为变成原长度的两倍，Hashtable则变为原来的两倍+1
> 2. HashMap在计算新的下表索引时，通过&运算后节点要么在原位置，要么等于原长度+原索引。而在Hashtable中则是对所有节点的位置重新进行完整的运算。
>
> ```java
> protected void rehash() {
>         int oldCapacity = table.length;
>         Entry<?,?>[] oldMap = table;
> 
>         //扩容为原长度的两倍+1
>         //计算新的长度
>         int newCapacity = (oldCapacity << 1) + 1;
>         if (newCapacity - MAX_ARRAY_SIZE > 0) {
>             if (oldCapacity == MAX_ARRAY_SIZE)
>                 // Keep running with MAX_ARRAY_SIZE buckets
>                 return;
>             newCapacity = MAX_ARRAY_SIZE;
>         }
>         Entry<?,?>[] newMap = new Entry<?,?>[newCapacity];
> 
>         modCount++;
> 		//计算新阈值
>         threshold = (int)Math.min(newCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
>         table = newMap;
> 		//将旧数组中的节点重新计算索引复制到新数组中
>         for (int i = oldCapacity ; i-- > 0 ;) {
>             for (Entry<K,V> old = (Entry<K,V>)oldMap[i] ; old != null ; ) {
>                 Entry<K,V> e = old;
>                 old = old.next;
> 
>                 int index = (e.hash & 0x7FFFFFFF) % newCapacity;
>                 e.next = (Entry<K,V>)newMap[index];
>                 newMap[index] = e;
>             }
>         }
>     }
> ```

## 总结
> 通过查看Hashtable的API可以发现，它对方法前都加上了synchronized关键字以保证同步，所以Hashtable是线程安全的。
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190415221400970.png)

### HashMap和Hashtable有何不同

> 1. Hahstable对方法加以synchronized修饰，所以Hashtable是线程安全的 ，HashMap则是线程不安全的
> 2. 数据结构上的不同。HashMap是用的是数组+链表+红黑树的组合最低查找时间复杂度是O(logn)，Hashtable使用的是数组+链表的组合，最坏情况的复杂度是O(n)。
> 3. 对hashCode的扰动方式不同，计算索引的方式也不同。Hashtable是将hashCode和**整数最大值**（二进制全是1）进行&运算再和数组长度**取模**。HashMap使用的是hashCode的**高16位和低16位**进行**^运算**再和**数组长度-1进行&运算**。HashMap对扰动算法进行了封装，Hashtable没封装。
> 4. 默认长度不同，Hashtable的默认初始化长度位11，HashMap是16
> 5. 扩容的大小不同，HashMap扩容后是原先的两倍，Hashtable是两倍+1。
> 6. 扩容后对节点位置的计算方式不同。Hashtable是将所有节点的位置进行完整的重新计算。HashMap则是判断节点位置是否有变动，无变更的还是在原先位置，变更的索引=原索引+原长度。
> 7. Hashtable中不允许Key或者value为null，HashMap中允许存在一个key为Null的节点，value可以有多个节点为null。