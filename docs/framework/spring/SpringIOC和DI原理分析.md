
* [概述](#%E6%A6%82%E8%BF%B0)
* [什么是IOC？](#%E4%BB%80%E4%B9%88%E6%98%AFioc)
* [什么是依赖注入（DI)](#%E4%BB%80%E4%B9%88%E6%98%AF%E4%BE%9D%E8%B5%96%E6%B3%A8%E5%85%A5di)
* [IOC和DI的联系](#ioc%E5%92%8Cdi%E7%9A%84%E8%81%94%E7%B3%BB)
* [几种依赖注入的方法](#%E5%87%A0%E7%A7%8D%E4%BE%9D%E8%B5%96%E6%B3%A8%E5%85%A5%E7%9A%84%E6%96%B9%E6%B3%95)
* [IOC/DI的原理](#iocdi%E7%9A%84%E5%8E%9F%E7%90%86)


# 概述
> - IOC（Inversion of control）即控制反转，指的是创建对象控制权的转移，以前创建对象都是在代码中直接new，而现在对象的创建交给了Spring容器。这么做有利于程序降低耦合，便于功能的扩展与复用，Spring的IOC常用的两种注入：构造器注入、setter方法注入。
> - DI（Dependency Injection）依赖注入，它是对IOC更简单的诠释。
> - IOC容器支持加载服务时的饿汉式初始化和懒加载。


# 什么是IOC？
>  **结论：** 就是将对对象的创建和管理交给Spring容器管理
>
> 看个简单的例子：

> ```java
> /**
> 	不使用Spring框架前，我们需要使用对象时就是直接
> 	new出来使用，如下代码所示，然后使用对象去完成我们
> 	的业务逻辑。
> */
> public void demo() {
> 	DemoService stu = new DemoServiceImpl();
> 	//省略业务逻辑
> 	.....
> }
> ```

> ```java
> /**
> 使用Spring后，在xml中配置了需要交给Spring容器管理的类student，
> 在实际使用时不需要由我们new，而是可以直接让Spring框架为我们代理生成。
> */
> 
> //application.xml文件中配置了类student
> ...
> <bean id="demoService" class="com.demo.service.DemoServiceImpl">
> </bean>
> ...
> 
> //Demo.class
> 
> public void demo() {
> 	ApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
> 	Student stu = context.getBean("demoService");
> 	//省略业务逻辑
> 	.....
> }
> ```

>  试想一下，如果这个时候要对业务代码进行扩展升级，假设需要修改此controller中的DemoService，而Java中有OCP原则（建议不要修改原代码，而是添加新的代码实现新功能，即对修改关闭，对扩展开放），那么通常的做法是保留久的DemoService，同时创建一个新的实现类进行新功能的扩展，那么此时controller中创建的实现类就需要改变，按照一般写法就需要修去修改原先写好的代码，这是不建议的做法。

> 那么如果使用的是SpringIOC，则只需要在配置文件中新修改原先Bean的配置就可以完成功能的扩展，而且不用动controller中的代码。很好的降低了耦合。

# 什么是依赖注入（DI)
> **结论：**   不用手动创建组件和服务，只需要在配置文件中配置好依赖关系，Spring容器会自动为你创建注入。


# IOC和DI的联系
> IOC是一个广泛的`概念`，而DI是一个具体的`操作`。IOC是指将对象的控制权反转给Spring容器，DI指Spring创建对象过程中将对象依赖属性通过配置注入，DI是IOC思想的具体实现。

# 几种依赖注入的方法
> `set注入，构造注入，接口注入，工厂注入（后面两个不常用）`

> Set注入 ：
> `可以注入普通属性也可以注入引用类型的对象，要求在被注入的类中必须有set方法，Spring在注入时实际上调用的是它的set方法。`

> ```java
> //xml配置文件：
> <bean id="userDao"  class="com.demo.dao.Impl"></bean>
> 
> <bean id=userService"  class="com.demo.service.impl">
> <property  name="userDao" ref="userDao"/>
> <property name="name" value="Tom"></property>
> </bean>
> 
> public class UserServiceImpl {
> 	UserDao userDao;
> 	String name;
> 	//必须有set方法才能注入成功
> 	public void setUserDao(UserDao userDao){
> 	   this.userDao=userDao;
> 	}
> 	
> 	public void setName(String name){
> 	   this.name=name;
> 	}
> 	//省略逻辑代码
> 	.......
> }
> ```

> 构造器注入：
> 实际上如果Bean标签中没有参数的话调用的是它的无参构造器注入。想要通过构造器注入属性就要求在被注入类中有相应参数的构造函数。

> ```java
> //配置文件
> <bean id="hopeSchool" class="com.demo.bean.School">
> </bean>
> 
> <bean id="tom" class="com.demo.bean.Persion">
> 	<constructor-arg name="name" value="tom"></constructor-arg>
> 	<constructor-arg name="height" value="195"></constructor-arg>
> 	<constructor-arg name="school" ref="hopeSchool"></constructor-arg>
> </bean>
> 
> //java类
> ...省略School类代码
> 
> public class Persion{
> 	String name;
> 	String heigh;
> 	School school;
> 	//省略get/set
> 	
> 	public Persion(String name,String heigh,School school){
> 		this.name = name;
> 		this.heigh = heigh;
> 		this.school = school;
> 	}
> }
> 
> ```

> 自动注入
> 自动注入也分为4种，无需自动装配，按照名称自动装配，按照类型自动装配，按照构造器自动装配。
>
> byName ：将与注入属性名相同的bean注入到属性中。

> ```java
> public class Student{
> 	public School school;
> }
> public class School{
> 	public String name;
> 	public String addr;
> }
> 
> <bean id="school" class="com.demo.School">
>     <property name="name" value="hopeSchool"></property>
>     <property name="addr" value="China"></property>
> </bean>
> 
> <bean id="student" class="com.demo.Student" autowire="byName"></bean>
> 
> ```
>
> 

> byType：将与注入属性class类型相同的bean注入到属性中，如果有多个相同类型的则会报错。

> ```java
> //java代码不变
> 
> <bean  class="com.demo.School">
>     <property name="name" value="hopeSchool"></property>
>     <property name="addr" value="China"></property>
> </bean>
> 
> <bean id="student" class="com.demo.Student" autowire="byType"></bean>
> ```
>
> 

> constructor：与被注入类的构造方法中相同类型的Bean将会被注入

> ```java
> public class Student{
> 	School school;
> 	public Student(School school){
> 		this.school = school;
> 	}
> }
> 
> public class School{
> 	public String name;
> 	public String addr;
> }
> 
> <bean id="student" class="com.demo.Student" autowire="constructor"></bean>
> ```

> @AutoWire
>
> @AutoWire自动装配策略：
> `此注解时Spring提供的`
> 它`默认`是按照`类型`来自动装配的，byType，但是如果同一个类型有多个bean，它会按照`byName`的规则来进行装配。如果没有找到Bean会报错，可以使用@AutoWire(require = false)解决。
> 也可以使用@qulifier指定名称，或者在某个Bean上加@Primary会在注入时优先选择它。

> @Resource
>
> 此注解时JDK提供的`
> @Resource默认按照byName注入，它有两个可配置属性name和type。在指定了name后将使用byName注入策略，使用type属性后将使用byType的注入策略，如果既不指定name也不指定type则默认使用的是byName。

> @AutoWire和@Resource的异同点？
>
> - 共同点：两者都可以注解在字段和set方法上，如果注解在字段上则不需要再写set方法。
> - 不同点：@AutoWire默认使用的是byType装配对象，@Resource默认使用的是byName装配对象

# IOC/DI的原理
> 由于整个Spring源码非常复杂，能力有限，只简单阅读了其他大神的源码解析，做出的简单总结。


![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190513154317973.png)

> Spring IOC初始化的过程大致为：
>
> 1. I/O流读取了XML文件
> 2. 解析XML文件并将每个Bean映射成BeanDefinition对象
> 3. 将BeanDefinition对象“注册”到BeanFactory中（注册的实质是将每个BeanDefinition作为value，key为beanName，存入了HashMap中）。
> 4. BeanFactory在需要创建Bean时，读取BeanDefinition对象中的属性，根据Java反射机制进行实例化对象，注入属性。