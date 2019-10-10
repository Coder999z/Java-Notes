
* [JUC常用锁入门](#juc%E5%B8%B8%E7%94%A8%E9%94%81%E5%85%A5%E9%97%A8)
  * [ReentrantLock](#reentrantlock)
    * [Condition](#condition)
  * [ReentrantReadWriteLock](#reentrantreadwritelock)
  * [CountDownLatch](#countdownlatch)
  * [CyclicBarrier](#cyclicbarrier)
  * [Semaphore](#semaphore)

# JUC常用锁入门

> 本篇文章主要介绍JUC包中几个常用锁的使用方式，所有Demo代码均已上传，源码分析将会在下一篇文章。

## ReentrantLock

> **概述**
>
> ReentrantLock是jdk1.5开始引入的`排他锁`，它的作用与内部锁（synchronized）相同，它提供了一些内部锁不具备的特性，使用起来更加灵活。类java.util.concurrent.locks.`ReentrantLock`是Lock接口的默认实现类。

> **API** 
>
> ```java
> // 创建一个 ReentrantLock ，默认是“非公平锁”。
> ReentrantLock()
> // 创建策略是fair的 ReentrantLock。fair为true表示是公平锁，fair为false表示是非公平锁。
> ReentrantLock(boolean fair)
> 
> // 查询当前线程保持此锁的次数。
> int getHoldCount()
> // 返回目前拥有此锁的线程，如果此锁不被任何线程拥有，则返回 null。
> protected Thread getOwner()
> // 返回一个 collection，它包含可能正等待获取此锁的线程。
> protected Collection<Thread> getQueuedThreads()
> // 返回正等待获取此锁的线程估计数。
> int getQueueLength()
> // 返回一个 collection，它包含可能正在等待与此锁相关给定条件的那些线程。
> protected Collection<Thread> getWaitingThreads(Condition condition)
> // 返回等待与此锁相关的给定条件的线程估计数。
> int getWaitQueueLength(Condition condition)
> // 查询给定线程是否正在等待获取此锁。
> boolean hasQueuedThread(Thread thread)
> // 查询是否有些线程正在等待获取此锁。
> boolean hasQueuedThreads()
> // 查询是否有些线程正在等待与此锁有关的给定条件。
> boolean hasWaiters(Condition condition)
> // 如果是“公平锁”返回true，否则返回false。
> boolean isFair()
> // 查询当前线程是否保持此锁。
> boolean isHeldByCurrentThread()
> // 查询此锁是否由任意线程保持。
> boolean isLocked()
> // 获取锁。
> void lock()
> // 如果当前线程未被中断，则获取锁。
> void lockInterruptibly()
> // 返回用来与此 Lock 实例一起使用的 Condition 实例。
> Condition newCondition()
> // 仅在调用时锁未被另一个线程保持的情况下，才获取该锁。
> boolean tryLock()
> // 如果锁在给定等待时间内没有被另一个线程保持，且当前线程未被中断，则获取该锁。
> boolean tryLock(long timeout, TimeUnit unit)
> // 试图释放此锁。
> void unlock()
> ```
>
> 

> **Demo**
>
> ```java
> public class ReentrantLockDemo {
> 
>     static ReentrantLock lock = new ReentrantLock();
>     static int tickets = 100;
> 
>     static Runnable cell = () ->{
>         try {
>             //获得锁
>             lock.lock();
>             while (tickets > 0) {
>                 TimeUnit.MILLISECONDS.sleep(10);
>                 tickets--;
>                 System.out.println(tickets);
>             }
>         } catch (Exception e) {
>             e.printStackTrace();
>         } finally {
>             //释放锁
>             lock.unlock();
>         }
>     };
> 
>     public static void main(String[] args) {
>         for (int i = 0; i < 10; i++) {
>             new Thread(cell).start();
>         }
>     }
> }
> ```
>
> 源码地址：https://github.com/Coder999z/Java-Notes/blob/master/sourcecode/juc/ReentrantLockDemo.java
>
> 如果注释掉获得锁和释放锁的代码将会出现余票量小于0的线程不安全的问题。
>
>  
>
> 显式锁的使用步骤大致为：
> - 创建Lock接口的实现类，一般默认使用的是ReentrantLock，从字面上可以看出它是一个 `可重入锁`。
> - 在访问需要同步的代码块之前需要先申请响应的显式锁，一般调用的是lock()方法。
> - 访问结束同步代码块后，`必须要手动调用unlock() 释放锁`，通常锁的释放放在finally中执行，可以避免锁的泄露。

### Condition

> **概述**
>
> 在Synchronize中wait()和notify()/notifAll()，它们只能选择随机唤醒或者全部唤醒等待锁的线程，如果遇到多个保护条件时容易导致过早唤醒问题，而Condition接口很好的解决了这个问题，它十分灵活，可以实现多路通知和选择性通知。文字有点抽象，看例子就明白了。

> **API**
>
> ```java
> // 造成当前线程在接到信号或被中断之前一直处于等待状态。
> void await()
> // 造成当前线程在接到信号、被中断或到达指定等待时间之前一直处于等待状态。
> boolean await(long time, TimeUnit unit)
> // 造成当前线程在接到信号、被中断或到达指定等待时间之前一直处于等待状态。
> long awaitNanos(long nanosTimeout)
> // 造成当前线程在接到信号之前一直处于等待状态。
> void awaitUninterruptibly()
> // 造成当前线程在接到信号、被中断或到达指定最后期限之前一直处于等待状态。
> boolean awaitUntil(Date deadline)
> // 唤醒一个等待线程。
> void signal()
> // 唤醒所有等待线程。
> void signalAll()
> ```
>
> 

> **Demo**
>
> 场景：肯德基的货架中可以摆放各种食品，假设每个柜台只能卖一种食品，后厨可以生产各种食品放在货架上，当一种食品缺货时对应柜台则暂停出售，后厨补充了对应食品时会喊前台人员继续出售。
>
> - 使用synchronized的写法：
>
> ```java
> public class SynchronizeDemo {
>     static final Object obj = new Object();
>     static int hamburger = 5;
>     static int chips = 0;
>     //厨师
>     static Runnable chef = () -> {
>         try {
>             while (true) {
>                 if (hamburger == 0) {
>                     synchronized (obj) {
>                         hamburger++;
>                         obj.notifyAll();
>                     }
>                 }
>                 TimeUnit.SECONDS.sleep(1);
>             }
>         } catch (Exception e) {
>             e.printStackTrace();
>         }
>     };
>     //卖汉堡柜台
>     static Runnable hamburgerCounter = () -> {
>         try {
>             while (true) {
>                 while (hamburger > 0) {
>                     System.out.println("HamburgerCounter卖出了第" + hamburger + "个汉堡");
>                     hamburger--;
>                     TimeUnit.MILLISECONDS.sleep(500);
>                 }
>                 synchronized (obj) {
>                     System.out.println("汉堡已卖完，进入等待");
>                     obj.wait();
>                     System.out.println("收到通知，继续卖汉堡");
>                 }
> 
>             }
>         } catch (InterruptedException e) {
>             e.printStackTrace();
>         }
>     };
>     //卖薯条柜台
>     static Runnable chipsCounter = () -> {
>         try {
>             while (true) {
>                 while (chips == 0) {
>                     synchronized (obj) {
>                         System.out.println("薯条已卖完，进入等待");
>                         obj.wait();
>                         System.out.println("收到通知，继续卖薯条");
>                     }
>                 }
>                 chips--;
>                 System.out.println("ChipsCounter卖出了第" + chips + "包薯条");
>                 TimeUnit.SECONDS.sleep(1);
>             }
>         } catch (InterruptedException e) {
>             e.printStackTrace();
>         }
>     };
> 
>     public static void main(String[] args) {
>         new Thread(chef).start();
>         new Thread(hamburgerCounter).start();
>         new Thread(chipsCounter).start();
>     }
> }
> 
> ```
>
> 源码地址：https://github.com/Coder999z/Java-Notes/blob/master/sourcecode/juc/SynchronizeDemo.java
>
> 运行结果：
>
> ```
> HamburgerCounter卖出了第5个汉堡
> HamburgerCounter卖出了第4个汉堡
> HamburgerCounter卖出了第3个汉堡
> HamburgerCounter卖出了第2个汉堡
> HamburgerCounter卖出了第1个汉堡
> 薯条已卖完，进入等待
> 汉堡已卖完，进入等待
> 收到通知，继续卖汉堡
> 收到通知，继续卖薯条
> 薯条已卖完，进入等待
> HamburgerCounter卖出了第1个汉堡
> 汉堡已卖完，进入等待
> 收到通知，继续卖汉堡
> HamburgerCounter卖出了第1个汉堡
> 收到通知，继续卖薯条
> 薯条已卖完，进入等待
> 汉堡已卖完，进入等待
> ```
>
> 由运行结果可知，在chef中生产汉堡后，唤醒等待的柜台notifyAll时，唤醒了HamburgerCounter和ChipsCounter ，HamburgerCounter继续卖汉堡，ChipsCounter线程检测到Chips为0于是再次进入等待。这样做虽然完成了需求，但是唤醒了无需唤醒的线程，这样会触发多余的上下文切换影响性能。

> - 使用Condition的写法：
>
> ```java
> public class ConditionDemo {
>     static Lock lock = new ReentrantLock();
>     static Condition hamburgerCondition = lock.newCondition();
>     static Condition chipsCondition = lock.newCondition();
> 
>     static int hamburger = 3;
>     static int chips = 0;
> 
>     static Runnable chef = ()->{
>         try {
>             while (true) {
>                 if (hamburger == 0) {
>                     try {
>                         lock.lock();
>                         hamburger++;
>                         hamburgerCondition.signalAll();
>                     } catch (Exception e) {
>                         e.printStackTrace();
>                     } finally {
>                         lock.unlock();
>                     }
>                 }
>                 TimeUnit.SECONDS.sleep(1);
>             }
>         } catch (Exception e) {
>             e.printStackTrace();
>         }
>     };
> 
>     static Runnable hamburgerCounter = ()->{
>         try {
>             while (true) {
>                 while (hamburger > 0) {
>                     System.out.println("HamburgerCounter卖出了第" + hamburger + "个汉堡");
>                     hamburger--;
>                     TimeUnit.MILLISECONDS.sleep(500);
>                 }
>                 try {
>                     lock.lock();
>                     System.out.println( "汉堡已卖完，进入等待");
>                     hamburgerCondition.await();
>                     System.out.println("收到通知，继续卖汉堡");
>                 } catch (InterruptedException e) {
>                     e.printStackTrace();
>                 } finally {
>                     lock.unlock();
>                 }
>             }
>         } catch (InterruptedException e) {
>             e.printStackTrace();
>         }
>     };
> 
>     static Runnable chipsCounter = ()-> {
>         try {
>             while (true) {
>                 while (chips > 0) {
>                     System.out.println("ChipsCounter卖出了第" + chips + "份薯条");
>                     chips--;
>                     TimeUnit.MILLISECONDS.sleep(500);
>                 }
>                 try {
>                     lock.lock();
>                     System.out.println("薯条已卖完，进入等待");
>                     chipsCondition.await();
>                     System.out.println("收到通知，继续卖薯条");
>                 } catch (InterruptedException e) {
>                     e.printStackTrace();
>                 } finally {
>                     lock.unlock();
>                 }
>             }
>         } catch (InterruptedException e) {
>             e.printStackTrace();
>         }
>     };
> 
>     public static void main(String[] args) {
>         new Thread(chef).start();
>         new Thread(hamburgerCounter).start();
>         new Thread(chipsCounter).start();
>     }
> }
> ```
>
> 源码地址：https://github.com/Coder999z/Java-Notes/blob/master/sourcecode/juc/ConditionDemo.java
>
> 运行结果：
>
> ```
> HamburgerCounter卖出了第3个汉堡
> 薯条已卖完，进入等待
> HamburgerCounter卖出了第2个汉堡
> HamburgerCounter卖出了第1个汉堡
> 汉堡已卖完，进入等待
> 收到通知，继续卖汉堡
> HamburgerCounter卖出了第1个汉堡
> 汉堡已卖完，进入等待
> 收到通知，继续卖汉堡
> HamburgerCounter卖出了第1个汉堡
> ```
> 在这个案例中，使用condition，生产了汉堡就精准的唤醒卖汉堡的线程，不会将卖薯条的唤醒，避免了过早唤醒的问题。

## ReentrantReadWriteLock

> **概述**
>
> ReentrantReadWriteLock是读写锁，它内部维护了一个读锁和一个写锁，并且可以指定公平性。
>
> - 读锁：它是共享锁，能够被多个线程同时持有。
>
> - 写锁：它是独占锁，一次只能被一个线程持有。
>
> - 读锁和写锁不能同时存在，即读取时不得写入，写入时不得读取。

> **API**
>
> ```java
> // 创建一个新的 ReentrantReadWriteLock，默认是采用“非公平策略”。
> ReentrantReadWriteLock()
> // 创建一个新的 ReentrantReadWriteLock，fair是“公平策略”。fair为true，意味着公平策略；否则，意味着非公平策略。
> ReentrantReadWriteLock(boolean fair)
> 
> // 返回当前拥有写入锁的线程，如果没有这样的线程，则返回 null。
> protected Thread getOwner()
> // 返回一个 collection，它包含可能正在等待获取读取锁的线程。
> protected Collection<Thread> getQueuedReaderThreads()
> // 返回一个 collection，它包含可能正在等待获取读取或写入锁的线程。
> protected Collection<Thread> getQueuedThreads()
> // 返回一个 collection，它包含可能正在等待获取写入锁的线程。
> protected Collection<Thread> getQueuedWriterThreads()
> // 返回等待获取读取或写入锁的线程估计数目。
> int getQueueLength()
> // 查询当前线程在此锁上保持的重入读取锁数量。
> int getReadHoldCount()
> // 查询为此锁保持的读取锁数量。
> int getReadLockCount()
> // 返回一个 collection，它包含可能正在等待与写入锁相关的给定条件的那些线程。
> protected Collection<Thread> getWaitingThreads(Condition condition)
> // 返回正等待与写入锁相关的给定条件的线程估计数目。
> int getWaitQueueLength(Condition condition)
> // 查询当前线程在此锁上保持的重入写入锁数量。
> int getWriteHoldCount()
> // 查询是否给定线程正在等待获取读取或写入锁。
> boolean hasQueuedThread(Thread thread)
> // 查询是否所有的线程正在等待获取读取或写入锁。
> boolean hasQueuedThreads()
> // 查询是否有些线程正在等待与写入锁有关的给定条件。
> boolean hasWaiters(Condition condition)
> // 如果此锁将公平性设置为 ture，则返回 true。
> boolean isFair()
> // 查询是否某个线程保持了写入锁。
> boolean isWriteLocked()
> // 查询当前线程是否保持了写入锁。
> boolean isWriteLockedByCurrentThread()
> // 返回用于读取操作的锁。
> ReentrantReadWriteLock.ReadLock readLock()
> // 返回用于写入操作的锁。
> ReentrantReadWriteLock.WriteLock writeLock()
> ```
>
> 

> **Demo**
>
> ```java
> public class ReadWriteDemo {
>     static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
> 
>     static Runnable read = () -> {
>         ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
>         try {
>             System.out.println("Read Thread-" + Thread.currentThread().getId() + " start");
>             readLock.lock();
>             System.out.println("Read Thread-" + Thread.currentThread().getId() + " running");
>             TimeUnit.SECONDS.sleep(1);
>             System.out.println("Read Thread-" + Thread.currentThread().getId() + " stop");
>         } catch (InterruptedException e) {
>             e.printStackTrace();
>         } finally {
>             readLock.unlock();
>         }
>     };
> 
>     static Runnable write = () -> {
>         ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
>         try {
>             System.out.println("Write Thread-" + Thread.currentThread().getId() + " start");
>             writeLock.lock();
>             System.out.println("Write Thread-" + Thread.currentThread().getId() + " running");
>             TimeUnit.SECONDS.sleep(1);
>             System.out.println("Write Thread-" + Thread.currentThread().getId() + " stop");
>         } catch (InterruptedException e) {
>             e.printStackTrace();
>         } finally {
>             writeLock.unlock();
>         }
>     };
> 
>     public static void main(String[] args) throws InterruptedException {
>         new Thread(read).start();
>         new Thread(read).start();
>         new Thread(write).start();
>         new Thread(write).start();
>         new Thread(read).start();
>     }
> }
> ```
>
> 源码地址：https://github.com/Coder999z/Java-Notes/blob/master/sourcecode/juc/ReadWriteDemo.java
>
> 执行结果：
>
> ```
> Read Thread-11 start
> Read Thread-12 start
> Write Thread-13 start
> Read Thread-12 running
> Read Thread-11 running
> Write Thread-14 start
> Read Thread-15 start
> Read Thread-12 stop
> Read Thread-11 stop
> Write Thread-13 running
> Write Thread-13 stop
> Write Thread-14 running
> Write Thread-14 stop
> Read Thread-15 running
> Read Thread-15 stop
> ```
>
> 从结果可以看出Thread-11和Thread-12和Thread-13按顺序启动了，并且11和12同时获得到了写锁，执行完毕后又Thread-13获得了写锁，14和15只能等待13执行完毕后才能获取锁。

## CountDownLatch

> **概述**
>
> CountDownLatch是一个同步辅助类，在内置的计数器清零之前，它允许一个或多个线程一直等待。
>
> CountDownLatch在各大框架中都有使用，在日常开发中，最常用的就是在系统启动自检时需要多个线程进行初始化，在所有线程完成初始化后才能开始执行主线程，这样的功能由CountDownLatch实现就十分合适。

> **API**
>
> ```java
> CountDownLatch(int count)
> 构造一个用给定计数初始化的 CountDownLatch。
> 
> // 使当前线程在锁存器倒计数至零之前一直等待，除非线程被中断。
> void await()
> // 使当前线程在锁存器倒计数至零之前一直等待，除非线程被中断或超出了指定的等待时间。
> boolean await(long timeout, TimeUnit unit)
> // 递减锁存器的计数，如果计数到达零，则释放所有等待的线程。
> void countDown()
> // 返回当前计数。
> long getCount()
> // 返回标识此锁存器及其状态的字符串。
> String toString()
> ```

> **Demo**
>
> ```java
> public class CountDownLatchDemo {
>     static CountDownLatch latch = new CountDownLatch(3);
> 
>     static Runnable init = ()->{
>         latch.countDown();
>         System.out.println(Thread.currentThread().getName() + " check off");
>     };
> 
>     public static void main(String[] args) throws InterruptedException {
>         System.out.println("Start init thread");
>         for (int i = 0; i < 3; i++) {
>             System.out.println("Start " + i + " success");
>             new Thread(init).start();
>         }
>         System.out.println("Waiting init");
>         latch.await();
>         System.out.println("All thread check off");
>         System.out.println("System start success");
> 
>     }
> }
> 
> ```
>
> 源码地址：https://github.com/Coder999z/Java-Notes/blob/master/sourcecode/juc/CountDownLatchDemo.java
>
> 执行结果：
>
> ```
> Start init thread
> Start 0 success
> Start 1 success
> Start 2 success
> Waiting init
> Thread-1 check off
> Thread-0 check off
> Thread-2 check off
> All thread check off
> System start success
> ```
>
> 在三个线程执行完毕CountDownLatch内部计数器变为0时main线程才从await()函数中返回继续执行。

## CyclicBarrier

> **概述**
>
> CyclicBarrier与CountdownLatch十分类似，它允许N个线程之间相互等待，在N个线程都调用await()使CyclicBarrier内部计数器达到一定值后将调用一个自定义的回调函数，并所有线程将继续执行。
>
> CyclicBarrier计数器可以被重置，因此它也被称为循环栅栏。

> **API**
>
> ```java
> CyclicBarrier(int parties)
> 创建一个新的 CyclicBarrier，它将在给定数量的参与者（线程）处于等待状态时启动，但它不会在启动 barrier 时执行预定义的操作。
> CyclicBarrier(int parties, Runnable barrierAction)
> 创建一个新的 CyclicBarrier，它将在给定数量的参与者（线程）处于等待状态时启动，并在启动 barrier 时执行给定的屏障操作，该操作由最后一个进入 barrier 的线程执行。
> 
> int await()
> 在所有参与者都已经在此 barrier 上调用 await 方法之前，将一直等待。
> int await(long timeout, TimeUnit unit)
> 在所有参与者都已经在此屏障上调用 await 方法之前将一直等待,或者超出了指定的等待时间。
> int getNumberWaiting()
> 返回当前在屏障处等待的参与者数目。
> int getParties()
> 返回要求启动此 barrier 的参与者数目。
> boolean isBroken()
> 查询此屏障是否处于损坏状态。
> void reset()
> 将屏障重置为其初始状态。
> ```

> **Demo**
>
> ```java
> public class CyclicBarrierDemo {
>     static CyclicBarrier cb = new CyclicBarrier(3,()-> System.out.println("CyclicBarrier call back function"));
> 
>     static Runnable runner = ()->{
>         try {
>             System.out.println(Thread.currentThread().getName() + " wait for CyclicBarrier");
>             cb.await();
>             System.out.println(Thread.currentThread().getName() + " continue");
>         } catch (Exception e) {
>             e.printStackTrace();
>         }
>     };
> 
>     public static void main(String[] args) throws InterruptedException {
>         for (int i = 0; i < 2; i++) {
>             new Thread(runner).start();
>         }
>         TimeUnit.SECONDS.sleep(1);
>         new Thread(runner).start();
>     }
> }
> ```
>
> 源码地址：https://github.com/Coder999z/Java-Notes/blob/master/sourcecode/juc/CyclicBarrierDemo.java
>
> 执行结果：
>
> ```
> Thread-0 wait for CyclicBarrier
> Thread-1 wait for CyclicBarrier
> Thread-2 wait for CyclicBarrier
> CyclicBarrier call back function
> Thread-2 continue
> Thread-0 continue
> Thread-1 continue
> ```
>
> 在三个线程调用await()后，达到CyclicBarrier初始化的计数器3后，启动了回调函数，最后三个线程继续执行。

> **CyclicBarrier和CountDownLatch的区别？**
>
> - CountDownLatch中各个工作线程执行完毕后就结束了，而CyclicBarrier的工作线程需要等到计数器打满后才会结束。
> - CountDownLatch的作用是允许1或N个线程等待计数器清零，而CyclicBarrier是让N个线程之间互相等待。
> - CountDownLatch的计数器无法重置，它是一次性的，而CyclicBarrier是可以重置的。

## Semaphore

> **概述**
>
> Semaphore信号量，本质是一个共享锁，Semaphore在创建时指定信号量集的数量，线程可以尝试获取指定数量的信号量，若剩余的信号量中有可用的，则线程尝试获取，否则线程必须等待到有足够信号量时为止。

> **API**
>
> ```java
> // 创建具有给定的许可数和非公平的公平设置的 Semaphore。
> Semaphore(int permits)
> // 创建具有给定的许可数和给定的公平设置的 Semaphore。
> Semaphore(int permits, boolean fair)
> 
> // 从此信号量获取一个许可，在提供一个许可前一直将线程阻塞，否则线程被中断。
> void acquire()
> // 从此信号量获取给定数目的许可，在提供这些许可前一直将线程阻塞，或者线程已被中断。
> void acquire(int permits)
> // 从此信号量中获取许可，在有可用的许可前将其阻塞。
> void acquireUninterruptibly()
> // 从此信号量获取给定数目的许可，在提供这些许可前一直将线程阻塞。
> void acquireUninterruptibly(int permits)
> // 返回此信号量中当前可用的许可数。
> int availablePermits()
> // 获取并返回立即可用的所有许可。
> int drainPermits()
> // 返回一个 collection，包含可能等待获取的线程。
> protected Collection<Thread> getQueuedThreads()
> // 返回正在等待获取的线程的估计数目。
> int getQueueLength()
> // 查询是否有线程正在等待获取。
> boolean hasQueuedThreads()
> // 如果此信号量的公平设置为 true，则返回 true。
> boolean isFair()
> // 根据指定的缩减量减小可用许可的数目。
> protected void reducePermits(int reduction)
> // 释放一个许可，将其返回给信号量。
> void release()
> // 释放给定数目的许可，将其返回到信号量。
> void release(int permits)
> // 返回标识此信号量的字符串，以及信号量的状态。
> String toString()
> // 仅在调用时此信号量存在一个可用许可，才从信号量获取许可。
> boolean tryAcquire()
> // 仅在调用时此信号量中有给定数目的许可时，才从此信号量中获取这些许可。
> boolean tryAcquire(int permits)
> // 如果在给定的等待时间内此信号量有可用的所有许可，并且当前线程未被中断，则从此信号量获取给定数目的许可。
> boolean tryAcquire(int permits, long timeout, TimeUnit unit)
> // 如果在给定的等待时间内，此信号量有可用的许可并且当前线程未被中断，则从此信号量获取一个许可。
> boolean tryAcquire(long timeout, TimeUnit unit)
> ```

> **Demo**
>
> ```java
> public class SemaphoreDemo {
>     static Semaphore semaphore = new Semaphore(10);
> 
>     public static void main(String[] args) {
>         new Worker(5).start();
>         new Worker(5).start();
>         new Worker(7).start();
>     }
> 
>     static class Worker extends Thread {
>         int count;
> 
>         public Worker(int count) {
>             this.count = count;
>         }
> 
>         @Override
>         public void run() {
>             try {
>                 semaphore.acquire(count);
>                 System.out.println(Thread.currentThread().getName() + " acquire" + count);
>                 TimeUnit.SECONDS.sleep(2);
>             } catch (InterruptedException e) {
>                 e.printStackTrace();
>             }finally {
>                 semaphore.release(count);
>                 System.out.println(Thread.currentThread().getName() + " release" + count);
>             }
>         }
>     }
> }
> ```
>
> 源码地址：https://github.com/Coder999z/Java-Notes/blob/master/sourcecode/juc/SemaphoreDemo.java
>
> 运行结果：
>
> ```
> Thread-0 acquire5
> Thread-1 acquire5
> Thread-0 release5
> Thread-1 release5
> Thread-2 acquire7
> Thread-2 release7
> ```
>
> Demo中前两个Work需要获取5个信号量，Semaphore中余量符合，因此Thread-0和1并发执行，而Thread-2需要7个信号量，因此需要等待Thread-0和1release后才足够获取，因此Thread-2在最后等待了两秒后才执行。

## 参考
> https://www.cnblogs.com/skywang12345/p/java_threads_category.html