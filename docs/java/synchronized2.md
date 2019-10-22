
* [概述](#%E6%A6%82%E8%BF%B0)
* [wait](#wait)
  * [参考注释](#%E5%8F%82%E8%80%83%E6%B3%A8%E9%87%8A)
* [notify、notifyAll](#notifynotifyall)
  * [参考注释](#%E5%8F%82%E8%80%83%E6%B3%A8%E9%87%8A-1)
* [实例](#%E5%AE%9E%E4%BE%8B)
  * [wait和notify](#wait%E5%92%8Cnotify)
  * [wait(long time)和notify](#waitlong-time%E5%92%8Cnotify)
  * [wait和 notifyAll](#wait%E5%92%8C-notifyall)
  * [notify和notifyAll的区别](#notify%E5%92%8Cnotifyall%E7%9A%84%E5%8C%BA%E5%88%AB)
  * [wait要放在while中](#wait%E8%A6%81%E6%94%BE%E5%9C%A8while%E4%B8%AD)
  * [性能问题](#%E6%80%A7%E8%83%BD%E9%97%AE%E9%A2%98)

本文参考了此博客部分内容：http://www.cnblogs.com/skywang12345/p/3479224.html

# 概述
> 在Object类中，定义了wait(), notify()和notifyAll()等接口。wait()的作用是让当前线程进入等待状态，同时，wait()也会让当前线程释放它所持有的锁。而notify()和notifyAll()的作用，则是唤醒当前对象上的等待线程；notify()是唤醒单个线程，而notifyAll()是唤醒所有的线程。

# wait
## 参考注释
> 节选了部分核心注释，理解它很有帮助
>
> ```java
>     /**
>      * 调用wait的当前线程必须拥有该对象的锁
>      * The current thread must own this object's monitor.
>      * 
>      * 这个方法会使当前线程将自己放入对象的等待集合，然后放弃它持有的锁，线程将暂时不会被调度并处于休眠状态，
>      * 直到以下四种情况之一的发生：
>      * This method causes the current thread (call it <var>T</var>) to
>      * place itself in the wait set for this object and then to relinquish
>      * any and all synchronization claims on this object. Thread <var>T</var>
>      * becomes disabled for thread scheduling purposes and lies dormant
>      * until one of four things happens:
>      * 
>      * ①其他线程调用同一个对象的notify方法，而此线程正好是被随机选中要唤醒的线程
>      * Some other thread invokes the {@code notify} method for this
>      * object and thread <var>T</var> happens to be arbitrarily chosen as
>      * the thread to be awakened.
>      * 
>      * ②其他线程调用了同一个对象的notifyAll方法
>      * Some other thread invokes the {@code notifyAll} method for this
>      * object.
>      * 
>      * ③其他线程中断了该线程
>      * Some other thread {@linkplain Thread#interrupt() interrupts}
>      * thread <var>T</var>.
>      * 
>      * ④wait(long waitTime)参数中的等待时间过去了，如果等待时间为0则会一直等待唤醒
>      * The specified amount of real time has elapsed, more or less.  If
>      * {@code timeout} is zero, however, then real time is not taken into
>      * consideration and the thread simply waits until notified.
>      * 
>      *  线程在被唤醒后，将会从该对象的等待集中删除，并重新启动线程调度，
>      * 在与其他线程竞争到锁后，wait方法才返回。对象和线程的状态将会与调用wait时完全相同
>      * The thread <var>T</var> is then removed from the wait set for this
>      * object and re-enabled for thread scheduling. It then competes in the
>      * usual manner with other threads for the right to synchronize on the
>      * object; once it has gained control of the object, all its
>      * synchronization claims on the object are restored to the status quo
>      * ante - that is, to the situation as of the time that the {@code wait}
>      * method was invoked. Thread <var>T</var> then returns from the
>      * invocation of the {@code wait} method. Thus, on return from the
>      * {@code wait} method, the synchronization state of the object and of
>      * thread {@code T} is exactly as it was when the {@code wait} method
>      * was invoked.
>      * 
>      * 此方法只能由该对象监视器的所有者线程调用，查看notify方法，了解如何成为监视器所有者。
>      * This method should only be called by a thread that is the owner
>      * of this object's monitor. See the {@code notify} method for a
>      * description of the ways in which a thread can become the owner of
>      * a monitor.
> ```

# notify、notifyAll
## 参考注释
> notify()
>
> ```java
> /**
>      * 如果有任何线程在等待此对象监视器（锁），则随机选择其中一个线程唤醒。
>      * 线程调用wait方法来等待对象的监视器（锁）
>      * Wakes up a single thread that is waiting on this object's
>      * monitor. If any threads are waiting on this object, one of them
>      * is chosen to be awakened. The choice is arbitrary and occurs at
>      * the discretion of the implementation. A thread waits on an object's
>      * monitor by calling one of the {@code wait} methods.
>      *
>      * 被唤醒的对象并不会立马开始执行，必须等到当前线程释放该对象上的锁，然后被
>      * 唤醒的线程将会公平的与其他线程进行竞争该锁。
>      * The awakened thread will not be able to proceed until the current
>      * thread relinquishes the lock on this object. The awakened thread will
>      * compete in the usual manner with any other threads that might be
>      * actively competing to synchronize on this object; for example, the
>      * awakened thread enjoys no reliable privilege or disadvantage in being
>      * the next thread to lock this object.
>      * 
>      * notify方法必须是持有该对象的监视器的线程才能够调用，以下有三种方法可以成为监视器的拥有者
>      *  <p>
>      * This method should only be called by a thread that is the owner
>      * of this object's monitor. A thread becomes the owner of the
>      * object's monitor in one of three ways:
>      * <ul>
>      * 执行该对象的同步方法
>      * <li>By executing a synchronized instance method of that object.
>      * 执行在该对象上同步的同步代码块
>      * <li>By executing the body of a {@code synchronized} statement
>      *     that synchronizes on the object.
>      * 执行类对象的静态方法
>      * <li>For objects of type {@code Class,} by executing a
>      *     synchronized static method of that class.
>      * </ul>
>      * <p>
>      * 在同一时间只能有一个线程拥有对象的监视器（锁）
>      * Only one thread at a time can own an object's monitor.
>      * 
> ```

> notifyAll()与notify()大致相同，这里只列出不同的
>
> ```java
> /**
> 	* 唤醒此对象监视器上的所有线程，对象通过wait方法来等待对象的监视器
>      * Wakes up all threads that are waiting on this object's monitor. A
>      * thread waits on an object's monitor by calling one of the
>      * {@code wait} methods.
>      */ 
> ```

# 实例
## wait和notify

> 这里使用一个经典的例子加深对wait的理解
>
> ```java
> // WaitTest.java的源码
> class ThreadA extends Thread{
> 
>     public ThreadA(String name) {
>         super(name);
>     }
> 
>     public void run() {
>         synchronized (this) {
>             System.out.println(Thread.currentThread().getName()+" call notify()");
>             // 唤醒当前的wait线程
>             notify();
>         }
>     }
> }
> 
> public class WaitTest {
> 
>     public static void main(String[] args) {
> 
>         ThreadA t1 = new ThreadA("t1");
> 
>         synchronized(t1) {
>             try {
>                 // 启动“线程t1”
>                 System.out.println(Thread.currentThread().getName()+" start t1");
>                 t1.start();
> 
>                 // 主线程等待t1通过notify()唤醒。
>                 System.out.println(Thread.currentThread().getName()+" wait()");
>                 t1.wait();
> 
>                 System.out.println(Thread.currentThread().getName()+" continue");
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }
>     }
> }
> ```
>
> 运行结果：
>
> ```
> main start t1
> main wait()
> t1 call notify()
> main continue
> ```
>
> 这里可能会有一个疑问，明明执行的是t1.wait()为什么主线程被暂停了呢，jdk源码注释中有一句话：
>
> ```
> Causes the current thread to wait until another thread invokes the notify() method or the notifyAll() method for this object. 
> ```
>
> 直译的意思是：导致`当前线程`等待，直到另一个线程为该对象调用notify()方法或notifyAll()方法。当前线程指的是当前CPU正在执行的线程，在上述例子中main线程是当前正在执行线程，而t1正好又是同步对象，所以在main线程中调用t1.wait()实际是使main线程进入了等待。

## wait(long time)和notify

> 举个栗子：
>
> ```java
> public class Demo implements Runnable {
>     private static final Object obj = new Object();
> 
>     @Override
>     public void run() {
>         synchronized (obj) {
>             try {
>                 System.out.println(Thread.currentThread().getName()+" start");
>                 obj.wait(1000);
>                 System.out.println(Thread.currentThread().getName()+" continue");
>                 System.out.println(Thread.currentThread().getName()+" end");
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }
>     }
> 
>     public static void main(String[] args) throws Exception {
>         Demo demo = new Demo();
>         Thread t1 = new Thread(demo);
>         t1.start();
> 		//这里睡100毫秒是为了让出cpu执行时间给t1
>         Thread.sleep(100);
>         synchronized(obj) {
>             System.out.println(Thread.currentThread().getName()+" start");
>             Thread.sleep(2000);
>             System.out.println(Thread.currentThread().getName()+" end");
>         }
>     }
> }
> ```
>
> 执行结果“
>
> ```
> Thread-0 start
> main start
> main end
> Thread-0 continue
> Thread-0 end
> ```
>
> 详细执行的流程如下：
>
> `Main线程进入运行状态`  --->  `创建线程对象t1(进入就绪状态)`  --->  `启动t1(进入就绪状态)`  --->  `Main线程休眠100ms(进入阻塞状态)触发上下文切换`  --->  `t1线程获得锁进入运行状态，打印Thread-0 start` --->  `t1线程执行wait(1000)等待1s，并释放锁进入阻塞状态`  --->   `main线程获得cpu执行时间(运行状态)，获得锁。打印了main start`  --->  `main线程睡1s进入阻塞状态，不释放锁(此时t1的等待时间早已结束，但是锁被main线程占用，wait方法还没返回)` --->  `main线程睡醒后打印main end，释放锁`  --->   `t1获得锁，进入运行状态，打印了Thread-0 continue、Thread-0 end`  ---> `t1运行结束进入死亡状态，main线程进入死亡状态 `
>
> 现在对代码做个简单的修改，多启动两个线程。
>
> ```java
> public class Demo4 implements Runnable {
>     private static final Object obj = new Object();
> 
>     @Override
>     public void run() {
>         synchronized (obj) {
>             try {
>                 System.out.println(Thread.currentThread().getName()+" start");
>                 obj.wait();
>                 System.out.println(Thread.currentThread().getName()+" continue");
>                 System.out.println(Thread.currentThread().getName()+" end");
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }
>     }
> 
>     public static void main(String[] args) throws Exception {
>         Demo4 demo4 = new Demo4();
>         Thread t1 = new Thread(demo4);
>         Thread t2 = new Thread(demo4);
>         Thread t3 = new Thread(demo4);
>         t1.start();
>         t2.start();
>         t3.start();
> 
>         Thread.sleep(100);
>         synchronized(obj) {
>             System.out.println(Thread.currentThread().getName()+" start");
>             System.out.println(Thread.currentThread().getName()+" notify");
>             obj.notify();
>             Thread.sleep(2000);
>             System.out.println(Thread.currentThread().getName()+" end");
>         }
>     }
> }
> ```
>
> 执行结果：
>
> ```
> Thread-0 start
> Thread-1 start
> Thread-2 start
> main start
> main end
> Thread-0 continue
> Thread-0 end
> ```
>
> 在上述代码中等待的时间没有设置，也就是默认值0，它会一直等待到被唤醒为止，在main方法中调用了notify唤醒，多次的执行中唤醒的线程不同，回顾源码中的注释可以知道，notify方法会随机唤醒等待队列中的一个线程，另外此程序需要手动结束，因为t1，t2，t3都是main线程的子线程，其中唤醒了一个，剩下两个仍然在等待并没有结束，主线程必须等待所有子线程死亡才会死亡。



## wait和 notifyAll

> ```java
> public class Demo4 implements Runnable {
>     private static final Object obj = new Object();
> 
>     @Override
>     public void run() {
>         synchronized (obj) {
>             try {
>                 System.out.println(Thread.currentThread().getName()+" start");
>                 obj.wait();
>                 System.out.println(Thread.currentThread().getName()+" continue");
>                 System.out.println(Thread.currentThread().getName()+" end");
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }
>         }
>     }
> 
>     public static void main(String[] args) throws Exception {
>         Demo4 demo4 = new Demo4();
>         Thread t1 = new Thread(demo4);
>         Thread t2 = new Thread(demo4);
>         Thread t3 = new Thread(demo4);
>         t1.start();
>         t2.start();
>         t3.start();
>         Thread.sleep(100);
>         synchronized(obj) {
>             System.out.println(Thread.currentThread().getName()+" start");
>             System.out.println(Thread.currentThread().getName()+" notify");
>             obj.notifyAll();
>             System.out.println(Thread.currentThread().getName()+" end");
>         }
>     }
> }
> ```
>
> 运行结果：
>
> ```
> Thread-0 start
> Thread-2 start
> Thread-1 start
> main start
> main notify
> main end
> Thread-1 continue
> Thread-1 end
> Thread-2 continue
> Thread-2 end
> Thread-0 continue
> Thread-0 end
> ```
>
> 过程比较简单，main线程调用了notifyAll()后释放锁，三个被唤醒的线程争夺锁，直到三个线程执行完main线程结束。

## notify和notifyAll的区别
> 先科普两个概念，锁池和等待池
>
> - 锁池：线程A获得锁执行某synchronized修饰的方法M，其他线程想要再执行M需要获取锁，但是锁目前被A持有，所以其他线程会先进入锁池中等待A释放锁以后进行公平竞争，竞争到的线程则离开锁池执行代码，没竞争到的线程留在锁池等待下一次竞争。
> - 等待池：线程A在持有锁时调用wait()释放锁并进入等待池中。
>
> Object.notify()会从该对象的等待池中随机唤醒一个线程(例如A)，A会从等待池中出来进入到该对象的锁池，等待该对象锁被释放时进行竞争。
> Object.notifyAll()会将该对象的等待池中的所有线程全部唤醒，并从等待池切换到锁池中，等待对象锁被释放时参与竞争

## wait要放在while中
> 结论：`为了确保目标动作只有在保护条件成立的情况下才能够执行`
> 举个例子：
> 要求，必须在condition = false时才能进行打印
>
> ```java
> public class Demo5 implements Runnable {
>     private static final Object obj = new Object();
>     private static boolean condition = true;
>     @Override
>     public void run() {
>         synchronized (obj) {
>             try {
>             	//A
>                 if (condition) {
>                 	//B
>                     obj.wait();
>                 }
>                 System.out.println(Thread.currentThread().getName() + " condition" + condition);
>             } catch (Exception e) {
>                 e.printStackTrace();
>             }
>         }
>     }
> 
>     public static void main(String[] args) throws Exception {
>         Demo5 demo5 = new Demo5();
>         Thread t1 = new Thread(demo5);
>         t1.start();
>         Thread.sleep(100);
> 
>         synchronized (obj) {
>             obj.notify();
>         }
>     }
> }
> ```
>
> 运行结果：`Thread-0 condition true`，显然结果和我们所期望的不同，condition为true但是却打印了，如果将A处的if换成while即可正确执行。
> 原因很简单，main函数唤醒线程t1后，t1回到B处，此时已经进行过if判断了，所以就会继续执行，如果使用的是while则会再次判断condition保护条件是否正确。

## 性能问题
> 1. `过早唤醒`：等待线程在其所需的保护条件并未成立的情况下被唤醒的现象称为过早唤醒，导致了资源的浪费，在jdk1.5中引入的java.util.concurrent.locks.Condition接口可以很好的解决，后续博客会介绍。
> 2. `信号丢失`
>    首先科普一下信号丢失问题：下列代码中，当线程T2执行完while判断后，cpu执行切换到T1进行了唤醒(此时T2还未进入等待)，T1执行结束，接着cpu切换到T2继续执行，T2进入了等待状态，于是T2就会无限处于等待状态而没有线程唤醒它，这个情况就相当于等待线程错过了一个本来要发送给它的信号，因此称为信号丢失。解决方法很简单，`必要的时候使用notify通知，或者将对保护条件的判断放入临界区内(同步代码块中)`
>
> ```java
>     T1：  
>     synchronized(sharedMonitor){  
>     	
>         sharedMonitor.notify();  
>     }  
>       
>     T2:  
>     while(someCondition){  
>     //Point1  
>        synchronized(sharedMonitor){  
>           sharedMonitor.wait();  
>        }  
>     }  
> ```
>
> 3. `欺骗性唤醒`：在jdk源码的注释中有提及
>
> A thread can also wake up without being notified, interrupted, or  timing out, a so-called <i>spurious wakeup</i>.  While this will rarely  occur in practice, applications must guard against it by testing for the condition that should have caused the thread to be awakened, and  continuing to wait if the condition is not satisfied.  
>
> 大致意思是，线程可能在不被通知、中断、超时的情况下唤醒，即所谓的伪唤醒，这种情况很少发生，应用程序必须通过判断保护条件来防止这个情况，如果条件不满足则继续等待。
> `通俗的说就是使用while循环判断保护条件，即便出现了欺骗性唤醒也会再次进入等待。`
>
> 4. `上下文切换问题` : wait和notify的使用会导致较多的上下文切换
>    科普上下文切换：
>
> CPU通过时间片分配算法来循环执行任务，当前任务执行一个时间片后会切换到下一个任务。但是，在切换前会保存上一个任务的状态，以便下次切换回这个任务时，可以再次加载这个任务的状态，从任务保存到再加载的过程就是一次上下文切换。
>
> ①wait和notify会多次的申请和释放锁，而锁的申请和释放可能导致上下文切换
> ②线程进入等待和唤醒这两个过程本身就会导致上下文切换
> ③被唤醒的线程进入锁池中和其他线程进行竞争时也可能导致上下文切换。                                                                                                                                                                                              