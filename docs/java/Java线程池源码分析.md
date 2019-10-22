
* [什么是线程池？](#%E4%BB%80%E4%B9%88%E6%98%AF%E7%BA%BF%E7%A8%8B%E6%B1%A0)
* [为什么要用线程池？](#%E4%B8%BA%E4%BB%80%E4%B9%88%E8%A6%81%E7%94%A8%E7%BA%BF%E7%A8%8B%E6%B1%A0)
* [线程池的使用方式](#%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%9A%84%E4%BD%BF%E7%94%A8%E6%96%B9%E5%BC%8F)
  * [ThreadPoolExecutor](#threadpoolexecutor)
    * [核心属性](#%E6%A0%B8%E5%BF%83%E5%B1%9E%E6%80%A7)
    * [几种创建方式](#%E5%87%A0%E7%A7%8D%E5%88%9B%E5%BB%BA%E6%96%B9%E5%BC%8F)
    * [创建任务](#%E5%88%9B%E5%BB%BA%E4%BB%BB%E5%8A%A1)
      * [Callable和Runnable的区别](#callable%E5%92%8Crunnable%E7%9A%84%E5%8C%BA%E5%88%AB)
      * [execute和submit区别](#execute%E5%92%8Csubmit%E5%8C%BA%E5%88%AB)
    * [关闭线程池](#%E5%85%B3%E9%97%AD%E7%BA%BF%E7%A8%8B%E6%B1%A0)
    * [ThreadPoolExecutor原理简析](#threadpoolexecutor%E5%8E%9F%E7%90%86%E7%AE%80%E6%9E%90)
      * [Worker对象](#worker%E5%AF%B9%E8%B1%A1)
    * [execute()原理](#execute%E5%8E%9F%E7%90%86)
    * [线程复用的秘密](#%E7%BA%BF%E7%A8%8B%E5%A4%8D%E7%94%A8%E7%9A%84%E7%A7%98%E5%AF%86)
* [JDK1\.7中新增线程池](#jdk17%E4%B8%AD%E6%96%B0%E5%A2%9E%E7%BA%BF%E7%A8%8B%E6%B1%A0)
* [参考](#%E5%8F%82%E8%80%83)

# 什么是线程池？
> 线程池可以看做是线程的`集合`。线程池中提供了对池中的`线程`与`任务`的集中调度和管理。

# 为什么要用线程池？
> 如果不使用线程池，显示的创建每一个线程也是可以实现业务，但是线程生命周期的`开销非常高`，它的创建和销毁所花费的资源和时间可能比真正处理任务的花销还要高。而且在受到攻击时，创建了过多的线程很容易造成系统的不稳定，很容易就挂了。所以推荐使用线程池在管理线程，向线程池中添加任务。

>线程池的好处有：
>
>- `降低资源消耗`。 通过重复利用已创建的线程降低线程创建和销毁造成的消耗。
>- `提高响应速度`。 当任务到达时，任务可以不需要的等到线程创建就能立即执行。
>- `提高线程的可管理性`。 线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一的分配，调优和监控。

# 线程池的使用方式

> JDK提供的线程池的总体架构：
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190519103050336.png)



## ThreadPoolExecutor
### 核心属性
> ```java
> //线程池的基本大小
> int corePoolSize
> //线程池中允许的最大线程数
> int maximumPoolSize
> //最大线程空闲时间
> long keepAliveTime
> // 任务的阻塞队列
> BlockingQueue<Runnable> workQueue
> // 任务拒绝策略
> RejectedExecutionHandler handler
> ```
>
> 
>
> **线程池处理任务策略：**
> 假设当前线程池中运行线程的数量为poolSize。
>
> - 当 poolSize < corePoolSize，新任务到达时`创建新的线程处理`
> - 当 poolSize = corePoolSize，新任务到达时会被`放入阻塞等待队列`workQueue。
> - 当 workQueue 的容量`到达上限`，且 `poolSize < maximumPoolSize`，则`新建`线程来处理任务。
> - 当 workQueue 的容量`到达上限`，且 `poolSize = maximumPoolSize`，那么线程池已经到达上限，会根据`handler指定的策略拒绝新任务`。
>
> ------
>
> **线程池拒绝策略：**
> 触发条件：`任务阻塞队列到达上限，且线程池中的线程数量等于允许的最大线程数。`
>
> - **AbortPolicy**：线程池的`默认`策略，满足触发条件时，它会直接`丢弃任务`，并`抛出RejectedExecutionException异常`。
> - **DiscardPolicy**：当触发满足条件时，它会`丢弃任务`，但是`不报错`。
> - **DiscardOldestPolicy** ： 当触发满足条件时，将等待最久的任务出队列移除，并将新任务入队列。
> - **CallerRunsPolicy**：当满足触发条件时，此任务会由给线程池添加任务的线程自己执行。
>
> 如果提供的这4个策略不满足使用场景，可以自定义拒绝 策略，只需要实现RejectedExecutionHandler接口，实现rejectedExecution方法即可。
>
> ```java
> public class MyRejectPolicy implements RejectedExecutionHandler{
>     public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
>         //处理逻辑
>     }
> }
> ```

### 几种创建方式
> **构造方法**
> 自定义所有核心参数，构建一个线程池。
>
> ```java
> public ThreadPoolExecutor(int corePoolSize,
>                               int maximumPoolSize,
>                               long keepAliveTime,
>                               TimeUnit unit,
>                               BlockingQueue<Runnable> workQueue,
>                               ThreadFactory threadFactory,
>                               RejectedExecutionHandler handler) {
>         if (corePoolSize < 0 ||
>             maximumPoolSize <= 0 ||
>             maximumPoolSize < corePoolSize ||
>             keepAliveTime < 0)
>             throw new IllegalArgumentException();
>         if (workQueue == null || threadFactory == null || handler == null)
>             throw new NullPointerException();
>         this.corePoolSize = corePoolSize;
>         this.maximumPoolSize = maximumPoolSize;
>         this.workQueue = workQueue;
>         this.keepAliveTime = unit.toNanos(keepAliveTime);
>         this.threadFactory = threadFactory;
>         this.handler = handler;
>     }
> ```
>
> ------
>
> **Executors工具类创建**
>
> - **newFixedThreadPool：** 创建的是一个固定数量的线程池，它返回一个`corePoolSize和maximumPoolSize相等的线程池`。
>
> ```java
> public static ExecutorService newFixedThreadPool(int nThreads) {
>         return new ThreadPoolExecutor(nThreads, nThreads,
>                                       0L, TimeUnit.MILLISECONDS,
>                                       new LinkedBlockingQueue<Runnable>());
>     }
> ```
>
> - **newCachedThreadPool：** 创建一个corePoolSize=0，maximumPoolSize=int最大值的线程池，也就是说每添加一个任务如果池子里没有空闲的线程就会创建一个新线程处理。
>
> ```java
> public static ExecutorService newCachedThreadPool() {
>         return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
>                                       60L, TimeUnit.SECONDS,
>                                       new SynchronousQueue<Runnable>());
>     }
> ```
>
> - **SingleThreadExecutor：** 创建一个corePoolSize和maximumPoolSize都为1的线程池，也就是说值有一个处理线程。
>
> ```java
> public static ExecutorService newSingleThreadExecutor() {
>         return new FinalizableDelegatedExecutorService
>             (new ThreadPoolExecutor(1, 1,
>                                     0L, TimeUnit.MILLISECONDS,
>                                     new LinkedBlockingQueue<Runnable>()));
>     }
> ```
>
> ### 创建任务
>
> 线程池中提交任务有两种方式：
>
> ```java
> void execute(Runnable command);
> <T> Future<T> submit(Callable<T> task);
> ```
>
> 这里出现了一个陌生的Callable

#### Callable和Runnable的区别
> 平常使用的Runnable在线程执行完毕后是没有返回值的，run()是个void方法，而Callable中的call()是允许返回值了，所以他们俩的最大区别在于一个有返回值一个没有。
>
> ```java
> public abstract void run();
> V call() throws Exception;
> ```
>
> ------
>
> **获得Callable的方式：使用Future对象**
>
> ```java
> //方法很简单执行完线程后返回num的值
> public class DemoCallable implements Callable<Integer> {
>     private int num = 1;
>     @Override
>     public Integer call() throws Exception {
>         return sum;
>     }
> }
> 
> public class Main{
>     public static void main(String[] args) throws Exception {
>         // 创建线程池对象
>         ExecutorService pool = Executors.newSingleThreadExecutor;
>         Future<Integer> f1 = pool.submit(new DemoCallable ());
>         //获得返回值
>         Integer i1 = f1.get();
>         System.out.println(i1);
>         // 结束
>         pool.shutdown();
>     }
> }
> 
> ```
>
> 需要注意的是必须在线程执行完任务后才会返回值，否则获取的线程将会一直阻塞。

#### execute和submit区别

> - execute() 方法用于提交`不需要返回值`的任务，所以无法判断任务是否被线程池执行成功与否；
> - submit() 方法用于提交`需要返回值`的任务。线程池会返回一个Future类型的对象，通过这个Future对象可以判断任务是否执行成功，并且可以通过future的get()方法来获取返回值，get()方法会阻塞当前线程直到任务完成，而使用 get（long timeout，TimeUnit unit）方法则会阻塞当前线程一段时间后立即返回，这时候有可能任务没有执行完。


### 关闭线程池
> ThreadPoolExecutor提供了shutdown()和shutdownNow()两个方法来关闭线程池。
>
> **shutdown()** ：线程池拒绝接收新任务，同时等待线程池中的剩余任务执行完然后关闭线程池。
> **shutdownNow()**：线程池拒绝接收新任务，同时立马关闭线程池，线程池里正在执行的或者在队列里的都不再执行。

### ThreadPoolExecutor原理简析
#### Worker对象
> Worker是ThreadPoolExecutor的一个内部类。线程池中用户处理任务的线程都被封装成了Worker对象。
>
> ```java
> //它实现了Runnable接口，继承了AQS类
> private final class Worker
>         extends AbstractQueuedSynchronizer
>         implements Runnable
>     {
>     	//省略了部分代码
>         { .... }
> 
>         // 
>         final Thread thread;
>         // 线程池中分配的第一个任务
>         Runnable firstTask;
>         // 此Worker完成的任务数量
>         volatile long completedTasks;
> 
>         /**
>          * Creates with given first task and thread from ThreadFactory.
>          * @param firstTask the first task (null if none)
>          */
>         Worker(Runnable firstTask) {
>             setState(-1); // 禁止中断，直到运行程序
>             this.firstTask = firstTask;
>             //调用了线程工厂创建了线程
>             this.thread = getThreadFactory().newThread(this);
>         }
> 
>         // Worker的run方法调用了ThreadPoolExecutor的runWorker方法，参数是自己
>         public void run() {
>             runWorker(this);
>         }
> 
> 		//省略显示锁相关代码
>        {....}
>     }
> ```

###  execute()原理
> ```java
> public void execute(Runnable command) {
>         if (command == null)
>             throw new NullPointerException();
>             
>         int c = ctl.get();
>         
>         // 当 线程池线程数量 < corePoolSize的情况
>         // 创建新线程执行
>         if (workerCountOf(c) < corePoolSize) {
>         	//调用的是addWorker()
>             if (addWorker(command, true))
>                 return;
>             c = ctl.get();
>         }
> 	
> 		//当 线程池的数量 >= corePoolSize ，或者上一步中创建新 线程失败，尝试将任务
> 		//添加到任务队列中。
>         if (isRunning(c) && workQueue.offer(command)) {
>             int recheck = ctl.get();
>             if (! isRunning(recheck) && remove(command))
>                 reject(command);
>             else if (workerCountOf(recheck) == 0)
>                 addWorker(null, false);
>         }
>         // 任务队列满了，创建新线程执行任务
>         else if (!addWorker(command, false))
>             reject(command);
>     }
> ```

> 重点在于**addWorker()**方法
>
> ```java
>  private boolean addWorker(Runnable firstTask, boolean core) {
>     retry:
>     
>     //使用CAS算法轮询线程池的状态
>     // 如果线程池处于SHUTDOWN或之后的状态则拒绝执行任务
>     for (;;) {
>         int c = ctl.get();
>         int rs = runStateOf(c);
> 
>         // Check if queue empty only if necessary.
>         if (rs >= SHUTDOWN &&
>             ! (rs == SHUTDOWN &&
>                firstTask == null &&
>                ! workQueue.isEmpty()))
>             return false;
> 
>         //使用CAS机制尝试将当前线程数+1
>         //如果是核心线程当前线程数必须小于corePoolSize 
>         //如果是非核心线程则当前线程数必须小于maximumPoolSize
>         //如果当前线程数小于线程池支持的最大线程数CAPACITY 也会返回失败
>         for (;;) {
>             int wc = workerCountOf(c);
>             if (wc >= CAPACITY ||
>                 wc >= (core ? corePoolSize : maximumPoolSize))
>                 return false;
>             if (compareAndIncrementWorkerCount(c))
>                 break retry;
>             c = ctl.get();  // Re-read ctl
>             if (runStateOf(c) != rs)
>                 continue retry;
>             // else CAS failed due to workerCount change; retry inner loop
>         }
>     }
> 
>     //这里已经成功执行了CAS操作将线程池数量+1，下面创建线程
>     boolean workerStarted = false;
>     boolean workerAdded = false;
>     Worker w = null;
>     try {
>         w = new Worker(firstTask);
>         //Worker内部有一个Thread，并且执行Worker的run方法，因为Worker实现了Runnable
>         final Thread t = w.thread;
>         if (t != null) {
>             //这里必须同步在状态为运行的情况下将Worker添加到set中
>             final ReentrantLock mainLock = this.mainLock;
>             mainLock.lock();
>             try {
>                 // Recheck while holding lock.
>                 // Back out on ThreadFactory failure or if
>                 // shut down before lock acquired.
>                 int rs = runStateOf(ctl.get());
> 
>                 if (rs < SHUTDOWN ||
>                     (rs == SHUTDOWN && firstTask == null)) {
>                     if (t.isAlive()) // precheck that t is startable
>                         throw new IllegalThreadStateException();
>                     workers.add(w);  //把新建的woker线程放入集合保存，这里使用的是HashSet
>                     int s = workers.size();
>                     if (s > largestPoolSize)
>                         largestPoolSize = s;
>                     workerAdded = true;
>                 }
>             } finally {
>                 mainLock.unlock();
>             }
>             //如果添加成功则运行线程
>             if (workerAdded) {
>                 t.start();
>                 workerStarted = true;
>             }
>         }
>     } finally {
>         //如果woker启动失败，则进行一些善后工作，比如说修改当前woker数量等等
>         if (! workerStarted)
>             addWorkerFailed(w);
>     }
>     return workerStarted;
> }
> ```
>
> addWorker()方法中：
>
> 1. 先尝试在线程池处于`RUNNING状态`下，并且线程数量`未达到上限`的情况下通过`CAS操作`将线程池的数量+1
> 2. 同步的方式创建Worker，并添加到存储work的HashSet中。
> 3. 添加成功后，运行workr内部维护的线程。


### 线程复用的秘密
> 接上一步，在添加worker成功后，运行workr内部维护的线程。
>
> 回顾一下它：
>
> ```java
> //简化过的代码
> private final class Worker
>         extends AbstractQueuedSynchronizer
>         implements Runnable
>     {
> 
> final Thread thread;
> 
> Worker(Runnable firstTask) {
>             setState(-1); // inhibit interrupts until runWorker
>             this.firstTask = firstTask;
>             this.thread = getThreadFactory().newThread(this);
>         }
> public void run() {
>             runWorker(this);
>         }
> }
> ```
>
> 由代码可知：
>
> - Worker中维护的线程thread，在Worker的构造函数中被ThreadFactory初始化，其中的Runnable实现类正是Worker本身，也就是说线程`thread调用了start()以后执行的是Worker中的run()方法`。
>
> ------
>
> 转而分析runWorker()，runWorker()是在ThreadPoolExecutor中实现的方法。
> 
>
> ```java
> //ThreadPoolExecutor类中
> final void runWorker(Worker w) {
>     Thread wt = Thread.currentThread();
>     Runnable task = w.firstTask;
>     w.firstTask = null;
>     // 因为Worker的构造函数中setState(-1)禁止了中断，这里的unclock用于恢复中断
>     w.unlock(); // allow interrupts
>     boolean completedAbruptly = true;
>     try {
>         //一般情况下，task都不会为空（特殊情况上面注释中也说明了），因此会直接进入循环体中
>         while (task != null || (task = getTask()) != null) {
>             w.lock();
>             if ((runStateAtLeast(ctl.get(), STOP) ||
>                  (Thread.interrupted() &&
>                   runStateAtLeast(ctl.get(), STOP))) &&
>                 !wt.isInterrupted())
>                 wt.interrupt();
>             try {
>                 //该方法是个空的实现，如果有需要用户可以自己继承该类进行实现
>                 beforeExecute(wt, task);
>                 Throwable thrown = null;
>                 try {
>                     //真正的任务执行逻辑
>                     task.run();
>                 } catch (RuntimeException x) {
>                     thrown = x; throw x;
>                 } catch (Error x) {
>                     thrown = x; throw x;
>                 } catch (Throwable x) {
>                     thrown = x; throw new Error(x);
>                 } finally {
>                     //该方法是个空的实现，如果有需要用户可以自己继承该类进行实现
>                     afterExecute(task, thrown);
>                 }
>             } finally {
>                 //这里设为null，也就是循环体再执行的时候会调用getTask方法
>                 task = null;
>                 w.completedTasks++;
>                 w.unlock();
>             }
>         }
>         completedAbruptly = false;
>     } finally {
>         //当指定任务执行完成，阻塞队列中也取不到可执行任务时，会进入这里，做一些善后工作
>         //比如在corePoolSize跟maximumPoolSize之间的woker会进行回收
>         processWorkerExit(w, completedAbruptly);
>     }
> }
> ```
>
> runWorker()它是线程复用的核心方法。对它的核心原理进行分析：
>
> 1. 当worker开始run时，进入while判断，如果是首次执行则使用初始化的firstTask，否则调用getTask()去获取任务队列中的任务。
> 2. 拿到任务后进入循环体中，执行前置方法beforeExecute（需要用户实现，否则为空），执行真正的任务逻辑，它调用了任务的run()方法，`注意！不是start()，它调用了Runnable实现类中实现的run方法，并没有启动新线程，只是在本线程内调用了一个方法`。执行完后，执行后置方法afterExecute（同样需要实现）
> 3. 执行完毕后任务设置为空，完成的数量++，继续while循环获取任务。
> 4. 如果此Worker不是核心线程（corePoolSize成员），如果设置了keepAliveTime>0，那么在获取任务等待时超过了空闲时间则会被销毁，直到最终线程的数量等于corePoolSize。同理，核心线程会一直阻塞在获取任务的方法中。
>
> ------
>
> getTask()
>
> ```java
> private Runnable getTask() {
>     boolean timedOut = false; // Did the last poll() time out?
> 
>     for (;;) {
>         int c = ctl.get();
>         int rs = runStateOf(c);
> 
>         //省略检查线程池状态代码
> 
>         int wc = workerCountOf(c);
> 
>         // 省略判断work是否需要被淘汰代码
> 
>         try {
>             
>             // timed为true时，根据指定的空闲时间poll获取任务，指定时间内没拿到则返回null。
>             // time为false时，调用take()获取任务，直到获取到任务前线程一直会被阻塞
>             Runnable r = timed ?
>                 workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
>                 workQueue.take();
>             if (r != null)
>                 return r;
>             timedOut = true;
>         } catch (InterruptedException retry) {
>             timedOut = false;
>         }
>     }
> }
> ```

# JDK1.7中新增线程池


> **ForkJoinPool**
>
> JDK1.7中新增的一个线程池，与ThreadPoolExecutor一样，同样继承了AbstractExecutorService。ForkJoinPool是Fork/Join框架的两大核心类之一。与其它类型的ExecutorService相比，其主要的不同在于采用了工作窃取算法(work-stealing)：所有池中线程会尝试找到并执行已被提交到池中的或由其他线程创建的任务。这样很少有线程会处于空闲状态，非常高效。这使得能够有效地处理以下情景：大多数由任务产生大量子任务的情况；从外部客户端大量提交小任务到池中的情况。

# 参考

> https://github.com/Snailclimb/JavaGuide/blob/master/docs/java/Multithread/JavaConcurrencyAdvancedCommonInterviewQuestions.md#4-%E7%BA%BF%E7%A8%8B%E6%B1%A0

> https://blog.csdn.net/panweiwei1994/article/details/78969238 

> https://blog.csdn.net/u010983881/article/details/79322499

> https://blog.csdn.net/u010983881/article/details/79322499

> 《Java并发编程的艺术》