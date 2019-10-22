
* [LinkedHashMap](#linkedhashmap)
  * [概述](#%E6%A6%82%E8%BF%B0)
  * [数据结构](#%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84)
  * [源码分析](#%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90)
    * [Entry类](#entry%E7%B1%BB)
    * [添加结点](#%E6%B7%BB%E5%8A%A0%E7%BB%93%E7%82%B9)
    * [获取结点](#%E8%8E%B7%E5%8F%96%E7%BB%93%E7%82%B9)
    * [删除结点](#%E5%88%A0%E9%99%A4%E7%BB%93%E7%82%B9)
    * [遍历分析](#%E9%81%8D%E5%8E%86%E5%88%86%E6%9E%90)
  * [总结](#%E6%80%BB%E7%BB%93)
    * [LinkedHashMap和HashMap有何不同？](#linkedhashmap%E5%92%8Chashmap%E6%9C%89%E4%BD%95%E4%B8%8D%E5%90%8C)
    * [LinkedHashMap如何有序遍历](#linkedhashmap%E5%A6%82%E4%BD%95%E6%9C%89%E5%BA%8F%E9%81%8D%E5%8E%86)
* [参考](#%E5%8F%82%E8%80%83)

# LinkedHashMap
## 概述
> LinkedHashMap继承自HashMap，它和HashMap基本相同，仅多了维护双向链表的代码。在LinkedHashMap中通过维护双向链表来解决HashMap的无序性问题。

## 数据结构
> 在HashMap的数组+链表+红黑树的基础上多了双向链表
> ![在这里插入图片描述](D:\临时截图\201904171311238.png)

## 源码分析
### Entry类
> Entry继承了HashMap中的Node类，仅仅多了两个属性before和after用来维护双向链表.
>
> ```java
> static class Entry<K,V> extends HashMap.Node<K,V> {
>         Entry<K,V> before, after;
>         Entry(int hash, K key, V value, Node<K,V> next) {
>             super(hash, key, value, next);
>         }
>     }
> ```

### 添加结点
> HashMap中的put()
>
> ```java
> public V put(K key, V value) {
>     return putVal(hash(key), key, value, false, true);
> }
> ```

>  HashMap中的putVal()
>
> ```java
> final V putVal(int hash, K key, V value, boolean onlyIfAbsent,boolean evict) {
>                    
>         {.........}
>         
>         //当table中索引位没有结点时创建新结点放入
>         if ((p = tab[i = (n - 1) & hash]) == null)
>             tab[i] = newNode(hash, key, value, null);
>         else {
>         
>         	//当key相同时覆盖
>             {.........}
> 
>             //当结点时红黑树结点时添加TreeNode，putTreeVal添加时调用了newTreeNode()
>             else if (p instanceof TreeNode)
>                 e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
>             else {
>             
> 				//循环遍历链表
>             	{
>             	//链表尾部加入新节点
>                if ((e = p.next) == null) {
>                         p.next = newNode(hash, key, value, null);
>                         //长度大于等于7转换成红黑树
>                         if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
>                             treeifyBin(tab, hash);
>                         break;
>                     }
>                     
>                		//链表中发现相同的key覆盖
>                     {........}
>                     
>                   }
>             }
>             
>        
>         //判断是否需要扩容，modCount++
>       	{...........}
>       	
>         return null;
>     }
> ```
>
> 

> LinkedHashMap中重写的newNode()和newTreeNode()
>
> > 
>
> ```java
>     Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
>     	//创建LinkedHashMap重写的Entry类
>         LinkedHashMap.Entry<K,V> p =
>             new LinkedHashMap.Entry<K,V>(hash, key, value, e);
>          //调用维护双向链表的函数
>         linkNodeLast(p);
>         return p;
>     }
> 	
> 	//维护双向链表的函数
>     private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
>         LinkedHashMap.Entry<K,V> last = tail;
>         //将尾部指针指向新添加的Entry  p
>         tail = p;
>         if (last == null)
>             head = p;
>         else {
>         	//p的前驱指向之前的尾结点
>             p.before = last;
>             //之前的尾节点的后继指向p
>             last.after = p;
>         }
>     }
> 	
> 	//红黑树结点的双向链表维护和上述的雷同，不赘述
>     TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
>         TreeNode<K,V> p = new TreeNode<K,V>(hash, key, value, next);
>         linkNodeLast(p);
>         return p;
>     }
> ```
>
> 简单总结一下LinkedhashMap添加结点的步骤
>
> 1. 调用父类的put函数
> 2. put函数中创建新节点的函数newNode和newTreeNode被重写，多态的特性使得调用了LinkedHashMap重写的创建方式
> 3. 在LinkedHashMap重写的函数中创建结点的同时维护了双向链表的关系

### 获取结点

> ```java
>     public V get(Object key) {
>         Node<K,V> e;
>         if ((e = getNode(hash(key), key)) == null)
>             return null;
>        
>         //accessOrder属性在其中一个构造函数中可以指定
>         if (accessOrder)
> 			//此方法是将key对应的结点加到链表尾部（不改动实际的位置，只是改变了链表中的前驱后继关系，逻辑位置）
>             afterNodeAccess(e);
>         return e.value;
>     }
> ```
>
> 

### 删除结点
> HashMap中的remove()
>
> ```java
>     public V remove(Object key) {
>         Node<K,V> e;
>         return (e = removeNode(hash(key), key, null, false, true)) == null ?
>             null : e.value;
>     }
> ```
>
> HashMap中的removeNode()
>
> ```java
> final Node<K,V> removeNode(int hash, Object key, Object value,boolean matchValue, boolean movable) {
>         Node<K,V>[] tab; Node<K,V> p; int n, index;
> 
> 			//根据key在table中寻找对应的结点node
> 			{...........}
> 			
> 			//从链表或者红黑树中移除node，并维护链表或红黑树关系
> 			{...........}
> 			
> 			//执行完以上操作后调用的回调函数，在LinkedHashMap中重写
>             afterNodeRemoval(node);
>     }
> ```
>
> LinkedHashMap中重写的afterNodeRemoval()函数，用于维护remove后双向链表的关系
>
> ```java
>     void afterNodeRemoval(Node<K,V> e) { // unlink
>     	//向下转型，转成LinkedHashMap重写的Entry
>         LinkedHashMap.Entry<K,V> p =
>             (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
>         p.before = p.after = null;
>         if (b == null)
>             head = a;
>         else
>             b.after = a;
>         if (a == null)
>             tail = b;
>         else
>             a.before = b;
>     }
> ```
>
> 

### 遍历分析
> 在概述中我们知道LinkedHashMap的遍历是有序的，而HashMap是无序的，分析一下原因。
>
> ```java
> LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
>         linkedHashMap.put("源码",1);
>         linkedHashMap.put("分析",2);
>         linkedHashMap.put("博客",3);
>         Set<Object> keySet = linkedHashMap.keySet();
>         for (Object obj : keySet) {
>             System.out.print(obj);
>         }
> 
>         System.out.println();
> 
>         HashMap<Object, Object> hashMap = new HashMap<>();
>         hashMap.put("源码", 1);
>         hashMap.put("分析", 2);
>         hashMap.put("博客", 3);
>         Set<Object> keySet2 = hashMap.keySet();
>         for (Object obj : keySet2) {
>             System.out.print(obj);
>         }
> ```
>
> 以上demo的输出为
>
> > 源码分析博客
> > 分析博客源码
>
> Map的遍历本质上是先遍历key再通过Key去获得value，返回的keySet的遍历方式有迭代器和for循环，增强for在编译后也是转换成迭代器实现，所以只需要分析LinkedHashMap的迭代器是如何实现的就可以知道它为何能有序输出了。（EntrySet也是同理，keySet只是在最后多调用了getKey()）
>
> ```java
> Iterator iterator = linkedHashMap.keySet().iterator();
> ```
>
> 一步步分析调用，有点长但是不难
>
> ```java
> public Set<K> keySet() {
>         Set<K> ks = keySet;
>         if (ks == null) {
>         	//新建LinkedKeySet对象
>             ks = new LinkedKeySet();
>             keySet = ks;
>         }
>         return ks;
>     }
> 
> final class LinkedKeySet extends AbstractSet<K> {
>         public final Iterator<K> iterator() {
>         	//新建LinkedKeyIterator对象
>             return new LinkedKeyIterator();
>         }
>  }
> 
> //继承了LinkedHashIterator
> final class LinkedKeyIterator extends LinkedHashIterator
>         implements Iterator<K> {
>        
>         public final K next() { 
>         	//调用父类的nextNode(在下面)获得node再返回Key
>         	return nextNode().getKey(); 
>         }
>     }
> 
>     abstract class LinkedHashIterator {
>         LinkedHashMap.Entry<K,V> next;
>         LinkedHashMap.Entry<K,V> current;
>         int expectedModCount;
> 
>         LinkedHashIterator() {
>         	//初始化时从双向链表头开始读
>             next = head;
>             //fail-fast机制
>             expectedModCount = modCount;
>             current = null;
>        }
> 		
> 		public final boolean hasNext() {return next != null;}
> 
>         final LinkedHashMap.Entry<K,V> nextNode() {
>             LinkedHashMap.Entry<K,V> e = next;
>             if (modCount != expectedModCount)
>                 throw new ConcurrentModificationException();
>             if (e == null)
>                 throw new NoSuchElementException();
>             current = e;
>             
> 			//读取的是当前结点的后继
>             next = e.after;
>             return e;
>         }
>  }
> ```
>
> 简单总结一下步骤，并和HashMap进行对比更加直观。
>
> | 步骤 | LinkedHashMap                                       | HashMap                                                 |
> | ---- | --------------------------------------------------- | ------------------------------------------------------- |
> | 1    | 获取keySet对象                                      | 同左                                                    |
> | 2    | 获取keySet实现的迭代器                              | 同左                                                    |
> | 3    | 迭代器初始化next=双向链表头，获得next时获取的是后继 | 迭代器初始化时获得table[ ]，next()是按照table的索引遍历 |

## 总结

### LinkedHashMap和HashMap有何不同？
> 1. 数据结构上LinkedHashMap多了一条双向链表
> 2. 迭代器的实现上不同，LinkedHashMap是从双向链表头开始遍历，HashMap是按照table[ ]的索引开始读。
> 3. 性能上HashMap比LinkedHashMap要高，因为LinkedHashMap需要比HashMap多维护一条双向链表的开销

### LinkedHashMap如何有序遍历
> LinkedHashMap在对结点进行操作时会多维护一条双向链表，链表的顺序就是添加的顺序，在遍历时迭代器是从链表头开始遍历，所以它是有序的。

# 参考

> https://www.imooc.com/article/22931