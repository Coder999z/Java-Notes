
* [概述](#%E6%A6%82%E8%BF%B0)
* [关于原子性](#%E5%85%B3%E4%BA%8E%E5%8E%9F%E5%AD%90%E6%80%A7)
* [关于可见性](#%E5%85%B3%E4%BA%8E%E5%8F%AF%E8%A7%81%E6%80%A7)
* [volatile变量的开销](#volatile%E5%8F%98%E9%87%8F%E7%9A%84%E5%BC%80%E9%94%80)
* [保障有序性、可见性](#%E4%BF%9D%E9%9A%9C%E6%9C%89%E5%BA%8F%E6%80%A7%E5%8F%AF%E8%A7%81%E6%80%A7)
  * [科普内存屏障](#%E7%A7%91%E6%99%AE%E5%86%85%E5%AD%98%E5%B1%8F%E9%9A%9C)
  * [读/写](#%E8%AF%BB%E5%86%99)
  * [总结](#%E6%80%BB%E7%BB%93)
* [volatile关键字的应用场景](#volatile%E5%85%B3%E9%94%AE%E5%AD%97%E7%9A%84%E5%BA%94%E7%94%A8%E5%9C%BA%E6%99%AF)
# 概述
> - volatile关键字用于修饰`共享可变`变量（没有使用final修饰的实例变量或静态变量）。
> - volatile关键字常被称为轻量级锁，它可以保证可见性和有序性。它能保证修饰变量的写操作的原子性，但没有锁的排他性，所以不会引起上下文切换，所以被称为轻量级锁。
> - volatile只能保证任意单个volatile变量读写的原子性

# 关于原子性
> `volatile关键字不保证它修饰的变量一定具有原子性。`
> 例1：
>
> ```java
> volatile int a;
> a++;
> ```
>
> ++操作是个复合操作，a++ = a + 1，其他线程可能在读取a时修改了a的值，因此该操作不具备原子性。
>
> 例2：
>
> ```java
> volatile int a;
> a = b + 1;
> ```
>
> 这个例子中，如果b是个局部变量则a的赋值操作具有原子性，如果b也是个共享可变变量则不具有原子性。
>
> 例3：
>
> ```java 
> volatile HashMap map = new HashMap();
> ```
>
> 创建HashMap对象是原子操作，因为创建该对象的步骤为：
>
> ```java 
> obj = allocate(HashMap.class);  //1
> invokeConstructor(obj);  //2
> map = obj;  //3
> ```
>
> volatile只保证第三部为原子操作，但是前两个步骤并未涉及共享变量，所以创建HashMap对象是原子操作。

# 关于可见性
> `volatile关键字仅仅保证读线程能读取到共享变量的相对新值`
> 例如使用volatile变量是一个数组，那么volatile只能对数组本身的引用起作用（读取数组的引用，更新数组的引用）。但是不对数组中的元素操作起作用（读取、更新元素）。

# volatile变量的开销
> volatile变量的读写操作不会引起上下文的切换，并且没有申请释放锁的操作，所以它的开销比锁小。写操作的主要开销在于冲刷处理器缓存的开销，读操作的开销在于每次读取volatile变量的值需要从高速缓存或者主存中读取，无法暂存在寄存器中，从而无法发挥访问的高效性。

# 保障有序性、可见性
## 科普内存屏障

> ![图片来自网络](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190506085027510.png)
>
> 内存屏障可以防止屏障两侧的指令重排序。
> 内存屏障可以将写缓冲区的数据刷新到主存或高速缓存中。



## 读/写

> | 写                 | 读                |
> | ------------------ | ----------------- |
> | 其他操作           | LoadLoad Barrier  |
> | StoreStore Barrier | volatile read     |
> | volatile write     | LoadStore Barrier |
> | StoreLoad Barrier  | 其他操作          |
>
> - 写操作
>   ①写操作之前的StoreStore屏障`禁止了volatile写操作与该操作之前的任何读写操作进行重排序`，从而保证了有序性。
>   ②写操作之后的StoreLoad屏障具有冲刷处理器缓存作用（`将写缓冲区的缓存同步到高速缓存或者主存`），这就使得该屏障之前的所有操作结果对其他处理器来说是可同步的。保障了`可见性`
> - 读操作
>   ①读操作之前的LoadLoad屏障具有刷新处理器缓存作用（`通过缓存一致性协议从其他处理器的高速缓存中同步或者从主内存中的相应变量进行缓存同步`）
>   ②读操作之后的LoadStore屏障确保volatile的读操作之前于后序的所有写操作，保障`有序性`。

## 总结

> - volatile通过内存屏障保障了对volatile变量的有序性
> - volatile的写操作通过存储屏障冲刷处理器缓存，将更新的数据同步到高速缓存和主存中，在读volatile变量时通过加载屏障进行刷新处理器缓存，同步已经更新的变量，进而保障了可见性。
> - volatile关键字也会提示JIT编译器，从而使编译器不会对相应代码做出优化。（[见多线程科普中JIT优化代码导致的可见性问题](https://blog.csdn.net/weixin_43184769/article/details/89708302)）

# volatile关键字的应用场景
> 其实就是结合它的特征匹配使用场景，主要是性能好，可见性，原子性
>
> 1. 原子性，用于标志状态，一般状态的转换只有true和false，volatile可以保证它的原子性。
> 2. 可见性， 独立观察，主要是利用可见性。例如：一个线程每隔一秒收集一次系统状态信息，使用volatile关键字让它收集的信息可见。
> 3. 有序性，单例模式，防止双重安全检查锁定中的重排序问题导致的脏读（后序介绍）。
> 4. 低开销，读多写少的场景下的读写锁，没有锁的争夺和申请和释放。实例代码：
>
> ```java
> public class Demo {  
>     private volatile int value;  
>  
>     //读操作，没有synchronized，提高性能  
>     public int getValue() {   
>         return value;   
>     }   
>   
>     //写操作，必须synchronized。因为x++不是原子操作  
>     public synchronized int increment() {  
>         return value++;  
>     }  
> }
> ```