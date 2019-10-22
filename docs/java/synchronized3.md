
* [yield()](#yield)
  * [概述](#%E6%A6%82%E8%BF%B0)
  * [例子](#%E4%BE%8B%E5%AD%90)
  * [yield和wait区别](#yield%E5%92%8Cwait%E5%8C%BA%E5%88%AB)
* [sleep()](#sleep)
  * [概述](#%E6%A6%82%E8%BF%B0-1)
  * [sleep和wait区别](#sleep%E5%92%8Cwait%E5%8C%BA%E5%88%AB)
* [join()](#join)
  * [概述](#%E6%A6%82%E8%BF%B0-2)
  * [例子](#%E4%BE%8B%E5%AD%90-1)
  * [源码解析](#%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90)
  * [join不会释放锁！](#join%E4%B8%8D%E4%BC%9A%E9%87%8A%E6%94%BE%E9%94%81)
* [interrupt()](#interrupt)
  * [概述](#%E6%A6%82%E8%BF%B0-3)
  * [例子](#%E4%BE%8B%E5%AD%90-2)
  * [interrupt，interrupted，isInterrupted区别](#interruptinterruptedisinterrupted%E5%8C%BA%E5%88%AB)

# yield()
## 概述
> A hint to the scheduler that the current thread is willing to yield its current use of a processor. The scheduler is free to ignore this hint.

> 源码注释中这句话的意思是：向线程调度器表示当前线程放弃当前对处理器的使用。调度器可能忽略这个提示。
> 通俗的说就是当让前的线程从`运行状态`进入到`就绪状态`，CPU重新进行调度，但是不保证调用yield后其他线程就一定能抢到执行时间，即使其他线程和调用yield具有相同优先级。yield()不会释放锁！

## 例子

> ```java
> public class Demo6 implements Runnable {
>     @Override
>     public void run() {
>         for (int i = 0; i < 10; i++) {
>             Thread.yield();
>             System.out.println(Thread.currentThread().getName() + "continue");
>         }
>     }
> 
>     public static void main(String[] args) throws Exception {
>         Demo6 demo6 = new Demo6();
>         Thread t1 = new Thread(demo6);
>         //设置t1的优先级和main方法相同
>         t1.setPriority(5);
>         t1.start();
>         for (int i = 0; i < 10; i++) {
>             Thread.yield();
>             System.out.println(Thread.currentThread().getName() + "continue");
>         }
>     }
> }
> ```
>
> 运行结果：
>
> ```
> maincontinue
> Thread-0continue
> Thread-0continue
> maincontinue
> Thread-0continue
> maincontinue
> maincontinue
> maincontinue
> maincontinue
> maincontinue
> maincontinue
> maincontinue
> maincontinue
> Thread-0continue
> Thread-0continue
> Thread-0continue
> Thread-0continue
> Thread-0continue
> Thread-0continue
> Thread-0continue
> ```
>
> 运行的结果并没有规律，说明在yield后调度器不一定会将时间片分配给其他线程执行，否则结果应该是一条t1线程输出一条Main线程输出。
>
> 例2：
>
> ```java
> public class Demo5 implements Runnable {
>     private static final Object obj = new Object();
>     @Override
>     public void run() {
>         synchronized (obj) {
>             try {
>                 System.out.println(Thread.currentThread().getName() + "-start");
>                 System.out.println(Thread.currentThread().getName() + "-end");
>             } catch (Exception e) {
>                 e.printStackTrace();
>             }
>         }
>     }
> 
>     public static void main(String[] args) throws Exception {
>         Demo5 demo5 = new Demo5();
>         Thread t1 = new Thread(demo5);
>         synchronized (obj) {
>             System.out.println(Thread.currentThread().getName() + "-start");
>             t1.start();
>             Thread.yield();
>             System.out.println(Thread.currentThread().getName() + "-end");
>         }
>     }
> }
> ```
>
> 输出结果：
>
> ```
> main-start
> main-end
> Thread-0-start
> Thread-0-end
> ```
>
> 由输出结果可知，main线程调用yield()后并没有释放锁，直到main线程运行完同步代码块释放锁后t1才开始执行，所以yield()不会释放锁。

## yield和wait区别

> 1. wait()是将线程从运行状态切换到`阻塞状态`，而yield()是将线程从运行状态切换到`就绪状态`
> 2. wait()会释放持有的对象的同步锁，yield()不会。
> 3. wait()必须在线程拥有对象的同步锁时才能通过对象调用，yield()由线程对象静态调用。它们都是对当前CPU正在运行的线程进行操作。

# sleep()
## 概述

> ## 
>
> ```
> Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of milliseconds, subject to the precision and accuracy of system timers and schedulers. The thread does not lose ownership of any monitors.
> ```
>
> jdk源码的注释大意为：使`当前执行的线程`暂时休眠指定的之间，线程不会失去任何监视器(锁)的所有权。
> 比较简单就不写例子了。

## sleep和wait区别
> wait()和sleep()都是将线程从运行状态切换到阻塞状态，但是wait()会释放对象的同步锁，而sleep()不会。

# join()
## 概述
> join()  让主线程等待子线程结束后继续运行
> join( long millis )  让主线程等待子线程多少毫秒后继续运行
> join方法不会释放锁~

## 例子

> `例1：`
>
> ```java
> public class Demo7 implements Runnable {
>     @Override
>     public void run() {
>         try {
>             System.out.println(Thread.currentThread().getName() + "-start");
>             Thread.sleep(1000);
>             System.out.println(Thread.currentThread().getName() + "-end");
>         } catch (InterruptedException e) {
>             e.printStackTrace();
>         }
>     }
> 
>     public static void main(String[] args) throws InterruptedException {
>         System.out.println(Thread.currentThread().getName() + "-start");
>         Thread t1 = new Thread(new Demo7());
>         t1.start();
>         // A 
>         t1.join();
>         System.out.println(Thread.currentThread().getName() + "-end");
>     }
> }
> ```
>
> 输出结果：
>
> ```
> main-start
> Thread-0-start
> Thread-0-end
> main-end
> ```
>
> 代码中t1睡眠了1秒，CPU上下文切换到main函数但是没有继续运行，而是等待t1线程执行完毕，join的作用正是让父线程等待调用join的子线程执行完毕才继续执行。
>
> `例2：`
> 在例子中的A处，给join加上参数，变为t1.join(500)
> 输出结果：
>
> ```
> main-start
> Thread-0-start
> main-end
> Thread-0-end
> ```
>
> 程序运行的步骤为：main线程运行，t1线程启动进入就绪状态，t1调用join( 500 )，main线程等待t1线程500毫秒，t1线程运行打印了start后睡眠1秒，期间main线程等待时间耗尽则继续运行输出了main-end，t1线程继续运行输出了Thread0-end。



## 源码解析
> 看注释
>
> ```java
>     public final synchronized void join(long millis)
>     throws InterruptedException {
>         long base = System.currentTimeMillis();
>         long now = 0;
> 		//当输入的时间小于0时抛出如下异常
>         if (millis < 0) {
>             throw new IllegalArgumentException("timeout value is negative");
>         }
> 		//join()空参方法调用的就是join(0)
>         if (millis == 0) {
>         	//判断调用join()的线程是否还存活，存活则进入wait，防止伪唤醒
>             while (isAlive()) {
>                 wait(0);
>             }
>         } else {
>             while (isAlive()) {
>             	//规定时间 - 当前已运行时间
>                 long delay = millis - now;
>                 //超时则跳出循环
>                 if (delay <= 0) {
>                     break;
>                 }
>                 wait(delay);
>                 now = System.currentTimeMillis() - base;
>             }
>         }
>     }
> ```
>
> 看源码时有个疑问，当参数为0时调用的是wait(0)，无限等待直到被唤醒，可是代码中并没有看到notify()/notifyAll()，那么它是如何被唤醒的呢？
> 源码方法的注释中有这么一句：当一个线程中止(进入死亡状态)时，会调用该线程对象的notifyAll()方法。
>
> > As a thread terminates the {@code this.notifyAll} method is invoked.
>
> `join方法中有synchronized关键字，那么join是一个同步方法，t1在main线程调用join时，main线程进入t1对象的等待池，等到t1线程执行完毕后调用了自身的notifyAll()，唤醒了等待池中的main线程，main线程则继续运行。`
>
> 再举个小栗子帮助理解：
>
> ```java
> public class Demo7 implements Runnable {
>     @Override
>     public void run() {
>         try {
>             System.out.println(Thread.currentThread().getName() + "-start");
>             System.out.println(Thread.currentThread().getName() + "-end");
>         } catch (Exception e) {
>             e.printStackTrace();
>         }
>     }
>    
>     public static void main(String[] args) throws InterruptedException {
>         Thread t1 = new Thread(new Demo7());
>         synchronized (t1) {
>             t1.start();
>             t1.wait();
>         }
>     }
> }
> ```
>
> 运行结果：
>
> ```
> Thread-0-start
> Thread-0-end
> ```
>
> 代码中并没有显式的调用notify方法，但是main线程仍然被唤醒了，t1执行完毕后自动执行了notifyAll()。但是jdk不建议使用线程对象作为同步对象。

## join不会释放锁！

> ## 
>
> ```java
> public class Demo7 implements Runnable {
>     static Object obj = new Object();
> 
>     @Override
>     public void run() {
>         synchronized (obj) {
>             try {
>                 System.out.println(Thread.currentThread().getName() + "-start");
>                 System.out.println(Thread.currentThread().getName() + "-end");
>             } catch (Exception e) {
>                 e.printStackTrace();
>             }
>         }
>     }
> 
>     public static void main(String[] args) throws InterruptedException {
>         Thread t1 = new Thread(new Demo7());
>         t1.start();
>         synchronized (obj) {
>             System.out.println(Thread.currentThread().getName() + "-start");
>             t1.join();
>             System.out.println(Thread.currentThread().getName() + "-end");
>         }
>     }
> }
> ```
>
> 输出结果：
>
> ```
> main-start
> 然后死锁了
> ```
>
> 结合源码我分析了死锁的原因：
> 线程main启动了t1线程(就绪状态)，main线程获得对象obj的同步锁，打印了main-start，接着执行了t1.join()，在join方法中执行到
>
> ```java
> while (isAlive()) {
> 	wait(0);
> }
> ```
>
> t1线程处于就绪状态，while判断为true调用了wait()释放了t1线程的同步锁，这里需要注意释放的不是obj的同步锁，所以t1线程始终拿不到obj的同步锁，而t1线程始终等待自己运行结束来调用自身的notifyAll让main线程继续运行，互相等待而导致了死锁。


# interrupt()
## 概述
> 在api中介绍如下：https://docs.oracle.com/javase/8/docs/api/
>
> > Interrupts this thread.
> > 	Unless the current thread is interrupting itself, which is always permitted, the checkAccess method of this thread is invoked, which may cause a SecurityException to be thrown.
> > If this thread is blocked in an invocation of the wait(), wait(long), or wait(long, int) methods of the Object class, or of the join(), join(long), join(long, int), sleep(long), or sleep(long, int), methods of this class, then its interrupt status will be cleared and it will receive an InterruptedException.
> > If this thread is blocked in an I/O operation upon an InterruptibleChannel then the channel will be closed, the thread's interrupt status will be set, and the thread will receive a ClosedByInterruptException.
> > If this thread is blocked in a Selector then the thread's interrupt status will be set and it will return immediately from the selection operation, possibly with a non-zero value, just as if the selector's wakeup method were invoked.
> > If none of the previous conditions hold then this thread's interrupt status will be set.
> > Interrupting a thread that is not alive need not have any effect.
>
> 大致意思是：
>
> - 除非当前线程中断的是自身，否则调用checkAccess 方法检查权限，这可能抛出SecurityException 。
> - 如果这个线程被阻塞，调用wait、join、sleep方法，那么它的中断状态将被清除(先将中断标记设置为true，由于是阻塞状态再清除标记为false)，并`在wait、join、sleep方法抛出InterruptedException`。
> - 如果这个线程在I/O操作中被阻塞，使用中断操作将会使通道被关闭，并且设置中断状态，再抛出ClosedByInterruptException。
> - 如果这个线程在选择器中被阻塞，使用中断操作将会设置中断状态，并立即从选择操作中返回。
> - 如果不是上述三种情况则会设置线程中断状态，中断非活动线程无效

## 例子
> `中断阻塞线程`
>
> ```java
>     @Override
>     public void run() {
>         try {
>             while (true) {
>                 .....
>             }
>         } catch (InterruptedException e) {
>             //调用interrupt方法后使用sleep、wait、join的阻塞会抛出异常，从而跳出while循环
>         }
>     }
> ```
>
> 如果把try/catch放入while中则会导致无法中断
>
> ```java
>     @Override
>     public void run() {
>     	while(true) {
>         	try {
>           		......
>         	} catch (InterruptedException e) {
>             	//调用interrupt方法后使用sleep、wait、join的阻塞会抛出异常，但是并没有退出循环
>         	}
>         }
>     }
> ```
>
> 跑个栗子：
>
> ```java
> public class Demo8 implements Runnable {
> 
>     @Override
>     public void run() {
> 
>         try {
>             System.out.println(Thread.currentThread().getName() + "-start");
>             while (true) {
>                 Thread.sleep(10000);
>             }
>         } catch (InterruptedException e) {
>             e.printStackTrace();
>             System.out.println("t1 isInterrupted：" + t1.isInterrupted());
>             System.out.println(Thread.currentThread().getName() + "-end");
>         }
>     }
> 
>     public static void main(String[] args) {
>         try {
>             System.out.println(Thread.currentThread().getName() + "-start");
>             Thread t1 = new Thread(new Demo8());
>             t1.start();
>             //main线程让出CPU执行时间
>             Thread.sleep(100);
>             System.out.println("t1 isInterrupted：" + t1.isInterrupted());
>             t1.interrupt();
>             System.out.println("t1 isInterrupted：" + t1.isInterrupted());
>         } catch (Exception e) {
>             e.printStackTrace();
>         }
>     }
> }
> ```
>
> 运行结果：
>
> ```
> main-start
> t1 isInterrupted：false
> t1 isInterrupted：true
> Thread-0-start
> java.lang.InterruptedException: sleep interrupted
> 	at java.lang.Thread.sleep(Native Method)
> 	at demo.Demo8.run(Demo8.java:21)
> 	at java.lang.Thread.run(Thread.java:748)
> t1 isInterrupted：false
> Thread-0-end
> ```
>
> 从运行结果可以看出，sleep被中断了，抛出了InterruptedException异常，同时跳出了循环中。中断状态由默认的false，在调用中断后被设置成true，由于是阻塞状态在抛出异常前中断状态又被清除了。
>
> `中断运行中的线程`
>
> ```java 
> public class Demo8 extends Thread {
>     @Override
>     public void run() {
>         System.out.println(Thread.currentThread().getName() + "-start");
>         while (true) {
>             System.out.println(Thread.currentThread().getName() + "-run");
>         }
>     }
> 
>     public static void main(String[] args) {
>         try {
>             System.out.println(Thread.currentThread().getName() + "-start");
>             Demo8 t1 = new Demo8();
>             t1.start();
>             //main线程让出CPU执行时间
>             Thread.sleep(10);
>             t1.interrupt();
>             System.out.println(Thread.currentThread().getName() + "-end");
>         } catch (Exception e) {
>             e.printStackTrace();
>         }
>     }
> }
> ```
>
> 输出结果：
>
> ```
> Thread-0-run
> Thread-0-run
> .......
> ```
>
> 此程序中main线程调用了线程t1的interrupt()但是并没有中断它，`interrupt不是直接的中断线程而是修改了中断状态值`
> 将代码修改：
>
> ```java
> public class Demo8 extends Thread {
>     @Override
>     public void run() {
>         System.out.println(Thread.currentThread().getName() + "-start");
>         while (!isInterrupted()) {
>             System.out.println(Thread.currentThread().getName() + "-run");
>         }
>         System.out.println(Thread.currentThread().getName() + "-interrupt");
>     }
> 
>     public static void main(String[] args) {
>         try {
>             System.out.println(Thread.currentThread().getName() + "-start");
>             Demo8 t1 = new Demo8();
>             t1.start();
>             //main线程让出CPU执行时间
>             Thread.sleep(10);
>             t1.interrupt();
>             Thread.sleep(10);
>             System.out.println(Thread.currentThread().getName() + "-end");
>         } catch (Exception e) {
>             e.printStackTrace();
>         }
>     }
> }
> ```
>
> 输出结果：
>
> ```
> main-start
> Thread-0-start
> .........
> Thread-0-run
> Thread-0-interrupt
> main-end
> ```
>
> 将while中判断条件改为判断中断状态，main中t1.interrupt();修改了t1的中断状态，所以跳出了while循环打印Thread-0-interrupt。

## interrupt，interrupted，isInterrupted区别
> interrupt()是中断线程的方法，无返回值。
> isInterrupted()获取线程中断状态，被中断为true，反之
> interrupted()获取线程的中断状态并清空，例如中断状态为true，调用interrupted()后返回true并把中断状态清空成false