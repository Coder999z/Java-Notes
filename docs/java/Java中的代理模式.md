
* [概述](#%E6%A6%82%E8%BF%B0)
* [静态代理](#%E9%9D%99%E6%80%81%E4%BB%A3%E7%90%86)
* [动态代理](#%E5%8A%A8%E6%80%81%E4%BB%A3%E7%90%86)
  * [原理分析](#%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90)
* [CGLIB代理](#cglib%E4%BB%A3%E7%90%86)
* [原理分析](#%E5%8E%9F%E7%90%86%E5%88%86%E6%9E%90-1)
  * [JDK和CGLIB的区别](#jdk%E5%92%8Ccglib%E7%9A%84%E5%8C%BA%E5%88%AB)
* [代理模式的优缺点](#%E4%BB%A3%E7%90%86%E6%A8%A1%E5%BC%8F%E7%9A%84%E4%BC%98%E7%BC%BA%E7%82%B9)
# 概述

> 代理是一种设计模式，提供了对目标对象的间接访问，即通过代理访问目标对象的方法，那么就可以很方便的在目标方法的基础上添加额外的操作，前拦截，后拦截等。

> 按照代理的创建时期可以分为两种：
>
> - **静态代理**：由程序员创建代理类或特定工具自动生成源代码再对其编译。在程序运行前代理类的.class文件就已经存在了。
> - **动态代理**：在程序运行时运用反射机制动态创建而成

# 静态代理
> 静态代理的原理比较简单：
>
> 1. 创建代理类实现代理接口
> 2. 在类中维护一个代理对象，通过构造器初始化代理对象
> 3. 实现接口方法，并在接口方法中使用代理对象调用同一接口方法，并实现前后拦截等功能。

> ```java
> //Persion接口
> public interface Persion {
>     void pay();
> }
> 
> //Student.java
> public class Student implements Persion {
>     String name;
> 
>     public Student(String name) {
>         this.name = name;
>     }
> 
>     @Override
>     public void pay() {
>         System.out.println(name + " pay!");
>     }
> }
> 
> //PersionProxy.java    创建代理类实现代理接口
> public class PersionProxy implements Persion {
> 	// 在类中维护一个代理对象，通过构造器初始化代理对象
>     Persion persion;
>     public PersionProxy(Persion persion) {
>         this.persion = persion;
>     }
> 
> 	// 实现接口方法，并在接口方法中使用代理对象调用同一接口方法，并实现前后拦截等功能。
>     @Override
>     public void pay() {
>         System.out.println("before");
>         persion.pay();
>         System.out.println("after");
>     }
> }
> 
> //主函数
> public class Main {
>     public static void main(String[] args) {
>         Student student = new Student("tom");
>         //获取student对象的代理
>         PersionProxy proxy = new PersionProxy(student);
>         //代理执行方法
>         proxy.pay();
>     }
> }
> 
> ```

>  输出结果
>
> ```
> before
> tom pay!
> after
> ```

> 由代码可知静态代理是写死的代码，每个代理只能代理一个类型的指定方法。那么它的缺点也显而易见了，如果需要对大量的接口进行代理，就会有大量的代理类，不利于维护；如果要对代理的接口修改/添加/删除方法，同时也需要维护实现类和代理类，不利于扩展。

# 动态代理

> ```java
> //Persion接口
> public interface Persion {
>     void pay();
> }
> 
> //Student.java
> public class Student implements Persion {
>     String name;
> 
>     public Student(String name) {
>         this.name = name;
>     }
> 
>     @Override
>     public void pay() {
>         System.out.println(name + " pay!");
>     }
> }
> 
> //DynamicProxy .java
> public class DynamicProxy {
> 
>     //返回一个代理对象
>     public static Object agent(Class interfaceClazz, Object proxy) {
>         return Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class[]{interfaceClazz},
>                 new MyHandler(proxy));
>     }
> 
>     static class MyHandler implements InvocationHandler {
>     	//代理对象
>         private Object proxyInstance;
> 
>         public MyHandler(Object proxyInstance) {
>             this.proxyInstance = proxyInstance;
>         }
> 
> 		/**
> 		proxy：被代理的类实例
> 		*/
>         @Override
>         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
>             System.out.println("before");
>             //执行指定方法
>             Object invoke = method.invoke(proxyInstance, args);
>             System.out.println("after");
>             return invoke;
>         }
>     }
> }
> 
> // main方法
> public class Main {
>     public static void main(String[] args) {
>     	//这里必须转成接口，包括参数中的Persion.class否则会报错
>         Persion proxy = (Persion) DynamicProxy.agent(Persion.class, new Student("tom"));
>         proxy.pay();
>     }
> }
> ```
>
> 输出结果
>
> ```
> before
> tom pay!
> after
> ```

> 优点：代理无需实现被代理的接口，编写的代理类可以代理任何实现了接口的对象（通过反射机制实现），具有较高的复用性。
>
> 缺点：被代理的目标对象必须要实现接口，否则无法使用JDK动态代理。
> 原理在于生成代理类中调用的方法实际指向invoke。

## 原理分析
> 现在来研究研究它的原理，由于源码分析较为复杂，能力有限，只看会了大神分析的大概思路，后序再补充。
> 首先是在newProxyInstance方法中，它生成了代理类，此代理类是动态生成的类文件，看代码：
> （摘自博客：https://www.cnblogs.com/gonjan-blog/p/6685611.html）
>
> ```java
> import java.lang.reflect.InvocationHandler;
> import java.lang.reflect.Method;
> import java.lang.reflect.Proxy;
> import java.lang.reflect.UndeclaredThrowableException;
> import proxy.Person;
> 
> public final class $Proxy0 extends Proxy implements Person
> {
>   private static Method m1;
>   private static Method m2;
>   private static Method m3;
>   private static Method m0;
>   
>   /**
>   *注意这里是生成代理类的构造方法，方法参数为InvocationHandler类型，看到这，是不是就有点明白
>   *为何代理对象调用方法都是执行InvocationHandler中的invoke方法，而InvocationHandler又持有一个
>   *被代理对象的实例，不禁会想难道是....？ 没错，就是你想的那样。
>   *
>   *super(paramInvocationHandler)，是调用父类Proxy的构造方法。
>   *父类持有：protected InvocationHandler h;
>   *Proxy构造方法：
>   *    protected Proxy(InvocationHandler h) {
>   *         Objects.requireNonNull(h);
>   *         this.h = h;
>   *     }
>   *
>   */
>   public $Proxy0(InvocationHandler paramInvocationHandler)
>     throws 
>   {
>     super(paramInvocationHandler);
>   }
>   
>   //这个静态块本来是在最后的，我把它拿到前面来，方便描述
>    static
>   {
>     try
>     {
>       //看看这儿静态块儿里面有什么，是不是找到了giveMoney方法。请记住giveMoney通过反射得到的名字m3，其他的先不管
>       m1 = Class.forName("java.lang.Object").getMethod("equals", new Class[] { Class.forName("java.lang.Object") });
>       m2 = Class.forName("java.lang.Object").getMethod("toString", new Class[0]);
>       m3 = Class.forName("proxy.Person").getMethod("giveMoney", new Class[0]);
>       m0 = Class.forName("java.lang.Object").getMethod("hashCode", new Class[0]);
>       return;
>     }
>     catch (NoSuchMethodException localNoSuchMethodException)
>     {
>       throw new NoSuchMethodError(localNoSuchMethodException.getMessage());
>     }
>     catch (ClassNotFoundException localClassNotFoundException)
>     {
>       throw new NoClassDefFoundError(localClassNotFoundException.getMessage());
>     }
>   }
>  
>   /**
>   * 
>   *这里调用代理对象的giveMoney方法，直接就调用了InvocationHandler中的invoke方法，并把m3传了进去。
>   *this.h.invoke(this, m3, null);这里简单，明了。
>   *来，再想想，代理对象持有一个InvocationHandler对象，InvocationHandler对象持有一个被代理的对象，
>   *再联系到InvacationHandler中的invoke方法。嗯，就是这样。
>   */
>   public final void giveMoney()
>     throws 
>   {
>     try
>     {
>       this.h.invoke(this, m3, null);
>       return;
>     }
>     catch (Error|RuntimeException localError)
>     {
>       throw localError;
>     }
>     catch (Throwable localThrowable)
>     {
>       throw new UndeclaredThrowableException(localThrowable);
>     }
>   }
> 
>   //注意，这里为了节省篇幅，省去了toString，hashCode、equals方法的内容。原理和giveMoney方法一毛一样。
> 
> }
> ```
>
> 

> 通过反编译的代码可以看出，生成的类继承了Proxy类，并实现了代理的interface接口，类中持有自定义的InvocationHandler 类h，而h中又拥有真实对象。

> 那么思路经很清晰了，代理类实现了接口，对其中的接口的方法的实现中，实际上调用的是h中的invoke方法。通过反射来获取每一个方法对象。

>  **总结一下步骤**
>
> 1. 生成一个实现了参数interface里所有接口并继承了Proxy代理类的字节码，并使用参数里的ClassLoader加载了这个代理类。
> 2. 使用代理类父类Proxy(InvocationHandler h)的构造函数创建一个代理类实例，将自定义的InvocationHandler实现类传入。并返回该实例。
> 3. 调用代理类实例的方法时，都会调用自定义handler中的invoke()方法。

# CGLIB代理

> ```java
> //Student.java
> public class Student implements Persion {
>     String name;
> 
>     public Student(String name) {
>         this.name = name;
>     }
> 
>     @Override
>     public void pay() {
>         System.out.println(name + " pay!");
>     }
> }
> 
> //CGLIBProxy.java
> public class CGLIBProxy implements MethodInterceptor {
> 	Object proxy;
> 	
> 	public Object getInstance(Object proxy) {
>         this.proxy = proxy;
>         Enhancer enhancer = new Enhancer();
>         enhancer.setSuperclass(this.proxy.getClass());
>         // 回调方法
>         enhancer.setCallback(this);
>         // 创建代理对象
>         return enhancer.create();
>     }
> 
> 	@Override
>     public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
>         System.out.println("beforeg");
>         //真正调用
>         Object obj= methodProxy.invokeSuper(o, objects);
>         System.out.println("after");
>         return obj;
>     }
> }
> 
> //main方法
> public static void main(String[] args) {
>         CGLIBProxy proxy = new CGLIBProxy ();
>         Student student= (Student) proxy.getInstance(new Student("tom"));
>         student.pay();
> }
> ```
>
> 输出结果
>
> ```
> before
> tom pay!
> after
> ```
>
> 使用方式和JDK的动态代理类似，但是它可以直接对没有实现接口的类进行操作，会更加方便。

# 原理分析
>  CGLIB(Code Generation Library)是一个开源项目！是一个强大的，高性能，高质量的Code生成类库，它可以在运行期扩展Java类与实现Java接口。Hibernate支持它来实现PO(Persistent Object 持久化对象)字节码的动态生成。（来自百度百科）
>
>   > 简单说它是用来生成字节码的库

> CGLIB可以代理没有实现接口的类，相比JDK的动态代理更加灵活，性能也更好。
>
> 下面通过反编译CGLIB生成的代理类进行分析。

> ```java
> import net.sf.cglib.core.Signature;
> import net.sf.cglib.core.ReflectUtils;
> import net.sf.cglib.proxy.MethodProxy;
> import java.lang.reflect.Method;
> import net.sf.cglib.proxy.MethodInterceptor;
> import net.sf.cglib.proxy.Callback;
> import net.sf.cglib.proxy.Factory;
> 
> // 
> // Decompiled by Procyon v0.5.30
> // 
> 
> public class UserService$$EnhancerByCGLIB$$394dddeb extends UserService implements Factory
> {
>     private boolean CGLIB$BOUND;
>     private static final ThreadLocal CGLIB$THREAD_CALLBACKS;
>     private static final Callback[] CGLIB$STATIC_CALLBACKS;
>     //我们自定义的拦截器对象会被这个指针指向
>     private MethodInterceptor CGLIB$CALLBACK_0;
>     private static final Method CGLIB$add$0$Method;
>     private static final MethodProxy CGLIB$add$0$Proxy;
>     private static final Object[] CGLIB$emptyArgs;
> 
>     
>     static void CGLIB$STATICHOOK2() {
>         CGLIB$THREAD_CALLBACKS = new ThreadLocal();
>         CGLIB$emptyArgs = new Object[0];
>         final Class<?> forName = Class.forName("UserService$$EnhancerByCGLIB$$394dddeb");
>         final Class<?> forName3;
>         CGLIB$add$0$Method = ReflectUtils.findMethods(new String[] { "add", "()V" }, (forName3 = Class.forName("UserService")).getDeclaredMethods())[0];
>         CGLIB$add$0$Proxy = MethodProxy.create((Class)forName3, (Class)forName, "()V", "add", "CGLIB$add$0");
>     }
>     
>     
>     final void CGLIB$add$0() {
>         super.add();
>     }
>     
>     public final void add() {
>         MethodInterceptor cglib$CALLBACK_2;
>         MethodInterceptor cglib$CALLBACK_0;
>         //判断自定义的拦截器是否为空，为空则调用CGLIB$BIND_CALLBACKS()，这个方法没有研究，大概是初始化拦截器
>         if ((cglib$CALLBACK_0 = (cglib$CALLBACK_2 = this.CGLIB$CALLBACK_0)) == null) {
>             CGLIB$BIND_CALLBACKS(this);
>             cglib$CALLBACK_2 = (cglib$CALLBACK_0 = this.CGLIB$CALLBACK_0);
>         }
>         // 如果拦截器不为空则调用拦截器中重写的intercept()方法，并返回
>         if (cglib$CALLBACK_0 != null) {
>             cglib$CALLBACK_2.intercept((Object)this, UserService$$EnhancerByCGLIB$$394dddeb.CGLIB$add$0$Method, UserService$$EnhancerByCGLIB$$394dddeb.CGLIB$emptyArgs, UserService$$EnhancerByCGLIB$$394dddeb.CGLIB$add$0$Proxy);
>             return;
>         }
>         super.add();
>     }
>      
>     static {
>         CGLIB$STATICHOOK2();
>     }
> }
> ```
>
> 

> 代码中的命名有点迷，重点看注释部分就可以。
> 简单总结一下：
>
> 1. 代理类继承了委托类，并且委托类的final方法不能被代理
> 2. 代理类为每个委托的方法都生成了两个，以代码中的为例CGLIB$add$0直接调用了委托类的原方法。
> 3. 当执行代理对象的add方法时，会先判断是否存在实现了MethodInterceptor接口的对象cglib$CALLBACK_0，如果存在，则调用MethodInterceptor对象的intercept方法实现了AOP。


## JDK和CGLIB的区别

> - JDK动态代理生成的代理类和委托类实现了相同的接口，再通过实现接口的方式调用了自定义的invoke方法实现的AOP。
> - CGLIB则是通过继承代理类，通过重写方法，在方法中调用拦截器实现的Invoke方法实现的AOP。
> - JDK动态代理只能对接口的实现类进行代理，底层通过反射进行方法的调用 
> - CGLIB可以对普通类进行代理，但是不能对final修饰的类以及final，private方法进行代理。它底层将方法放入一个数组中，通过索引直接进行方法的调用。


# 代理模式的优缺点
> 优点：代理模式能将代理对象和真实被调用的目标对象分离，使`代理逻辑`与`业务逻辑`相互独立互不影响，降低了耦合，有利于对原方法的增强与扩展。
>
> 缺点：使用代理模式会造成系统设计中类的数目增加，对内存的控制难度增加。使用动态代理会对性能造成一定的影响，它需要动态生成类。还会增加系统的复杂度。