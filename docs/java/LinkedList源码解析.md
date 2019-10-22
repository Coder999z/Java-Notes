
* [LinkedList](#linkedlist)
    * [简介](#%E7%AE%80%E4%BB%8B)
    * [底层实现](#%E5%BA%95%E5%B1%82%E5%AE%9E%E7%8E%B0)
      * [核心方法分析](#%E6%A0%B8%E5%BF%83%E6%96%B9%E6%B3%95%E5%88%86%E6%9E%90)
    * [特征](#%E7%89%B9%E5%BE%81)
    * [拓展](#%E6%8B%93%E5%B1%95)
* [LinkedList和ArrayList对比](#linkedlist%E5%92%8Carraylist%E5%AF%B9%E6%AF%94)


## LinkedList
#### 简介
> - LinkedList类实现了Queue接口，它能够进行队列操作
> - 实现了Deque接口，能做双端队列使用
> - 实现了Serializable接口，支持序列化。
> - 实现Cloneable接口，实现了clone()
> - 它是线程不安全的

#### 底层实现 
> - LinkedList的数据结构为双向链表结构，它实现了Deque接口，那么LinkedList也是双端队列的一种实现。成员变量中的头尾节点为真实头尾节点的引用。
>
> ```java
>     transient int size = 0;//长度
>     transient Node<E> first;//头节点
>     transient Node<E> last;//尾节点
> ```
>
> - LinkedList的每个Node都是一个对象，在源码中有内部类实现。
>
> ```java
> private static class Node<E> {
>         E item; // 数据域
>         Node<E> next; // 后继
>         Node<E> prev; // 前驱
>         // 构造函数，赋值前驱后继
>         Node(Node<E> prev, E element, Node<E> next) {
>             this.item = element;
>             this.next = next;
>             this.prev = prev;
>         }
>     }
> ```

##### 核心方法分析
> - add函数
>
> ```java
> public boolean add(E e) {
> 		//添加到链表末尾
>         linkLast(e);
>         return true;
>     }
>     
> void linkLast(E e) {
> 		//获得末尾节点
>         final Node<E> l = last;
>         //构造新插入的节点，前驱为当前末尾节点，后继为null
>         final Node<E> newNode = new Node<>(l, e, null);
>         //尾节点指针指向新建的节点
>         last = newNode;
>         if (l == null)
>         	//如果末尾节点为空则表示是个空链表，那么头尾节点都是刚插入的节点
>             first = newNode;
>         else
>         	//末尾节点不为空则将其后继设置为新插入的节点
>             l.next = newNode;
>         //链表长度++
>         size++;
>         modCount++;
>     }
> ```
>
> - addAll函数
>
> ```java
>     public boolean addAll(int index, Collection<? extends E> c) {
>     	//检查插入位置的有效性
>         checkPositionIndex(index);
> 		//将集合转换成数组，为了防止在以下操作中集合的数据发生改变
>         Object[] a = c.toArray();
>         int numNew = a.length;
>         if (numNew == 0)
>             return false;
> 		//pred前驱，succ后继
>         Node<E> pred, succ;
>         if (index == size) {
>         	//插入位置在末尾
>             succ = null;
>             pred = last;
>         } else {
>         	//获得插入位置的节点，并设置为后继
>             succ = node(index);
>             //前驱设置为插入节点的前驱
>             pred = succ.prev;
>         }
> 		
>         for (Object o : a) {
>             @SuppressWarnings("unchecked")
>             E e = (E) o;
>             Node<E> newNode = new Node<>(pred, e, null);
>             if (pred == null)
>                 first = newNode;
>             else
>             	//将前驱节点的后继设置成当前构造的节点
>                 pred.next = newNode;
>              //设置前驱节点指针指向当前创建的节点
>             pred = newNode;
>         }
> 		
>         if (succ == null) {
>         	//如果在末尾插入则将末尾指针指向最后创建的节点
>             last = pred;
>         } else {
>             pred.next = succ;
>             succ.prev = pred;
>         }
> 
>         size += numNew;
>         modCount++;
>         return true;
>     }
> ```
>
> - node函数（根据下表获取节点）
>
> ```java
>     Node<E> node(int index) {
>         // 判断插入的位置在链表前半段或者是后半段
>         if (index < (size >> 1)) { // 插入位置在前半段
>             Node<E> x = first; 
>             for (int i = 0; i < index; i++) // 从头结点开始正向遍历
>                 x = x.next;
>             return x; // 返回该结点
>         } else { // 插入位置在后半段
>             Node<E> x = last; 
>             for (int i = size - 1; i > index; i--) // 从尾结点开始反向遍历
>                 x = x.prev;
>             return x; // 返回该结点
>         }
>     }
> ```
>
> 根据索引进行查找时会先判断索引在前半段还是后半段，然后进行遍历查找，这样保证了只要遍历最多一半的节点就能找到它，但是查找效率依然远不如ArrayList的索引访问。
>
> - unlink函数
>   由于双链表函数的特性，对链表中的节点进行移除或者替换操作都是将该节点与前驱后继节点的引用设置为空，表示断开连接，再将前驱与后继连接上引用。

#### 特征
> - 由于存储数据的结构原因。LinkedList它的顺序访问相对高效，而随机访问效率较低。
>
> - 与Array List相比，LinkedList对随机节点的增加删除操作效率会更高。
> - LInkedList实现了多个接口，可以用作多种数据结构的实现
> - LInkedList不存在容量不足的问题，只要JVM的堆空间足够大。

#### 拓展
> 在jdk1.7之前LinkedList使用的是循环链表，1.7及以后使用的是非循环链表，差别在于：
>
> - 1.6
>
> ```java
> private transient Entry<E> header = new Entry<E>(null, null, null);
> private transient int size = 0;
> ```
>
> - 1.6以后
>
> ```java
> transient int size = 0;
> transient Node<E> first;
> transient Node<E> last;
> ```
>
> 1.6及其之前的版本提供的两个基本属性为size和一个header Node对象，header的前驱存储尾节点的引用，后继则是第二个元素，形成了一个环形结构。
>
> 在1.6以后使用的是两个指针指向头尾。优点是，代码逻辑更加清晰易懂，节省了一个对象的内存空间。

## LinkedList和ArrayList对比
> 1. 底层实现的区别：ArrayList使用的是Object[ ]数组实现存储，LinkedList底层实现是双链表
> 2. ArrayList随机读取速度更快，但是插入慢，LinkedList顺序读取和插入删除操作快，随机读取慢。原因：ArrayList底层由数组存储，每个元素有对应的索引，所以随机读取快，LinkedList底层又双向链表实现，随机读取时最多需要便利半个列表来搜索。进行插入或者删除操作时，ArrayList操作的时间复杂度又元素的位置决定，进行插入删除操作时，后续的所有元素都需要向后或者向前移动（又数组复制实现），而LinkedList只需要找到对应的位置改变前驱和后继的引用指针即可完成修改。
> 3. 对内存空间的占用，ArrayList存在一定的空间浪费，因为他动态扩容会在列表尾部预留一定的空间，在越长时预留的也越多，LinkedList的空间占用在于每一个节点除了存储本节点数据以外还需要存储前驱和后继的数据。