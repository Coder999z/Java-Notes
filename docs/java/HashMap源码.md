* [HashMap](#hashmap)
  * [简介](#%E7%AE%80%E4%BB%8B)
  * [底层实现](#%E5%BA%95%E5%B1%82%E5%AE%9E%E7%8E%B0)
    * [存储结构](#%E5%AD%98%E5%82%A8%E7%BB%93%E6%9E%84)
    * [核心函数](#%E6%A0%B8%E5%BF%83%E5%87%BD%E6%95%B0)
      * [无参构造](#%E6%97%A0%E5%8F%82%E6%9E%84%E9%80%A0)
      * [带参构造](#%E5%B8%A6%E5%8F%82%E6%9E%84%E9%80%A0)
      * [hash()函数](#hash%E5%87%BD%E6%95%B0)
      * [put()函数](#put%E5%87%BD%E6%95%B0)
      * [resize() 函数](#resize-%E5%87%BD%E6%95%B0)
        * [分析链表分组原理](#%E5%88%86%E6%9E%90%E9%93%BE%E8%A1%A8%E5%88%86%E7%BB%84%E5%8E%9F%E7%90%86)
      * [get()函数](#get%E5%87%BD%E6%95%B0)
      * [remove()函数](#remove%E5%87%BD%E6%95%B0)
      * [tableSizeFor()函数](#tablesizefor%E5%87%BD%E6%95%B0)
      * [迭代器](#%E8%BF%AD%E4%BB%A3%E5%99%A8)
  * [HashMap线程不安全问题分析](#hashmap%E7%BA%BF%E7%A8%8B%E4%B8%8D%E5%AE%89%E5%85%A8%E9%97%AE%E9%A2%98%E5%88%86%E6%9E%90)
    * [resize()死循环](#resize%E6%AD%BB%E5%BE%AA%E7%8E%AF)
    * [过程分析](#%E8%BF%87%E7%A8%8B%E5%88%86%E6%9E%90)
    * [原因分析](#%E5%8E%9F%E5%9B%A0%E5%88%86%E6%9E%90)
  * [补充](#%E8%A1%A5%E5%85%85)
* [概念以及面试题](#%E6%A6%82%E5%BF%B5%E4%BB%A5%E5%8F%8A%E9%9D%A2%E8%AF%95%E9%A2%98)
  * [先说说我的见解](#%E5%85%88%E8%AF%B4%E8%AF%B4%E6%88%91%E7%9A%84%E8%A7%81%E8%A7%A3)
  * [hash碰撞](#hash%E7%A2%B0%E6%92%9E)
  * [为什么table的长度是2的幂次方](#%E4%B8%BA%E4%BB%80%E4%B9%88table%E7%9A%84%E9%95%BF%E5%BA%A6%E6%98%AF2%E7%9A%84%E5%B9%82%E6%AC%A1%E6%96%B9)
  * [什么HashMap中要对原hashCode再调用hash()进行运算？](#%E4%BB%80%E4%B9%88hashmap%E4%B8%AD%E8%A6%81%E5%AF%B9%E5%8E%9Fhashcode%E5%86%8D%E8%B0%83%E7%94%A8hash%E8%BF%9B%E8%A1%8C%E8%BF%90%E7%AE%97)
  * [关于填充因子loadFactor](#%E5%85%B3%E4%BA%8E%E5%A1%AB%E5%85%85%E5%9B%A0%E5%AD%90loadfactor)
  * [HashMap随JDK版本的优化（1\.7~1\.8）](#hashmap%E9%9A%8Fjdk%E7%89%88%E6%9C%AC%E7%9A%84%E4%BC%98%E5%8C%961718)
  * [参考](#%E5%8F%82%E8%80%83)



> 面试必问一定要好好理解。本篇文章暂不涉及红黑树。
# HashMap
## 简介
> 1. HashMap是散列表，存储的是键值对（key-value），HashMap 继承于AbstractMap，实现了Map、Cloneable、java.io.Serializable接口。可以进行克隆和序列化。
> 2. 在之前介绍的ArrayList和LinkedList中各有优劣，数组随机读取性能高而插入和删除开销较大，链表的随机读取困难而插入删除相对容易，而哈希表综合了两者的特性。

## 底层实现
### 存储结构
> HashMap底层存储数据是由Node[ ]实现，在初始化HashMap

> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190414170207215.png)

```java
//序列化版本id
private static final long serialVersionUID = 362498820763181265L;
//初始容量16（必须是2的幂）
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
//最大容量（必须是2的幂且小于2的30次方，传入的容量过大会被这个值替换）
static final int MAXIMUM_CAPACITY = 1 << 30;
//默认的加载因子
static final float DEFAULT_LOAD_FACTOR = 0.75f;
//链表转换成红黑树的长度 阈值
static final int TREEIFY_THRESHOLD = 8;
//决定何时将红黑树转变成链表
static final int UNTREEIFY_THRESHOLD = 6;
//链表转换成红黑树时进行判断数组的最小长度。如果比这个长度小则不转变成红黑树，而是进行扩容
static final int MIN_TREEIFY_CAPACITY = 64;

//静态内部类节点
static class Node<K,V> implements Map.Entry<K,V> {
		//key的hash值
        final int hash;
        final K key;
        V value;
        //同一hash值的下一个节点
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
	}
}

transient Node<K,V>[] table;
//所有Entity的集合
transient Set<Map.Entry<K,V>> entrySet;
//HashMap中拥有的元素
transient int size;
transient int modCount;
//长度阈值，table长度超过它时将会扩容
int threshold;
//确认的加载因子
final float loadFactor;
```
### 核心函数
#### 无参构造
> 无参构造并没有初始化table，而是只将加载因子初始化成了默认值。在首次put时会为table初始化
> 长度为默认16
```java
public HashMap() {
		//初始化加载因子为默认的0.75f
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }
```
#### 带参构造
> 指定初始化长度的构造函数
```java
public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
```
> 指定初始化长度和加载因子的构造函数
```java
public HashMap(int initialCapacity, float loadFactor) {
		//初始化长度小于0抛异常
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        //初始化长度大于最大值则设置成最大值
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        //初始化的填充因子小于等于0则抛异常
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }
```
> 参数为Map类型的构造函数
```java
public HashMap(Map<? extends K, ? extends V> m) {
		//加载因子指定为默认
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }

    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
        	//当table数组为空时初始化长度
            if (table == null) { // pre-size
                float ft = ((float)s / loadFactor) + 1.0F;
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
                if (t > threshold)
                    threshold = tableSizeFor(t);
            }
            //当插入的Map长度本身已经超过HashMap长度阈值，则扩容
            else if (s > threshold)
                resize();
            //循环便利出Map中的Entity经过计算索引位置创建Node
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }
```
#### hash()函数

```java
static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
```
> - 若key = null则返回0。
> - 若key不等于null则获取key的哈希值，与无符号右移16位的哈希值进行异或运算。

> 关于hashCode()函数。在Object中的默认实现为调用底层c++的函数，返回的是根据对象的内部地址转换成的整数。
在String中重写了hashCode()，将每个字符对应ASCII码的十进制数相加并每次乘以31，代码如下：

```java
public int hashCode() {
        int h = hash;
        if (h == 0 && value.length > 0) {
            char val[] = value;
			
            for (int i = 0; i < value.length; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }
```

#### put()函数

```java
public V put(K key, V value) {
		//调用putVal函数
        return putVal(hash(key), key, value, false, true);
    }
    /**
    * hash  key的hash值
    * key，vlaue键值
    * onlyIfAbsent 为true时，如果存在相同的键那么不进行覆盖。反之
    *  evict  只是个标志，为false表示该table时处在创建模式
    */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab;
        Node<K,V> p; 
        int n, i;
        //tab指针指向table对象，判断当前tab是否为空
        if ((tab = table) == null || (n = tab.length) == 0)
        	//若当前table为空则调用扩容函数初始化（扩容后续分析）
            n = (tab = resize()).length;
        // i 是根据哈希值进行运算获得的位置索引，p为tab[i]处的节点对象
        //判断索引位置是否有节点
        if ((p = tab[i = (n - 1) & hash]) == null)
        	//数组中索引为i的位置为Null，则创建新对象放入。
            tab[i] = newNode(hash, key, value, null);
         //若tab[i]处不为空，则说明发生了哈希冲突，按照链表法进行处理
        else {
            Node<K,V> e; K k;
            //判断节点处对象p的hash值与传入的是否相等，并且key值是否相同
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) {
                e = p;
             }
            //判断节点对象p是不是红黑树的节点
            else if (p instanceof TreeNode) {
            	//将p转换成红黑树节点并插入
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
             }
             //若节点处对象p的键和hash值和新传入的不同，而且p不是红黑树节点（还没超过链表规定长度）
            else {
            	//这个for循环是为了计算链表的长度
                for (int binCount = 0; ; ++binCount) {
                	//到达了链表的尾部
                    if ((e = p.next) == null) {
                    	//链表创建新节点
                        p.next = newNode(hash, key, value, null);
                        //如果链表长度达到阈值，转换成红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    //e的下一个节点指针不为空
                    //判断key是否相同
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                        break;
                    }
                    //用于遍历链表
                    p = e;
                }
            }
            //为刚才遍历中找到的key相同的键覆盖值并返回
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        //判断实际大小是否大于阈值，大于则扩容
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
```
> 借用美团技术团队在知乎的一张流程图

> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190413191539431.png)

#### resize() 函数
> 调整table大小的函数

```java
    final Node<K,V>[] resize() {
    	//oldTab指向原table数组对象
        Node<K,V>[] oldTab = table;
        //获得原容量
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        //原阈值
        int oldThr = threshold;
        int newCap, newThr = 0;
        //table的长度大于0
        if (oldCap > 0) {
        	//如果原长度大于了最大容量，就将阈值设置为整数的最大值，并返回
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            //如果原长度扩大一倍后小于最大容量，并且原长度大于默认值16
            else if (newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                //新的阈值为原来的一倍
                newThr = oldThr << 1; 
        }
        //如果table长度为0，而阈值大于0，即调用构造设置长度为0，阈值合法的情况
        else if (oldThr > 0) // initial capacity was placed in threshold
        	//设置新的长度为阈值
            newCap = oldThr;
        //如果原table长度为0，阈值也为0，即调用空参构造的情况
        else {               
        	//初始化长度为默认
            newCap = DEFAULT_INITIAL_CAPACITY;
            //初始化阈值为  （默认填充因子 * 默认长度）取整
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        //更新扩容后的阈值
        if (newThr == 0) {
        	//计算新阈值  （更新后长度  *  填充因子）
            float ft = (float)newCap * loadFactor;
            //判断扩容后的新阈值大小是否合法，若大于最大整数则取最大整数为阈值
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        //设置新阈值
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        	//创建新的table并设置长度为扩容过的长度
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        //HashMap存储的table指向新创建的newTab
        table = newTab;
        //将旧的table中的数据复制进新table中
        if (oldTab != null) {
        	//遍历旧table中的节点
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
               //若节点为空则继续下一个循环，不为空进入复制操作
                if ((e = oldTab[j]) != null) {
                	//释放原table的节点
                    oldTab[j] = null;
                    //如果当前节点没有指向下一个节点，不够成链表结构，则直接复制进新数组
                    if (e.next == null)
                    	//长度变了，计算新的散列位置
                        newTab[e.hash & (newCap - 1)] = e;
                    //如果e是红黑树节点类型，则e是头节点
                    else if (e instanceof TreeNode)
                    	/*
                    	* this 当前HashMap对象
                    	* newTab 新创建的table
                    	* j 当前节点在原table中的索引位置
                    	* oldCap 原table的长度
                    	* 如果节点是红黑树对象，进行拆分
                    	* */
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    //若节点有指向的下一个节点，即链表存在并且不是红黑树，则进行一下分组
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            //容量翻倍后，位置不变的节点
                            if ((e.hash & oldCap) == 0) {
                            	//尾部指针为空表示链表还没有长度
                                if (loTail == null)
                                    loHead = e;
                                else
                                	//尾部指针指向新添加的节点
                                    loTail.next = e;
                                //并将尾部指针指向新添加的节点
                                loTail = e;
                            }
                            //设置容量翻倍后位置改变的节点链表
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                            //直到没有下一个为止
                        } while ((e = next) != null);
                        if (loTail != null) {
                        	//新组合的链表的尾部可能还保留着原先指向的后继，清空。
                            loTail.next = null;
                            //将新table的对应位置设置为链表头
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                        	//同上
                            hiTail.next = null;
                            //位置改变的链表的新位置为 原位置+原table的长度
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
```
> 代码有点长，分解成几个步骤
> 1. 计算新的阈值大小newThr和新的table数组长度newCap。
> 2. 根据新的长度newCap创建新的table。
> 3. 将原数组中的Entry节点重新映射到新的数组中，如果节点是TreeNode类型则需要拆分红黑树，如果节点是普通Node类型则按照原链表进行分组。

##### 分析链表分组原理
> 假设当前HashMap中的存储结构如下：

> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/2019041415532283.png)

> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190414155718660.png)

> 可以看出规律计算位置索引时，只对最后三位进行了运算，容量翻倍的实质时进行了一次<<1计算，按照计算索引的公式：
(table长度 - 1) & hash。

> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190414160106896.png)

> 显而易见在扩容后参与&运算的位数由三位变成了四位，进行&运算时只有第四位不同，那么在扩容后对35和27的位置进行重新运算：

> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190414160854223.png)

> 可以发现在扩容后需要变换的位置为oldCap + 原位置索引。整个表进行调整后如下：

> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190414161421360.png)

> 使用8进行&运算是个是分巧妙的设计，7的二进制为0111，8的二进制为1000，进行&运算时正好符合了翻倍后多一位运算的规则，并且结果也只有两种情况，若无需改变则结果为0，需要改变位置结果则不为零。

#### get()函数
```java
public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        //判断当前table不为空，并且定位到该hash值对应的位置也不为空
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            //检查首个节点是否符合
            if (first.hash == hash && ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
            	//如果是红黑树节点，则调用getTreeNode获得节点
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                //若不是红黑树节点，则遍历链表获取
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```
> 简述步骤：
> 1. 判断表是否为空，并定位到该hash对应的索引位。
> 2. 检查首个节点是否符合，符合就返回
> 3. 判断存储的结构是红黑树还是链表，调用不同方法进行获取节点并返回。

#### remove()函数

```java
public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }

    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        //判断当前table不为空，并且定位到该hash值对应的位置也不为空
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null, e; K k; V v;
            //判断首个节点是否符合
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
			//首节点不符合则进入链表（红黑树）
            else if ((e = p.next) != null) {
            	//判断是不是红黑树节点，true的话调用方法getTreeNode获取要remove的节点
                if (p instanceof TreeNode)
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                //非红黑树节点则遍历链表，寻找符合的Node
                else {
                    do {
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            //若上述代码寻找到符合的节点则删除，并修复红黑树或链表的结构
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                if (node instanceof TreeNode)
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                else if (node == p)
                    tab[index] = node.next;
                else
                    p.next = node.next;
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }
```
> 简述步骤：
> 1. 判断表是否为空，并定位到该hash对应的索引位。
> 2. 检查首个节点是否符合，符合则设置node指针指向它。
> 3. 判断存储的结构是红黑树还是链表，调用不同方法进行获取节点并返回，并使node指针指向它
> 4. 判断是否寻找到符合的节点，找到则进行删除操作，并修复红黑树或链表结构

#### tableSizeFor()函数
> 这个函数所要实现的功能是，将table的长度设置成2的幂次方大小。例如输入的长度cap=10，经过此函数会返回一个最接近的2幂次方数16。
```java
static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
```

#### 迭代器
> HashMap中提供的迭代器是keySet，value Collection，EntrySet的迭代器。
> 代码也比较简单，继承了同一个父类HashIterator，只是在返回值上的差别。

```java
    final class KeyIterator extends HashIterator
        implements Iterator<K> {
        //返回key
        public final K next() { return nextNode().key; }
    }

    final class ValueIterator extends HashIterator
        implements Iterator<V> {
        //返回value
        public final V next() { return nextNode().value; }
    }

    final class EntryIterator extends HashIterator
        implements Iterator<Map.Entry<K,V>> {
        //返回整个Node
        public final Map.Entry<K,V> next() { return nextNode(); }
    }
```

```java
abstract class HashIterator {
        Node<K,V> next;        // next entry to return
        Node<K,V> current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            //初始化时获得table，桶
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

		//代码也比较简单，这个方法返回的e其实是在上一个nextNode中获取的，而此次调用nextNode中执行的是将e设置成current，并获得next，最后返回的其实是current也就是e
        final Node<K,V> nextNode() {
            Node<K,V>[] t;
            Node<K,V> e = next;
            //fail-fast机制
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            //这个if判断中将current指向了e，并且开始获取e的后继，如果没有就会去遍历数组
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            //最后返回的是e而不是上一个if中所寻找的下一个Node
            return e;
        }

        public final void remove() {
            Node<K,V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            //调用removeNode()移除结点
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }
```


## HashMap线程不安全问题分析
### resize()死循环
> 在JDK1.8之前，HashMap在动态扩容时复制旧table中的链表结点到新扩容后的newTab中使用的是头插法，每个节点都是插入在链表的头部，这也是导致多线程环境下出现环形链表死循环的根本原因。

### 过程分析
> 清晰思路只简化出核心代码：
```java
while(null != e) {
	//①断开链表前读取下一个节点，否则将会丢失链表
    Entry<K,V> next = e.next;
    //头插法，
    e.next = newTable[i];
    //复制节点到新数组
    newTable[i] = e;
    e = next;
}
```
> 假设目前HashMap的table的长度为2，扩容阈值为1。


> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190415154357376.png)

> 1. 线程T1向HashMap中插入k3，当执行到代码①处时cpu执行时间耗尽，暂时被挂起。
>
> 此时：
> 原table中已经插入k3，并触发扩容，创建了新数组newTab，但是还没开始复制操作。核心代码中的e指针指向k3，next则指向k4


> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190415154858835.png)

> 2. 此时线程T2开始运行，T2向HashMap中插入k2，并完成了整个扩容操作。


> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190415155729186.png)

> 3. T1获得CPU时间继续运行，此时e-->k3，next-->k4。按照代码顺序运行，可以得出循环中每次复制的节点队列：


> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/2019041516083719.png)

> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190415161406285.png)

> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190415162040228.png)


> ![1569402929151](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190415162733903.png)

> 此时k3-->k4，k4-->k3，形成了环形链表，在读取时会出现死循环。
### 原因分析 
> 线程T1在恢复执行时读取的链表已经是被T2扩容复制过的链表了，k3和k4已经交换了顺序，再加上复制时使用的头插法导致了环形链表。
## 补充
> 在jdk1.8中新增了一下三个函数，它在HashMap中是没有实现的，它是提供给LinkedHashMap重写的回调函数。后续博客介绍LinkedHashMap时补充

```java
    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }
```

# 概念以及面试题
## 先说说我的见解
> 欢迎指出错误。
> 1. 在HashMap中想要尽可能提高get()和Put()效率，需要将每个节点分配均匀，那么每个节点都应该尽量单独处于table中，此时读取的时间复杂度为O(1)，根据hash计算的索引就可以直接获得节点，读写速度是最快的。当然这是理想化的情况，实际中会出现**碰撞**，那么就需要减少碰撞。
> 2. 减少碰撞就需要在计算位置索引时&运算的结果更加多样 --->  那么根据&运算的特性要尽量是1，或者参与计算的长度要尽量长，显然key的hash值我们不能改变，只能使table的长度为2的幂次方，参与计算的长度过长会导致浪费空间（详见后文table长度为什么是2的幂次方）。
> 3. 不可避免的出现了碰撞，拉链法中使用链表存储，最坏情况所有节点插入了同一个链表中，时间复杂度从O(1)变O(n)，想要加快读取速度就得优化链表的读取速度，所以jdk1.8中引入了红黑树，但是为了平衡维护数据结构的开销，设置了链表长度的阈值超过8时才转换成红黑树，红黑树是平衡搜索树其时间复杂度为O(logN)。

## hash碰撞
> **概念：** 在HashMap中的哈希碰撞是：对象经过hash运算后的值再通过(table.length - 1) & hash 公式运算出来的结果相同
> **导致的问题：** 导致的结果是经过公式   (table.length - 1) & hash  计算的索引相同，在table中处于相同的索引位置。
> **解决方式：** 在HashMap中解决哈希碰撞的方式是拉链法，相同索引值的Node会被组成链表，链表的头部存储在table中

> ![1569403572487](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190414173345251.png)
## 为什么table的长度是2的幂次方
> 1. hash算法的目的是为了让每个值均匀的分布在table中，由于&运算的规则

> 1 & 1 = 1； 1 & 0 = 0； 0 & 0 = 0

> 在公式(table.length - 1) & hash中，只有令(table.length - 1) 的二进制数尽量都取1才能获得最多的计算情况

> 假如数组长度是10

> 1010 & 101010100101001001000 结果：1000 = 8
1010 & 101000101101001001001 结果：1000 = 8
1010 & 101010101101101001010 结果： 1010 = 10
1010 & 101100100111001101100 结果： 1000 = 8

> 例子来自：https://blog.csdn.net/qq_38182963/article/details/78940047 

> 2. 由于哈希值的取值范围太大，HashMap中将哈希值右移16位后与table的长度值进行取余操作（%），在底层实现中位运算中的&运算效率远高于取余，公式a % b == (b-1) & a ，当b是2的指数时，等式成立。

> 3. 便于扩容后的定位，减少操作次数。在上文中提到，2倍扩容后，&运算只是多了一位，对需要改变位置的Node只是由当前索引加上原长度即可确认新位置，若不是2的幂次方则需要对所有节点的位置进行运算。

## 什么HashMap中要对原hashCode再调用hash()进行运算？

> 所有处理的根本目的，都是为了提高 存储key-value的数组下标位置 的随机性 &
> 分布均匀性，尽量避免出现hash值冲突。即：对于不同key，存储的数组下标位置要尽可能不一样

```java
(h = key.hashCode()) ^ (h >>> 16)
```
> - 这行核心代码的意思是：获得Key的hashCode，并将其和自己无符号右移16位后的值进行异或运算。
> - 那么为何是右移16位呢？HashCode的取值范围和Integer整数相同-2147483648 到2147483647。转换成2进制就是31位2进制数，右移16位类似于取中点，将前半段和后半段进行异或运算。
> - 为了避免一大部分的极端情况，例如低位基本是0等，使后续计算中散列中的节点分布更加均匀。
 

## 关于填充因子loadFactor
> -  loadFactor默认为0.75，通常不对其进行改动。
> - loadFactor的值越大，HashMap对table中的空间利用率就越高，但是hash碰撞的几率也随之增大，链表（红黑树深度）可能也会越来越大，影响性能。
> - loadFactor的值越小，表中的数据过于稀疏，很容易就触发了扩容（阈值 = 当前容量 * loadFactor），占用的内存空间较大，好处是hash碰撞少了，效率高了。
> - 因此在空间与效率之间选择平衡的折中点来设置loadFactor

## HashMap随JDK版本的优化（1.7~1.8）
> 1. hash()函数中，在jdk1.7使用了9次的扰动处理，在1.8则只扰动了两次，各有优劣，扰动次数多可能随机性更强，扰动次数少性能更好。
> 2. jdk1.7中对链表的插入使用的是头插法，每次添加从链表头插入。jdk1.8中使用的是尾插法。性能上差别不大（线程不安全导致问题，后续补充）
> 3. 最大的改动是加入了红黑树，在链表长度大于8时，将链表转换成红黑树，在红黑树节点数小于6时恢复成链表，提高了性能。红黑树是平衡搜索树，其时间复杂度从O(n)变为O(longn)。
> 4. resize()函数中扩容以后，重新定位节点位置方式的不同。1.7中会对所有节点重新进行hash运算出位置（获得hashCode调用hash()扰动9次，再和长度取模运算，获得位置索引），1.8中也会计算新的位置但是简单得多，使用一次&运算，判断节点要么在原位置，要么位置索引变为oldCap + index。

## 参考
> https://blog.csdn.net/qq_36520235/article/details/82417949
> https://www.jianshu.com/p/8324a34577a0?utm_source=oschina-app