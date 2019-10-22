* [单例模式](#%E5%8D%95%E4%BE%8B%E6%A8%A1%E5%BC%8F)
* [懒汉式单例](#%E6%87%92%E6%B1%89%E5%BC%8F%E5%8D%95%E4%BE%8B)
* [饿汉式单例](#%E9%A5%BF%E6%B1%89%E5%BC%8F%E5%8D%95%E4%BE%8B)
* [登记式单例](#%E7%99%BB%E8%AE%B0%E5%BC%8F%E5%8D%95%E4%BE%8B)

# 单例模式

> 单例是一种常见的设计模式，在各大框架中经常见到，例如Spring中的SpringBean默认就是单例，单例模式需要保证整个系统中单例模式的类只能存在一个实例。Java实现简单的单例模式可以大致有三种实现方式：饿汉式、懒汉式、登记式。

# 懒汉式单例
> ```java
> public class SingleTon {
>     private static SingleTon singleTon = null;
>     private SingleTon() {}
>     public static SingleTon getInstance() {
>         if (singleTon == null) {
>             singleTon = new SingleTon();
>         }
>         return singleTon;
>     }
> }
> ```

> 这里构造方法使用了private修饰，防止被外部显式调用初始化（忽略反射这茬）。只有在getInstance()中可以获取单例对象singleton，单线程情况下没有问题，但是如果在多线程的情况下会出现[线程不安全问题](https://blog.csdn.net/weixin_43184769/article/details/89708302)，导致创建了多个对象。

>**添加锁** 。在getInstance()方法上添加内部锁，或者ReentrantLock，保证线程的安全，但是加锁开销大。

> ```java
> //加内部锁的例子
> public class SingleTon {
>     private static SingleTon singleTon = null;
>     private SingleTon() {}
>     public static synchronized SingleTon getInstance() {
>         if (singleTon == null) {
>             singleTon = new SingleTon();
>         }
>         return singleTon;
>     }
> }
> ```

>  **双重检查锁定：** 

> ```java
> public static Singleton getInstance() {
>         if (singleton == null) {  
>             synchronized (Singleton.class) {  
>                if (singleton == null) {  
>                   singleton = new Singleton(); 
>                }  
>             }  
>         }  
>         return singleton; 
>     }
> ```

> 这里需要注意为什么写了两个判断空的操作？如果只有一个外层的判空：

> ```java
> public static Singleton getInstance() {
>         if (singleton == null) {  
>             synchronized (Singleton.class) {  
>                   singleton = new Singleton(); 
>             }  
>         }  
>         return singleton; 
>     }
> ```

> 那么在多线程情况下可能会有多个线程判断singleton == null，进入锁池等待获得同步锁，但是只要有一个线程执行完同步代码，单例对象就已经被创建了。后序争取到锁的线程继续执行同步代码块依然会创建新的对象，就违反了单例的原则。那么双重判断就好理解了，获取到锁后内部再进行一次判断，如果创建了就直接返回。

> **静态内部类形式**

> ```java
> public class SingleTon {
>     private SingleTon() {}
> 
>     public static SingleTon getInstance() {
>         return InitSingleTon.singleTon;
>     }
> 
>     private static class InitSingleTon{
>         private final static SingleTon singleTon = new SingleTon();
>     }
> }
> ```

>  这里在单例类的内部创建了静态内部类，在静态内部类中创建了静态的单例类对象。使用静态内部类的方式可以保证多线程环境下获取单例对象的线程安全性。
>
> `静态内部类的优点在于`：
>
> 外部类加载时并不需要立即加载内部类，同理内部类没有加载那么就不会去初始化内部类中的静态成员变量，只有在第一次调用getInstance()方法时才会进行初始化，类似于懒加载。
>
> `静态内部类是如何保证线程安全的呢？`
> JVM会保证一个类的初始化过程在多线程中被正确的加锁同步，如果多个线程同时去初始化一个类，那么只有一个类会真正去执行初始化方法，其他线程需要阻塞等待。
>
> `静态内部类的缺点：`
>
> 静态内部类由于使用的是内部类，那么如果在单例对象中有参数需要设置时使用静态内部类将无法传递。


# 饿汉式单例
> ```java
> public class SingleTon {
>     private static final SingleTon singleTon = new SingleTon();
>     private SingleTon() {}
>     public static SingleTon getInstance() {
>         return singleTon;
>     } 
> }
> ```

>  从代码中可见，当SingleTon 类初始化时，单例对象也已经被初始化了，所以不存在线程不安全问题。

# 登记式单例
>  在Spring中管理单例对象使用的就是登记式单例，一个Hash，key为类名，value为单例对象。

> ```java
> //类似Spring里面的方法，将类名注册，下次从里面直接获取。
> public class Singleton {
>     private static Map<String,Singleton> map = new HashMap<String,Singleton>();
>     static{
>         Singleton single = new Singleton();
>         map.put(single.getClass().getName(), single);
>     }
>     
>     protected Singleton(){}
>     public static Singleton getInstance(String name) {
>         if(name == null) {
>             return null;
>         }
>         if(map.get(name) == null) {
>             try {
>                 map.put(name, (Singleton) Class.forName(name).newInstance());
>             } catch (InstantiationException e) {
>                 e.printStackTrace();
>             } catch (IllegalAccessException e) {
>                 e.printStackTrace();
>             } catch (ClassNotFoundException e) {
>                 e.printStackTrace();
>             }
>         }
>         return map.get(name);
>     }
> }
> ```

> 其实也很简单，内部是一个饿汉式的加载，只不过多了个根据name拿对象的过程。