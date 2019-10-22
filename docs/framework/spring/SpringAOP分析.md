
* [概述](#%E6%A6%82%E8%BF%B0)
* [面向切面](#%E9%9D%A2%E5%90%91%E5%88%87%E9%9D%A2)
* [代理模式](#%E4%BB%A3%E7%90%86%E6%A8%A1%E5%BC%8F)
* [SpringAOP的概念理解](#springaop%E7%9A%84%E6%A6%82%E5%BF%B5%E7%90%86%E8%A7%A3)
* [示例](#%E7%A4%BA%E4%BE%8B)
* [AOP的使用场景有哪些](#aop%E7%9A%84%E4%BD%BF%E7%94%A8%E5%9C%BA%E6%99%AF%E6%9C%89%E5%93%AA%E4%BA%9B)
* [Spring AOP的实现原理](#spring-aop%E7%9A%84%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86)
* [AOP有哪些可用的实现？](#aop%E6%9C%89%E5%93%AA%E4%BA%9B%E5%8F%AF%E7%94%A8%E7%9A%84%E5%AE%9E%E7%8E%B0)

# 概述
> AOP（Aspect Oriented Programming），即`面向切面编程`。它利用一种称为"横切"的技术，剖解开封装的对象内部，并将那些影响了多个类的`公共行为`封装到一个`可重用模块`，并将其命名为"Aspect"，即`切面`。所谓"切面"，简单说就是那些`与业务无关`，却为业务模块所`共同调用`的逻辑或责任封装起来，便于减少系统的重复代码，降低模块之间的耦合度，并有利于未来的可操作性和可维护性。

> AOP思想的实现一般都是基于 `代理模式 `，在JAVA中一般采用JDK动态代理模式，但是我们都知道，JDK动态代理模式只能代理`接口`而不能代理类。因此，Spring AOP 会进行切换，因为Spring AOP 同时支持 CGLIB、ASPECTJ、JDK动态代理。
> `如果目标对象的实现类实现了接口，Spring AOP 将会采用 JDK 动态代理来生成 AOP 代理类`
> `如果目标对象的实现类没有实现接口，Spring AOP 将会采用 CGLIB 来生成 AOP 代理类`

# 面向切面
> AOP称为面向切面编程。
> 先由一个小例子引入。

> ```java
> public void setMoney(long num){
> 	//开启事务
> 	
> 	//业务代码
> 	
> 	//提交事务
> 	//记录日志
> }
> 
> public void transferAccounts(User u1,User u2,long num){
> 	//开启事务
> 	
> 	//业务代码
> 	
> 	//提交事务
> 	//记录日志
> }
> ```
>
> 

> 例子中的两个方法的执行顺序类似，都是在开始时启动事务，然后执行业务代码，最后提交事务，记录日志。我们可以发现其中的开启事务，提交事务，记录日志都是在重复相同的操作，此时可以将它们进行封装。

> 此时引入AOP的理念：`将分散在各个业务逻辑代码中相同的代码通过横向切割的方式抽取到一个独立的模块中`。即切面。所谓"切面"，简单说就是那些与业务无关，却为业务模块所共同调用的逻辑或责任封装起来，便于减少系统的重复代码，降低模块之间的耦合度，并有利于未来的可操作性和可维护性。 
> （图中表示，将业务逻辑的各个部分分割成单独模块）
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190515085535966.png)
> 那么如何将横切出来的模块融合到业务逻辑中呢？在Spring中使用的是动态代理。

# 代理模式
> 由于Spring中AOP是基于代理模式实现，所以有必要先了解一下代理模式。接传送门：[Java中的代理模式](https://github.com/Coder999z/Java-Notes/blob/master/docs/java/Java中的代理模式.md)

# SpringAOP的概念理解
> 通知类型：
>
> - `Before`：在方法被调用之前调用。
> - `Afterr`：在方法执行完成后调用，无论方法是否执行成功。
> - `After-Returning`：在方法执行成功return后调用。
> - `After-Throwing`：在方法抛出异常后调用
> - `Around`：在方法调用前和调用之后执行自定义代码。

> 概念
>
> - `切面（Aspect）`：切面是切点和通知的结合，一般单独作为一个类。通知和切点共同定义了关于切面的全部内容。
> - `连接点（JoinPoint）`：程序执行的某个特定位置（如：某个方法调用前、调用后，方法抛出异常后），Spring仅支持方法的连接点。通俗点说就是允许使用通知的地方。
> - `切点（PointCut）`：用于筛选连接点，为织入筛选对象。例如一个类中有10个连接点，切点定义了方法名为back开头的方法，那么将会筛选出以back开头的连接点。
> - `织入（Weaving）`：织入指的是将增强添加到目标类具体连接点上的过程。spring使用的是动态代理的方式实现了运行时织入。
> - `引介（Introduction）`：引介是一种特殊的增强，它为类添加一些属性和方法。这样，即使一个业务类原本没有实现某个接口，通过引介功能，可以动态的未该业务类添加接口的实现逻辑，让业务类成为这个接口的实现类。


# 示例
> Spring中有两种方式实现AOP，一个是XML配置风格，另一个是AspectJ注解风格。这里的例子是注解实现。
>
> 注：例子省略对spring环境的搭建

> 编写自定义注解类
>
> ```java
> //编写自定义注解类
> @Target(ElementType.METHOD)
> @Retention(RetentionPolicy.RUNTIME)
> @Documented
> public @interface SysLog {
>     String value() default "";
> }
> ```

> 解释一下注解：

> @Target 说明了注解可以修饰的范围
> （ElementType）取值有：
>
> ```
> 1. constructor：用于描述构造器。
> 2. field：用于描述域
> 3. local_variable：用于描述局部变量
> 4. method：用于描述方法
> 5. PACKAGE:用于描述包　　　　
> 6. PARAMETER:用于描述参数　　
> 7. TYPE:用于描述类、接口(包括注解类型) 或enum声明
> ```

> @Retention定义了Annotation被保留时间的长短
> （RetentionPolicy）取值有：
>
> ```
> 1.SOURCE:在源文件中有效（即源文件保留）　　　　
> 2.CLASS:在class文件中有效（即class保留）　　　　
> 3.RUNTIME:在运行时有效（即运行时保留）
> ```

> @Documented：用于描述其它类型的annotation应该被作为被标注的程序成员的公共API，因此可以被例如javadoc此类的工具文档化。

> 编写切面类

> ```java
> @Aspect
> @Component
> public class LogAspect {
> 
>     //指定切入点为这个注解类，当注解一个方法时，该方法就是一个切入点
>     @Pointcut("@annotation(com.example.test.annotation.SysLog)")
>     public void annocPointCut(){}
> 
>     @Before("annocPointCut()")
>     public void beforeAnnoc(JoinPoint point) {
>         System.out.println("before被执行");
>     }
> 
>     @AfterReturning("annocPointCut()")
>     public void afterReturnAnnoc(JoinPoint joinPoint,Object returnVal) {
>         System.out.println("afterReturning被执行");
>     }
> 
>     @After("annocPointCut()")
>     public void afterAnnoc(JoinPoint point) {
>         System.out.println("after被执行");
>     }
> 
>     @AfterThrowing("annocPointCut()")
>     public void exceptionAnnoc(Throwable e) {
>         System.out.println("AfterThrowing被执行");
>     }
> 
> 
>    @Around("annocPointCut()")
>     public void aroundAnnoc(ProceedingJoinPoint point) throws Throwable {
>         System.out.println("around：方法执行前");
>         point.proceed();
>         System.out.println("around：方法执行后");
>     }
> 
> }
> ```
>
> 

> 解释一下注解：
>
> - @Pointcut("@annotation(com.example.test.annotation.SysLog)")，定义一个切点，这里定义的是一个注解，当它注解一个方法时，该方法就是一个切入点。
> - @Before，该注解注解的方法将会在方法被执行之前执行。
> - @Around，该注解可以在方法执行前后都执行，代码中point.proceed();表示调用当前切入点方法，在point.proceed();之前的代码和@Before效果相同，在point.proceed();之后的代码即为切入点执行完毕后执行的代码。如果不写point.proceed();那么切入点将不会被执行。
> - @After，它注解的方法在切点执行完毕后一定执行。
> - @AfterReturning，它注解的方法将会在切入点方法正确执行后执行(报错就不执行了)。
> - @AfterThrowing，它注解发方法将会在切入点方法抛出异常后执行。

> 测试类：
>
> ```java
> public class AOPTest{
> 	@SysLog
> 	public void test(){
> 		System.out.print("run test!");
> 	}
> 	
> 	public static void main(String[] args) {
> 		new AOPTest().test();
> 	}
> }
> ```


# AOP的使用场景有哪些
> 根据AOP的理念分析，它是将分散到各个业务逻辑中的相同代码抽取出来进行封装复用，那么可能大量出现重复与核心业务逻辑无关代码的地方就是它的使用场景。
>
> 例如：日志系统，缓存系统，事务，权限控制，错误处理等。


# Spring AOP的实现原理
>  详细了解可以参考此博客：https://blog.csdn.net/c_unclezhang/article/details/78769426。
>
> 这里只写出大致流程。
>
> 1. `准备工作`： Spring启动容器时解析配置文件或扫描注解，加载切面类信息，映射成BeanDefinition对象，最后注册到Spring容器中。
> 2. Spring容器创建bean对象的过程中，在实例化Bean之前有一个专门的处理器，它会判断此bean是否需要被代理。（如果`无需代理`或者是`AOP的切面类`或者代理类的`拦截器链为null`则会直接返回）
>    （根据配置文件的beanName获取已经注册的切面类，查找切面类中能够应用到目标对象的拦截器集合并返回）
> 3. 判断返回的拦截器链是否为空，不为空则开始创建代理对象。
> 4. 判断代理对象是否实现接口，如果实现了则使用JDK中的动态代理，否则使用CGLIB生成代理对象。


# AOP有哪些可用的实现？
> 基于Java的主要AOP实现有：
>
> ```
> AspectJ
> Spring AOP
> JBoss AOP
> ```