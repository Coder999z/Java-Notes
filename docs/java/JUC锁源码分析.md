
* [AQS源码分析](#aqs%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90)
  * [概念介绍](#%E6%A6%82%E5%BF%B5%E4%BB%8B%E7%BB%8D)
  * [AQS源码分析](#aqs%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90-1)
    * [类结构](#%E7%B1%BB%E7%BB%93%E6%9E%84)
    * [结合ReenTrantLock](#%E7%BB%93%E5%90%88reentrantlock)
    * [结合CountDownLatch](#%E7%BB%93%E5%90%88countdownlatch)
    * [总结](#%E6%80%BB%E7%BB%93)
  * [参考](#%E5%8F%82%E8%80%83)

# AQS源码分析

> 阅读这篇文章需要读者在对JUC包中的各个工具类有一定的了解的基础上，不适合新手入门。建议先阅读：https://github.com/Coder999z/Java-Notes/blob/master/docs/java/JUC锁入门.md

## 概念介绍

> **AQS（AbstractQueuedSynchronizer）** AQS是JUC包中用于实现锁机制的一个抽象类，JUC包中的ReenTrantLock，CountDownLatch，Semaphore等都是通过AQS实现的。

> **CLH队列** CLH仅仅是三个人名首字母的缩写，在AQS中包含一个CLH队列，此队列是一种基于链表的公平自旋锁，队列确保无饥饿性和保证先来先服务的公平性，申请线程仅仅在本地变量上自旋，不断轮询前驱的状态，假设发现前驱线程释放了锁则结束自旋。

> **CAS函数（Compare and swap）** 它是提供了一种无锁的原子操作函数，通过比较并转换的方式。

## AQS源码分析

### 类结构

> ![1570674338614](https://github.com/Coder999z/Java-Notes/blob/master/img/1/1570674338614.png)
>
> **Node：**
>
> - 表示CLH队列中的节点对象
> - CANCELLED 当前线程已经取消
>   SIGNAL 当前线程的后继线程需要被唤醒
>   CONDITION 线程处于Condition的await中休眠
>   PROPAGATE 共享锁状态
>   0 不处于任何状态，通常是新加入的节点
> - waitStatus表示上述状态其一
> - thread表示此Node对应的线程
> - prev前驱节点，next后继节点
>
> **AbstractQueuedSynchronizer：**
>
> - head和tail表示CLH队列中的头尾节点
> - state状态位，在不同锁的使用中用途不同，下文会介绍，记住有这么个变量就行。



### 结合ReenTrantLock

> **概述**
>
> 上篇介绍过了，ReenTrantLock是一个可重入的独占锁，它有公平和非公平两种模式。

> **类结构**
>
> ![1570673964885](https://github.com/Coder999z/Java-Notes/blob/master/img/1/1570673964885.png)
>
> - FairSync和NofairSync分别是公平锁和非公平锁的实现
> - ReentrantLock中的成员变量sync指向具体的锁实现类

> 从示例代码入手：
>
> ```java
> ReentrantLock lock = new ReentrantLock();
> lock.lock();
> // do something
> lock.unlock();
> ```

**非平锁**

> 1. 首先是无参构造，默认为创建非公平锁。
> 2. lock()是Sync内部类中的抽象方法在NofailSync内部类中有对应的实现。
>
> ```java
> final void lock() {
>     // 1. 当State的值为0时尝试设置其为1
>     // 在ReentrantLock中state的值表示锁被持有的个数，为0时表示无线程持有，为1时表示线程持有一个锁，大于1表示线程重入了多个锁
> 	if (compareAndSetState(0, 1)) {
>         //如果state设置成功则将锁的持有线程更新为当前线程
> setExclusiveOwnerThread(Thread.currentThread());
> 	}
> 	else{
>         // 尝试设置失败后
> 		acquire(1);
>     }
> }
> ```
>
> acquire()在AQS中
>
> ```java
>     public final void acquire(int arg) {
>         if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
>             selfInterrupt();
>     }
> ```
>
> tryAcquire()在NofailSync中实现
>
> ```java
>         protected final boolean tryAcquire(int acquires) {
>             return nonfairTryAcquire(acquires);
>         }
> ```
>
> nonfairTryAcquire()在NofailSync中。这一步中就是单纯的获取锁，如果没拿到就会返回false
>
> ```java
>         final boolean nonfairTryAcquire(int acquires) {
>             final Thread current = Thread.currentThread();
>             int c = getState();
>             // c==0表示锁无人持有
>             if (c == 0) {
>                 // CAS设置状态值，成功则设置当前线程为持有线程
>                 if (compareAndSetState(0, acquires)) {
>                     setExclusiveOwnerThread(current);
>                     return true;
>                 }
>             }
>             // 若当前线程为持有锁的线程，则为重入操作，令state+1
>             else if (current == getExclusiveOwnerThread()) {
>                 int nextc = c + acquires;
>                 if (nextc < 0) // overflow
>                     throw new Error("Maximum lock count exceeded");
>                 setState(nextc);
>                 return true;
>             }
>             return false;
>         }
> ```
>
> addWaiter()，此方法是创建CLH队列节点并将其加入队列尾，并返回此Node对象
>
> ```java
>     private Node addWaiter(Node mode) {
>         //创建当前线程对应的Node
>         Node node = new Node(Thread.currentThread(), mode);
>         Node pred = tail;
>         // 若CLH队列已初始化，则在此代码块中加入队尾
>         if (pred != null) {
>             node.prev = pred;
>             if (compareAndSetTail(pred, node)) {
>                 pred.next = node;
>                 return node;
>             }
>         }
>         // 若CLH队列未初始化，会在此方法中初始化
>         enq(node);
>         return node;
>     }
> ```
>
> acquireQueued()，线程进入此方法将会进入无限循环知道拿到锁才return，
>
> ```java
>     final boolean acquireQueued(final Node node, int arg) {
>         boolean failed = true;
>         try {
>             boolean interrupted = false;
>             for (;;) {
>                 final Node p = node.predecessor();
>                 // 如果前驱节点是头结点，并且尝试获得锁成功，则将此节点设置为头结点，前驱节点出队列。
>                 if (p == head && tryAcquire(arg)) {
>                     setHead(node);
>                     p.next = null; // help GC
>                     failed = false;
>                     return interrupted;
>                 }
>                 // 判断前驱节点状态，决定是否阻塞此线程
>                 if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
>                     interrupted = true;
>             }
>         } finally {
>             if (failed)
>                 cancelAcquire(node);
>         }
>     }
> ```
>
> shouldParkAfterFailedAcquire()，判断前驱节点的状态设置
>
> ```java
>     private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
>         // 获得前驱节点的状态
>         int ws = pred.waitStatus;
>         
>         if (ws == Node.SIGNAL)
>             return true;
>        //前继节点已经被取消，则通过先前回溯找到一个有效(非CANCELLED状态)的节点，并返回false
>         if (ws > 0) {
> 
>             do {
>                 node.prev = pred = pred.prev;
>             } while (pred.waitStatus > 0);
>             pred.next = node;
>         } else {
> 			// 如果前驱节点为0或共享锁状态，则将其设置为SIGNAL状态
>             compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
>         }
>         return false;
>     }
> ```
>
> parkAndCheckInterrupt()，线程等待锁时就是被阻塞在这个方法中，何时被唤醒呢？下文会得到解释。
>
> ```java
>     private final boolean parkAndCheckInterrupt() {
>         // 阻塞线程
>         LockSupport.park(this);
>         // 返回线程的中断状态
>         return Thread.interrupted();
>     }
> ```
>
> 3. unlock()，释放锁的方法在ReentrantLock中是公平锁与非公平锁通用的方法。
>
> ```java
>     public void unlock() {
>         sync.release(1);
>     }
> ```
>
> release()在AQS中
>
> ```java
>     public final boolean release(int arg) {
>         //尝试释放锁，如果成功则进入unparkSuccessor唤醒一个有效后继节点
>         if (tryRelease(arg)) {
>             Node h = head;
>             if (h != null && h.waitStatus != 0)
>                 unparkSuccessor(h);
>             return true;
>         }
>         return false;
>     }
> ```
>
> tryRelease()在ReentrantLock中实现，根据可重入锁的规则释放独占锁
>
> ```java
>         protected final boolean tryRelease(int releases) {
>             // 释放指定数量锁，因为它是可重入锁
>             int c = getState() - releases;
>             //如果释放锁的线程不是持有锁的线程则抛出异常
>             if (Thread.currentThread() != getExclusiveOwnerThread())
>                 throw new IllegalMonitorStateException();
>             boolean free = false;
>             //如果释放后state=0则设置锁无线程持有
>             if (c == 0) {
>                 free = true;
>                 setExclusiveOwnerThread(null);
>             }
>             setState(c);
>             return free;
>         }
> ```
>
> unparkSuccessor()在AQS中实现，
>
> ```java
>     private void unparkSuccessor(Node node) {
> 
>         int ws = node.waitStatus;
>         if (ws < 0)
>             compareAndSetWaitStatus(node, ws, 0);
> 
>         Node s = node.next;
>         // 寻找一个有效后继节点
>         if (s == null || s.waitStatus > 0) {
>             s = null;
>             for (Node t = tail; t != null && t != node; t = t.prev)
>                 if (t.waitStatus <= 0)
>                     s = t;
>         }
>         if (s != null)
>             // 唤醒后继节点对应的线程
>             LockSupport.unpark(s.thread);
>     }
> ```

> **总结：**
>
> ReentrantLock非公平锁lock()的过程为尝试获取锁，若获取失败则加入CLH队列中，在队列中根据前驱节点的状态决定是否进入阻塞，在被唤醒后再尝试获取锁，若成功获取到则将Head节点出队列，并将本节点设置成Head节点。
>
> unlock()过程比较简单，尝试释放锁，如果释放成功则唤醒一个有效后继节点。

> 公平锁比较简单，仅仅比非公平锁少了一个步骤，留给你自己探索吧。

### 结合CountDownLatch

> ![1570690578379](https://github.com/Coder999z/Java-Notes/blob/master/img/1/1570690578379.png)
>
> AQS中state在这用来存栅栏的数量

> **示例代码**
>
> ```java
> Thread-1
> CountDownLatch latch = new CountDownLatch(1);
> latch.await();
> ....
> 
> Thread-2
> latch.countDown();
> ```

> 1. 构造方法中指定了栅栏的数量，设置的是AQS中state的值。
> 2. 线程调用await()进入等待，栅栏数减为0时线程继续运行
>
> ```java
>     public void await() throws InterruptedException {
>         sync.acquireSharedInterruptibly(1);
>     }
> ```
>
> acquireSharedInterruptibly()
>
> ```java
>     public final void acquireSharedInterruptibly(int arg)
>             throws InterruptedException {
>         //如果线程被中断则抛出异常
>         if (Thread.interrupted())
>             throw new InterruptedException();
>         // 如果栅栏数不等于则进入判断
>         if (tryAcquireShared(arg) < 0)
>             doAcquireSharedInterruptibly(arg);
>     }
> ```
>
> tryAcquireShared()在CountDownLatch中的实现
>
> ```java
>         protected int tryAcquireShared(int acquires) {
>             // 判断栅栏数是否等于0，是返回1否则返回-1
>             return (getState() == 0) ? 1 : -1;
>         }
> ```
>
> doAcquireSharedInterruptibly()
>
> ```java
>     private void doAcquireSharedInterruptibly(int arg)
>         throws InterruptedException {
>         final Node node = addWaiter(Node.SHARED);
>         boolean failed = true;
>         try {
>             for (;;) {
>                 final Node p = node.predecessor();
>                 if (p == head) {
>                     //判断state是否等于0，不等于0返回-1
>                     int r = tryAcquireShared(arg);
>                     if (r >= 0) {
>                         setHeadAndPropagate(node, r);
>                         p.next = null; // help GC
>                         failed = false;
>                         return;
>                     }
>                 }
>                 // 根据前驱节点判断进入阻塞，实际上栅栏数只要不等于0就一定会进入阻塞
>                 if (shouldParkAfterFailedAcquire(p, node) &&
>                     parkAndCheckInterrupt())
>                     throw new InterruptedException();
>             }
>         } finally {
>             if (failed)
>                 cancelAcquire(node);
>         }
>     }
> ```
>
> 3. countDown()减少栅栏数
>
> ```java
>     public void countDown() {
>         sync.releaseShared(1);
>     }
> ```
>
> releaseShared()在AQS中
>
> ```java
>     public final boolean releaseShared(int arg) {
>         if (tryReleaseShared(arg)) {
>             doReleaseShared();
>             return true;
>         }
>         return false;
>     }
> ```
>
> tryReleaseShared()在AQS
>
> ```java
>         protected boolean tryReleaseShared(int releases) {
>             for (;;) {
>                 int c = getState();
>                 //栅栏=0时不能减少
>                 if (c == 0)
>                     return false;
>                 int nextc = c-1;
>                 // CAS设置栅栏数
>                 if (compareAndSetState(c, nextc))
>                     return nextc == 0;
>             }
>         }
> ```
>
> doReleaseShared()，当栅栏数=0时执行此方法，方法中循环执行唤醒CLH队列中等待的线程。
>
> ```java
>     private void doReleaseShared() {
> 
>         for (;;) {
>             Node h = head;
>             if (h != null && h != tail) {
>                 int ws = h.waitStatus;
>                 if (ws == Node.SIGNAL) {
>                     if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
>                         continue;            
>                     unparkSuccessor(h);
>                 }
>                 else if (ws == 0 &&
>                          !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
>                     continue;               
>             }
>             if (h == head)                  
>                 break;
>         }
>     }
> ```

> **总结**
>
> 实际上CountDownLatch是基于共享锁的思想实现的，采用CAS无锁操作对State的值进行改变，当State=0时即为栅栏数=0，CLH队列中阻塞的Node进入共享锁获取锁。

### 总结

> 吃透了ReentrantLock和CountdownLatch后理解Semaphore和CyclicBarrier也就轻松得多了，后续有时间再补充。

## 参考

> https://www.cnblogs.com/skywang12345/p/java_threads_category.html