
* [概述](#%E6%A6%82%E8%BF%B0)
* [数据结构](#%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84)
  * [二叉查找树](#%E4%BA%8C%E5%8F%89%E6%9F%A5%E6%89%BE%E6%A0%91)
  * [红黑树](#%E7%BA%A2%E9%BB%91%E6%A0%91)
* [函数源码解析](#%E5%87%BD%E6%95%B0%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90)
  * [构造函数](#%E6%9E%84%E9%80%A0%E5%87%BD%E6%95%B0)
  * [左旋/右旋](#%E5%B7%A6%E6%97%8B%E5%8F%B3%E6%97%8B)
    * [左旋：](#%E5%B7%A6%E6%97%8B)
    * [右旋](#%E5%8F%B3%E6%97%8B)
  * [put()](#put)
  * [get()](#get)
  * [remove()](#remove)
* [参考博客：](#%E5%8F%82%E8%80%83%E5%8D%9A%E5%AE%A2)

# 概述
> - TreeMap 是一个有序的key-value集合，它是按照提供的比较器进行比较，没有添加则使用默认比较器，注意这里的有序不是按照添加的顺序。
> - TreeMap 继承于AbstractMap，所以它是一个Map，即一个key-value集合。
> - TreeMap 实现了Cloneable接口，它能被克隆。实现了java.io.Serializable接口，它支持序列化。
> - TreeMap基于红黑树实现， containsKey、get、put 和 remove 的时间复杂度是 log(n) 。
> - TreeMap是线程不安全的。 它的iterator 方法返回的迭代器支持fail-fastl机制。

# 数据结构
> TreeMap底层使用红黑树存储节点数据，红黑树本质是一颗二叉查找树，简单介绍一下二叉查找树。

## 二叉查找树
> 二叉查找树（Binary Search Tree），也称有序二叉树（ordered binary tree）,排序二叉树（sorted binary tree），是指一棵空树或者具有下列性质的二叉树
> - 若任意结点的左子树不空，则左子树上所有结点的值均小于它的根结点的值；
>  - 若任意结点的右子树不空，则右子树上所有结点的值均大于它的根结点的值；
> -  任意结点的左、右子树也分别为二叉查找树。
> -  没有键值相等的结点（no duplicate nodes）。

> 因为，一棵由n个结点，随机构造的二叉查找树的高度为lgn，所以顺理成章，一般操作的执行时间为O（logn）.。

>但二叉树若退化成了一棵具有n个结点的线性链后，则此些操作最坏情况运行时间为O（n）。后面我们会看到一种基于二叉查找树-红黑树，它通过一些性质使得树相对平衡，使得最终查找、插入、删除的时间复杂度最坏情况下依然为O（logn）。

## 红黑树
> 红黑树在二叉查找树的基础上添加了着色和5条相关限制，从而保证了红黑树的增删改查最坏情况下的复杂度为O(logn)。

> 红黑树的性质：
> 1）每个结点要么是红的，要么是黑的。
> 2）根结点是黑的。
> 3）每个叶结点（叶结点即指树尾端NIL指针或NULL结点）是黑的。
> 4）如果一个结点是红的，那么它的俩个儿子都是黑的。
> 5）对于任一结点而言，其到叶结点树尾端NIL指针的每一条路径都包含相同数目的黑结点。

> 入下图所示就是一颗红黑树下图引自(wikipedia：http://t.cn/hgvH1l)
> ![](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190507093646177.png)

# 函数源码解析
## 构造函数
> ```java
> //默认空参，无比较器
> public TreeMap() {
>         comparator = null;
>     }
> //提供比较器的构造函数
> public TreeMap(Comparator<? super K> comparator) {
>         this.comparator = comparator;
>     }
> //创建一个包含了map的TreeMap
> public TreeMap(Map<? extends K, ? extends V> m) {
>         comparator = null;
>         putAll(m);
>     }
> ```

## 左旋/右旋
> 在红黑树插入或者删除节点后可能会破坏它的性质，通常需要修改着色和左右旋转操作来恢复红黑树的特性。

### 左旋：
>
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190507094653206.png)
> 如图，将节点X按照X和Y的连线为轴进行左旋，使X成为了Y的左孩子，同时Y的左孩子β，称为了X的右孩子。

> 步骤大致为：
> 1. 将Y的左子节点β设置为X的右子节点
> 2. 将X的父节点设置为Y
> 3. 将Y的父节点设置成X的父节点，若X的父节点为空则设置Y为根节点。

> 代码实现：（注释按照上图示例）
>
> ```java
> 	//假设传入的p是上图中的X
>     private void rotateLeft(Entry<K,V> p) {
>         if (p != null) {
>         	//指针r指向p的右孩子。（X的右孩子Y）
>             Entry<K,V> r = p.right;
>             //p的右孩子设置为r的左孩子。（X的右孩子设置成β）
>             p.right = r.left;
>             if (r.left != null){
>             	//如果r的做孩子不为空，将父节点设置为p。（β父节点设置为X）
>                 r.left.parent = p;
>             }
>             //r的父节点设置为p的父节点。（Y的父节点设置为X的父节点）
>             r.parent = p.parent;
>             if (p.parent == null)
>             	//p的父节点为空则表示它的根节点，将r设置成根节点
>                 root = r;
>             else if (p.parent.left == p)
>             	//p是父节点的左子节点则将r设置成左子节点
>                 p.parent.left = r;
>             else
>             	//同理设置成右子节点
>                 p.parent.right = r;
>             //r的左节点设置成p
>             r.left = p;
>             //p的父节点设置成r
>             p.parent = r;
>         }
>     }
> ```
>
> 

### 右旋
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190507101007920.png)
> 如图所示，将节点Y按照X和Y的连接线为轴进行右旋，称为了X的 右子节点，同时X的右子节点β称为了Y的左子节点。和左旋差别不大，不做详细介绍
>
> ```java
> private void rotateRight(Entry<K,V> p) {
>         if (p != null) {
>             Entry<K,V> l = p.left;
>             p.left = l.right;
>             if (l.right != null) l.right.parent = p;
>             l.parent = p.parent;
>             if (p.parent == null)
>                 root = l;
>             else if (p.parent.right == p)
>                 p.parent.right = l;
>             else p.parent.left = l;
>             l.right = p;
>             p.parent = l;
>         }
>     }
> ```

## put() 
> 向TreeMap中添加节点本质上是在红黑树中添加节点。
>
> ```java
>     public V put(K key, V value) {
>     	//获得根节点
>         Entry<K,V> t = root;
>         //根节点为空时，将添加的节点设置为根节点。
>         if (t == null) {
>             compare(key, key); // type (and possibly null) check
>             root = new Entry<>(key, value, null);
>             size = 1;
>             modCount++;
>             return null;
>         }
>         int cmp;
>         Entry<K,V> parent;
>         Comparator<? super K> cpr = comparator;
>         //如果有提供比较器
>         if (cpr != null) {
>         	
>             do {
>                 parent = t;
>                 cmp = cpr.compare(key, t.key);
>                 //小于0表示key“小于”比较节点，按照二叉查找树性质取左子节点
>                 if (cmp < 0)
>                     t = t.left;
>                 //大于0表示大于比较节点，取右子节点
>                 else if (cmp > 0)
>                     t = t.right;
>                 //等于0表示两个Key相等，进行值的覆盖
>                 else
>                     return t.setValue(value);
>             //一直循环到叶节点（NIL或者NULL指针）为止
>             } while (t != null);
>         }
>         //没有提供比较器
>         else {
>             if (key == null)
>                 throw new NullPointerException();
>             @SuppressWarnings("unchecked")
>             	//使用key对象中实现的比较器，如果没有实现Compare<String>接口就会报错，
>             	//并且泛型必须为String类型
>                 Comparable<? super K> k = (Comparable<? super K>) key;
>             //这部分和上面的相同
>             do {
>                 parent = t;
>                 cmp = k.compareTo(t.key);
>                 if (cmp < 0)
>                     t = t.left;
>                 else if (cmp > 0)
>                     t = t.right;
>                 else
>                     return t.setValue(value);
>             } while (t != null);
>         }
>         //新建节点
>         Entry<K,V> e = new Entry<>(key, value, parent);
>         //比较的值如果小于0则设置新节点为父节点的左子节点，反之
>         if (cmp < 0)
>             parent.left = e;
>         else
>             parent.right = e;
>         //修复红黑树
>         fixAfterInsertion(e);
>         size++;
>         modCount++;
>         return null;
>     }
> ```

> 概括一下步骤为：
> 1. 判断根节点是否为空，为空则初始化根节点
> 2. 使用比较器寻找新添加节点的父节点
> 3. 创建新节点，与父节点连接
> 4. 修复红黑树，节点数++，fail-fast的modCount++

> 介绍修复红黑树源码前先介绍一下红黑树添加节点后修复的几种情况：
> 先回顾一下红黑树的性质：

> (1) 每个节点或者是黑色，或者是红色。
> (2) 根节点是黑色。
> (3) 每个叶子节点是黑色。（叶结点即指树尾端NIL指针或NULL结点）
> (4) 如果一个节点是红色的，则它的子节点必须是黑色的。
> (5) 从一个节点到该节点的子孙节点的所有路径上包含相同数目的黑节点。

>为了减少处理的情况，就需要尽量少的违背它的性质，所以讲新节点暂时设置成红色最为合适。

> 添加节点可能出现的情况有：
> 1. 如果添加的节点为根节点，那么直接涂黑
> 2. 如果添加的节点的父节点是黑色，那么无需处理
> 3. 如果添加的节点的父节点是红色，那么就违背了性质(4)，解决方式如下：
>
> | 情况                                                         | 解决方案                                                     |
> | ------------------------------------------------------------ | ------------------------------------------------------------ |
> | 1. 当前节点的父节点是红色，叔叔（父节点的兄弟节点）节点也是红色 | (1)将父节点和叔叔节点涂成黑色，   (2)将祖父(爷爷)节点设置成红色， (3)将祖父节点设置成当前节点，继续操作 |
> | 2.当前节点的父节点是红色，叔叔节点是黑色，且当前节点是父节点的右孩子 | (1)将父节点设置成当前节点， (2)将当前节点进行左旋            |
> | 3.当前节点的父节点是红色，叔叔节点是黑色，且当前节点是父节点的左孩子 | (1)将父节点设置成黑色，(2)将祖父节点设置成红色，(3)对祖父节点进行右旋 |
>
> ```java
> private void fixAfterInsertion(Entry<K,V> x) {
> 		//设置新节点为红色
>         x.color = RED;
> 		//只有父节点是红色的情况需要处理
>         while (x != null && x != root && x.parent.color == RED) {
>         	//父节点是祖父节点的左节点
>             if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
>             	//y指向祖父节点的右节点（叔叔节点）
>                 Entry<K,V> y = rightOf(parentOf(parentOf(x)));
>                 //如果叔叔节点是红色，对应情况1
>                 if (colorOf(y) == RED) {
>                 	//设置父节点、叔叔节点为黑色，祖父节点为红色
>                     setColor(parentOf(x), BLACK);
>                     setColor(y, BLACK);
>                     setColor(parentOf(parentOf(x)), RED);
>                     x = parentOf(parentOf(x));
>                     //叔叔节点为黑色的情况
>                 } else {
>                 	//如果x是父节点的右子节点，符合情况2
>                     if (x == rightOf(parentOf(x))) {
>                     	//将x指向父节点，并对x进行左旋
>                         x = parentOf(x);
>                         rotateLeft(x);
>                     }
>                     //执行if内语句左旋后，当前节点x是父节点的左子节点，符合情况3
>                     //设置父节点为黑色，祖父节点为红色，对祖父节点进行右旋操作。
>                     setColor(parentOf(x), BLACK);
>                     setColor(parentOf(parentOf(x)), RED);
>                     rotateRight(parentOf(parentOf(x)));
>                 }
>                 //同上
>             } else {
>                 Entry<K,V> y = leftOf(parentOf(parentOf(x)));
>                 if (colorOf(y) == RED) {
>                     setColor(parentOf(x), BLACK);
>                     setColor(y, BLACK);
>                     setColor(parentOf(parentOf(x)), RED);
>                     x = parentOf(parentOf(x));
>                 } else {
>                     if (x == leftOf(parentOf(x))) {
>                         x = parentOf(x);
>                         rotateRight(x);
>                     }
>                     setColor(parentOf(x), BLACK);
>                     setColor(parentOf(parentOf(x)), RED);
>                     rotateLeft(parentOf(parentOf(x)));
>                 }
>             }
>         }
>         root.color = BLACK;
>     }
> ```
>
> 

>
> ![在这里插入图片描述](https://img-blog.csdnimg.cn/20190507114201255.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzE4NDc2OQ==,size_16,color_FFFFFF,t_70)

## get()
> 比较简单，看注解
>
> ```java
> public V get(Object key) {
>         Entry<K,V> p = getEntry(key);
>         return (p==null ? null : p.value);
> }
> final Entry<K,V> getEntry(Object key) {
>         // 如果有比较器则调用getEntryUsingComparator获取
>         if (comparator != null)
>             return getEntryUsingComparator(key);
>         if (key == null)
>             throw new NullPointerException();
>         @SuppressWarnings("unchecked")
>         	// 使用默认实现的比较器
>             Comparable<? super K> k = (Comparable<? super K>) key;
>         Entry<K,V> p = root;
>         //循环比较到p为叶节点等于空时表示没找到指定节点。结束循环
>         while (p != null) {
>             int cmp = k.compareTo(p.key);
>             // cmp < 0表示Key“小于”p节点的key值，按照二叉搜索树性质，小于的放在左子节点
>             if (cmp < 0)
>                 p = p.left;
>             else if (cmp > 0)
>                 p = p.right;
>             else
>                 return p;
>         }
>         return null;
> }
> 
>     final Entry<K,V> getEntryUsingComparator(Object key) {
>         @SuppressWarnings("unchecked")
>             K k = (K) key;
>         Comparator<? super K> cpr = comparator;
>         if (cpr != null) {
>             Entry<K,V> p = root;
>             while (p != null) {
>                 int cmp = cpr.compare(k, p.key);
>                 if (cmp < 0)
>                     p = p.left;
>                 else if (cmp > 0)
>                     p = p.right;
>                 else
>                     return p;
>             }
>         }
>         return null;
>     }
> ```
>
> 

##  remove()
> 移除TreeMap中的节点实际上是移除红黑树中的节点，先介绍一下红黑树移除节点的操作：
> 这篇文章写得很好：https://www.cnblogs.com/qingergege/p/7351659.html

> 红黑树的删除操作也分三种情况
> 1. 被删除的节点没有子节点，说明它是一个非空的叶子节点，如果是红色直接删除就可以了，黑色则需要调整后删除
> 2. 被删除的节点有一个子节点，直接删除该节点，并让其子节点接替它的位置即可，如果被删除节点是黑色则需要修复
> 3. 被删除的节点有两个子节点，这个情况就相对麻烦，需要从子树中选出后继，TreeMap中选取的是右子树中最小的，（选择最小的是因为方便查找并且它没有子节点需要处理。选左子树中最大的也同理），使用后继节点将需要删除的节点替换，同时将后继节点原先位置的节点设为待删除节点。重复1,2操作。
>
> ```java
>     public V remove(Object key) {
>     	//调用getEntry获取指定节点
>         Entry<K,V> p = getEntry(key);
>         if (p == null)
>             return null;
> 
>         V oldValue = p.value;
>         deleteEntry(p);
>         return oldValue;
>     }
>     
> 	private void deleteEntry(Entry<K,V> p) {
>         modCount++;
>         size--;
> 
>         if (p.left != null && p.right != null) {
>         	//如果左右子节点都不为空则获取后继节点，successor()为获取后继节点的方法
>             Entry<K,V> s = successor(p);
>             //使用后继节点替换掉当前节点
>             p.key = s.key;
>             p.value = s.value;
>             //指针p指向后继节点原先的位置
>             p = s;
>         } 
> 
>         // 如果左子树不为空指向左子树，为空则指向右子树
>         Entry<K,V> replacement = (p.left != null ? p.left : p.right);
>         
> 		/* p只有一颗子树非空的情况，用它的子节点顶替它
> 			这种情况比较简单，符合红黑树性质的只有黑色节点加一个红色子节点的情况
> 		*/
>         if (replacement != null) { 
>             //	replacement 与 p的父节点连接
>             replacement.parent = p.parent;
>             if (p.parent == null)
>                 root = replacement;
>             //如果p是父节点的左子节点，那么父节点的左子节点设置为replacement 
>             else if (p == p.parent.left)
>                 p.parent.left  = replacement;
>             else
>                 p.parent.right = replacement;
> 
>             // 将p节点与其他节点的连接断开
>             p.left = p.right = p.parent = null;
> 
>             // p节点为黑色，则需要修复红黑树关系。进入修复后会直接将红色子节点涂黑
>             if (p.color == BLACK)
>                 fixAfterDeletion(replacement);
>                 
>         } else if (p.parent == null) { // return if we are the only node.
>             root = null;
>         } else { //  p没有子节点的情况
>         	//如果是黑色节点则先需要修复关系
>             if (p.color == BLACK)
>                 fixAfterDeletion(p);
> 			
> 			//删除p节点
>             if (p.parent != null) {
>                 if (p == p.parent.left)
>                     p.parent.left = null;
>                 else if (p == p.parent.right)
>                     p.parent.right = null;
>                 p.parent = null;
>             }
>         }
>     }
> 
>     static <K,V> TreeMap.Entry<K,V> successor(Entry<K,V> t) {
>         if (t == null)
>             return null;
>         else if (t.right != null) {
>         	// 右子树不为空则寻找右子树中key最小的节点
>             Entry<K,V> p = t.right;
>             while (p.left != null)
>                 p = p.left;
>             return p;
>         } else {
>         	//右子树为空则寻找第一个向左走的祖先
>             Entry<K,V> p = t.parent;
>             Entry<K,V> ch = t;
>             while (p != null && ch == p.right) {
>                 ch = p;
>                 p = p.parent;
>             }
>             return p;
>         }
>     }
> 
>  private void fixAfterDeletion(Entry<K,V> x) {
>  		//循环停止的条件是x为根节点，或者x不是黑色
>         while (x != root && colorOf(x) == BLACK) {
>        
>             if (x == leftOf(parentOf(x))) { //	x是左子节点的情况
>             	//x的兄弟节点
>                 Entry<K,V> sib = rightOf(parentOf(x));
> 				
> 				//	情况1：x为黑色，兄弟节点为红色。此操作是将情况1转换成情况2或3或4
>                 if (colorOf(sib) == RED) {
>                     setColor(sib, BLACK);
>                     setColor(parentOf(x), RED);
>                     rotateLeft(parentOf(x));
>                     sib = rightOf(parentOf(x));
>                 }
> 
> 				//	情况2：x为黑色，兄弟节点的左子节点和右子节点都是黑色。如果情况1转换成
> 				//	了情况2，那么情况2之后就会退出循环，因为情况1会将X的父节点变为红色。
>                 if (colorOf(leftOf(sib))  == BLACK &&
>                     colorOf(rightOf(sib)) == BLACK) {
>                     setColor(sib, RED);
>                     x = parentOf(x);
>                 } else {
>                 	//情况3：X为黑色，兄弟节点的右子节点为黑色，左子节点颜色任意。
>                 	// 经过此处理后实际上就变成了情况4
>                     if (colorOf(rightOf(sib)) == BLACK) {
>                         setColor(leftOf(sib), BLACK);
>                         setColor(sib, RED);
>                         rotateRight(sib);
>                         sib = rightOf(parentOf(x));
>                     }
>                     //情况4：x为黑色，兄弟节点的右子节点为红色，左子节点任意
>                     //	经过此操作后将会退出循环。
>                     setColor(sib, colorOf(parentOf(x)));
>                     setColor(parentOf(x), BLACK);
>                     setColor(rightOf(sib), BLACK);
>                     rotateLeft(parentOf(x));
>                     x = root;
>                 }
>             } else { // x是右子节点的情况
>                 Entry<K,V> sib = leftOf(parentOf(x));
> 
>                 if (colorOf(sib) == RED) {
>                     setColor(sib, BLACK);
>                     setColor(parentOf(x), RED);
>                     rotateRight(parentOf(x));
>                     sib = leftOf(parentOf(x));
>                 }
> 
>                 if (colorOf(rightOf(sib)) == BLACK &&
>                     colorOf(leftOf(sib)) == BLACK) {
>                     setColor(sib, RED);
>                     x = parentOf(x);
>                 } else {
>                     if (colorOf(leftOf(sib)) == BLACK) {
>                         setColor(rightOf(sib), BLACK);
>                         setColor(sib, RED);
>                         rotateLeft(sib);
>                         sib = leftOf(parentOf(x));
>                     }
>                     setColor(sib, colorOf(parentOf(x)));
>                     setColor(parentOf(x), BLACK);
>                     setColor(leftOf(sib), BLACK);
>                     rotateRight(parentOf(x));
>                     x = root;
>                 }
>             }
>         }
> 
>         setColor(x, BLACK);
>     }
> ```



# 参考博客：

> http://www.cnblogs.com/skywang12345/p/3245399.html#aa5
>
> https://github.com/julycoding/The-Art-Of-Programming-By-July/blob/master/ebook/zh/03.01.md
>
> http://www.cnblogs.com/wuchanming/p/4444961.html
>
> http://www.cnblogs.com/skywang12345/p/3245399.html
>
> https://www.cnblogs.com/CarpenterLee/p/5525688.html
>
> http://www.importnew.com/24930.html

