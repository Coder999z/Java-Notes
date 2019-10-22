
* [synchronized](#synchronized)
  * [概述](#%E6%A6%82%E8%BF%B0)
  * [基本规则](#%E5%9F%BA%E6%9C%AC%E8%A7%84%E5%88%99)
    * [验证规则1](#%E9%AA%8C%E8%AF%81%E8%A7%84%E5%88%991)
    * [验证规则2](#%E9%AA%8C%E8%AF%81%E8%A7%84%E5%88%992)
    * [验证规则3](#%E9%AA%8C%E8%AF%81%E8%A7%84%E5%88%993)
  * [全局锁和对象锁](#%E5%85%A8%E5%B1%80%E9%94%81%E5%92%8C%E5%AF%B9%E8%B1%A1%E9%94%81)
  * [synchronized和同步代码块](#synchronized%E5%92%8C%E5%90%8C%E6%AD%A5%E4%BB%A3%E7%A0%81%E5%9D%97)
  * [总结](#%E6%80%BB%E7%BB%93)
* [参考](#%E5%8F%82%E8%80%83)


# synchronized
## 概述

> Java中每一个对象有且仅有一个与之关联的锁，这种锁成为`内部锁`，内部锁是一种`排它锁`(又称互斥锁，一个锁只能被一个线程持有)，内部锁是通过synchronized关键字实现的，它可以用来修饰方法和代码块。synchronized修饰的方法叫做同步方法，修饰的静态方法称为同步静态方法，修饰的代码块称为同步块。

> synchronized锁的是对象

## 基本规则

> 1. 当一个线程访问某对象的synchronized方法或synchronized代码块时，其他线程可以访问`该对象`的`非synchronized修饰的方法和代码块`。
> 2. 当一个线程访问某对象的synchronized方法或synchronized代码块时，其他线程对`该对象`的`该synchronized方法和代码块`访问将被阻塞。
> 3. 当一个线程访问某对象的synchronized方法或synchronized代码块时，其他线程对`该对象`的`其他synchronized方法和代码块`访问将被阻塞。

### 验证规则1

> ```java
> public static void main(String[] args) {
>         Inner inner = new Inner();
>         new Thread(() -> {
>             try {
>             	//执行同步方法
>                 inner.synMethod();
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }).start();
> 
>         new Thread(() -> {
>             try {
>             	//执行非同步方法
>                 inner.commonMethod();
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }).start();
>     }
> 
> class Inner {
> 
>     public synchronized void synMethod() throws InterruptedException {
>         for (int i = 0; i < 5; i++) {
>             System.out.println(Thread.currentThread().getName() + "--synMethod");
>             Thread.sleep(100);
>         }
>     }
> 
>     public void  commonMethod() throws InterruptedException {
>             for (int i = 0; i < 5; i++) {
>                 System.out.println(Thread.currentThread().getName() + "--commonMethod");
>                 Thread.sleep(100);
> 		}
>     }
> ```
>
> 执行结果：
>
> ```
> Thread-0--synMethod
> Thread-1--commonMethod
> Thread-0--synMethod
> Thread-1--commonMethod
> Thread-0--synMethod
> Thread-1--commonMethod
> Thread-0--synMethod
> Thread-1--commonMethod
> Thread-1--commonMethod
> Thread-0--synMethod
> ```
>
> 

### 验证规则2

> ```java
> public static void main(String[] args) {
>         Inner inner = new Inner();
>         Inner inner2 = new Inner();
> 
>         new Thread(() -> {
>             try {
>                 inner.synMethod();
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }).start();
> 
> 
>         new Thread(() -> {
>             try {
>                 inner.synMethod();
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }).start();
>     }
> 
> class Inner {
>     
>     public synchronized void synMethod() throws InterruptedException {
>         for (int i = 0; i < 5; i++) {
>             System.out.println(Thread.currentThread().getName() + "--synMethod");
>             Thread.sleep(100);
>         }
>     }
> }
> ```
>
> 验证结果:
>
> ```
> Thread-0--synMethod
> Thread-0--synMethod
> Thread-0--synMethod
> Thread-0--synMethod
> Thread-0--synMethod
> Thread-1--synMethod
> Thread-1--synMethod
> Thread-1--synMethod
> Thread-1--synMethod
> Thread-1--synMethod
> ```

### 验证规则3

> ```java
> public static void main(String[] args) {
>         Inner inner = new Inner();
>         Inner inner2 = new Inner();
> 
>         new Thread(() -> {
>             try {
>                 inner.synMethod();
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }).start();
> 
> 
>         new Thread(() -> {
>             try {
>                 inner.synBlock();
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }).start();
> }
> 
> 
> class Inner {
>     
>     public synchronized void synMethod() throws InterruptedException {
>         for (int i = 0; i < 5; i++) {
>             System.out.println(Thread.currentThread().getName() + "--synMethod");
>             Thread.sleep(100);
>         }
>     }
> 
>     public void synBlock() throws InterruptedException {
>     	//获得的是this对象的锁，也就是当前访问对象
>         synchronized (this) {
>             for (int i = 0; i < 5; i++) {
>                 System.out.println(Thread.currentThread().getName() + "--synBlock");
>                 Thread.sleep(100);
>             }
>         }
>     }
> }
> ```
>
> 验证结果：
>
> ```
> Thread-0--synMethod
> Thread-0--synMethod
> Thread-0--synMethod
> Thread-0--synMethod
> Thread-0--synMethod
> Thread-1--commonMethod
> Thread-1--commonMethod
> Thread-1--commonMethod
> Thread-1--commonMethod
> Thread-1--commonMethod
> ```
>
> 如果我们将synBlock中锁的对象this换成其他对象
>
> ```java
> class Inner {
> 	private final Object obj = new Object();
>     public void synBlock() throws InterruptedException {
>         //synchronized (this) {
>         synchronized (obj) {
>             for (int i = 0; i < 5; i++) {
>                 System.out.println(Thread.currentThread().getName() + "--synBlock");
>                 Thread.sleep(100);
>             }
>         }
>     }
> }
> ```
>
> 此时启动main方法的结果是异步执行，原因在于获取的对象锁不同，因为`synchronized (obj) `中获取的是obj对象的锁，同步方法则获取的是this对象的锁




## 全局锁和对象锁
> 对象锁：获取的是某个对象对应唯一关联的锁，如果对象是单例则也是全局锁。如非静态方法的synchronized关键字获取的就是对象锁
> 全局锁：该锁针对的是类对象，java中万物皆对象，类也是有对应的对象，例如静态方法的synchronized获得的锁是类对象，看例子：
>
> ```java
> 	public static void main(String[] args) {
>         Inner inner = new Inner();
>         new Thread(() -> {
>             try {
>                 Inner.synStaticMethod();
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }).start();
> 
>         new Thread(() -> {
>             try {
>                 inner.synBlock();
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }).start();
>     }
> 
> class Inner{
>     public synchronized static void synStaticMethod() throws InterruptedException {
>         for (int i = 0; i < 5; i++) {
>             System.out.println(Thread.currentThread().getName() + "--synStaticMethod");
>             Thread.sleep(100);
>         }
>     }
>     public void synBlock() throws InterruptedException {
>         synchronized (Inner.class) {
>             for (int i = 0; i < 5; i++) {
>                 System.out.println(Thread.currentThread().getName() + "--synBlock");
>                 Thread.sleep(100);
>             }
>         }
>     }
> }
> ```
>
> 运行结果
>
> ```
> Thread-0--synStaticMethod
> Thread-0--synStaticMethod
> Thread-0--synStaticMethod
> Thread-0--synStaticMethod
> Thread-0--synStaticMethod
> Thread-1--synBlock
> Thread-1--synBlock
> Thread-1--synBlock
> Thread-1--synBlock
> Thread-1--synBlock
> ```
>
> `Inner.synStaticMethod();`调用的是静态同步方法，获得的是Inner类对象对应的锁，与`synchronized (Inner.class) `获得的是同一个锁，类对象是全局唯一的，所以类对象类对象对应的锁也叫全局锁
>
> 参考别人博客拿来的一个例子：
>
> ```java
> 	public synchronized void isSyncA(){}
>     public synchronized void isSyncB(){}
>     public static synchronized void cSyncA(){}
>     public static synchronized void cSyncB(){}
> ```
>
> 假设，Something有两个实例x和y。分析下面4组表达式获取的锁的情况。
>
> ```
> (01) x.isSyncA()与x.isSyncB()
> (02) x.isSyncA()与y.isSyncA()
> (03) x.cSyncA()与y.cSyncB()
> (04) x.isSyncA()与Something.cSyncA()
> ```
>
> (1)不能被同时访问，因为访问的都是对象x的同步锁。
> (2)可以被同时访问，因为访问的不是同个对象的同步锁。
> (3)不能被同时访问，实例化对象调用静态方法和类名直接访问是相同的，但是不建议使用实例化对象因为会增加编译器解析成本。
> (4)可以被同时访问， x.isSyncA()访问的是x对象的同步锁，Something.cSyncA()访问的是全局锁


## synchronized和同步代码块
> 当synchronized修饰`普通(非静态)方法`时，执行它需要获得是`执行该方法的对象`的锁，也就是常说的this，所以使用synchronized修饰的方法的效果和在方法中使用synchronized修饰代码块锁定this对象的效果是相同的。如下代码所示
>
> ```java
> public synchronized void synMethod()  { 
> 	//逻辑代码
> }
> 
> public void synBlock() {
> 	synchronized (this) {
> 	//逻辑代码
> 	}
> }
> ```
>
> 当synchronized修饰`静态方法时`，执行时获得的是类对象的锁，下面两个方法获得的锁对象都是类对象对应的锁。
>
> ```java
> 	public synchronized static void synStaticMethod() throws InterruptedException {
>         //代码逻辑
>     }
>     public void synBlock() throws InterruptedException {
>         synchronized (Inner.class) {
>             //代码逻辑
>         }
>     }
> ```

## 总结
> synchronized关键字访问的锁是对象锁，这是它的核心，判断是否能够异步执行也就是判断访问的锁对象是否相同。

# 参考

> http://www.cnblogs.com/skywang12345/p/3479202.html