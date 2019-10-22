
* [HashSet](#hashset)
  * [概述](#%E6%A6%82%E8%BF%B0)
  * [数据结构](#%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84)
  * [源码分析](#%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90)
    * [属性](#%E5%B1%9E%E6%80%A7)
    * [构造函数](#%E6%9E%84%E9%80%A0%E5%87%BD%E6%95%B0)
    * [核心函数](#%E6%A0%B8%E5%BF%83%E5%87%BD%E6%95%B0)
    * [迭代器](#%E8%BF%AD%E4%BB%A3%E5%99%A8)
  * [总结](#%E6%80%BB%E7%BB%93)
    * [HashSet如何保证不重复](#hashset%E5%A6%82%E4%BD%95%E4%BF%9D%E8%AF%81%E4%B8%8D%E9%87%8D%E5%A4%8D)
    * [HashMap和HashSet的不同？](#hashmap%E5%92%8Chashset%E7%9A%84%E4%B8%8D%E5%90%8C)


# HashSet
## 概述
> 先掌握HashMap的底层实现再学习Hash Set会比较轻松
> HashSet 是一个没有重复元素的集合。
> 它的底层由HashMap实现。
> 不保证元素的顺序，而且HashSet允许使用 null 元素。
> HashSet是非同步的。

## 数据结构
> HashSet源码中存储对象使用的是HashMap，要了解HashSet就必须先了解HashMap。
> 简单复习一下HashMap的存储结构，在JDK1.8开始使用的是数组+链表+红黑树的组合
> ![在这里插入图片描述](D:\临时截图\20190417104155373.png)

## 源码分析

### 属性
> 只有三个属性
>
> ```java
> //序列化版本id
> static final long serialVersionUID = -5024744406713321676L;
> //存储数据的HashMap，这里还没初始化。
> private transient HashMap<E,Object> map;
> //用来作为value的对象
> private static final Object PRESENT = new Object();
> ```

### 构造函数
> 空参构造。调用HashMap的空参构造初始化。
>
> ```java
> public HashSet() {
>     map = new HashMap<>();
> }
> ```
> 参数为Collection的构造。
> 调用了HashMap的初始化table长度的构造
>
> ```java
>     public HashSet(Collection<? extends E> c) {
>     	//比较Collection的长度和16谁大取谁
>         map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
>         //将Collection中的元素复制入Set
>         addAll(c);
>     }
> ```
>
> 指定长度和填充因子的构造，调用的HashMap构造也同样是指定长度和填充因子的构造
>
> ```java
>     public HashSet(int initialCapacity, float loadFactor) {
>         map = new HashMap<>(initialCapacity, loadFactor);
>     }
> ```
>
> 指定长度的构造函数，HashMap同理
>
> ```java
> public HashSet(int initialCapacity) {
>     map = new HashMap<>(initialCapacity);
> }
> ```

### 核心函数
> 单纯调用HashMap函数的方法就不再赘述。
>
> 添加元素就是向Map中put一个节点，只存储key，value都是相同的对象，也就利用HashMap保证了set中元素的唯一性。
>
> ```java
>     public boolean add(E e) {
>     	//调用map的put方法，存入key，value都是PRESENT
>         return map.put(e, PRESENT)==null;
>     }
> ```
>
> clone()
>
> ```java
>     public Object clone() {
>         try {
>         	//先克隆新的Set对象
>             HashSet<E> newSet = (HashSet<E>) super.clone();
>             //克隆原set中的map再将它赋值给新set的map
>             newSet.map = (HashMap<E, Object>) map.clone();
>             return newSet;
>         } catch (CloneNotSupportedException e) {
>             throw new InternalError(e);
>         }
>     }
> ```
>
> contains()，调用HashMap的containsKey()，可见Set存储的对象放在map节点的key的位置
>
> ```java
>     public boolean contains(Object o) {
>         return map.containsKey(o);
>     }
> ```
>
>  iterator()，获得HashMap的keySet()实现的迭代器
>
> ```java
>     public Iterator<E> iterator() {
>         return map.keySet().iterator();
>     }
> ```
>
> ### 迭代器
>
> HashSet的迭代器调用的其实是HashMap的keySet实现的迭代器
>
> ```java
> public Iterator<E> iterator() {
>     return map.keySet().iterator();
> }
> ```

## 总结
### HashSet如何保证不重复
> 答：HashSet存储的Object实际是存储在HashMap的key中，HashMap会对每一次存入的节点的Key进行对比是否有重复，要是它的hash值相等并且equal或==相等则判定key相等，那么将会覆盖值，而每次Hash Set存入的值都是相同的Object，如果没有重复key则存入新的节点。
> ![在这里插入图片描述](https://img-blog.csdnimg.cn/20190417111947662.png)

### HashMap和HashSet的不同？

> |          | HashMap     | HashSet     |
> | -------- | ----------- | ----------- |
> | 实现接口 | 实现Map接口 | 实现Set接口 |
> | 存储方式 | key-value   | 单Object    |
>
> 