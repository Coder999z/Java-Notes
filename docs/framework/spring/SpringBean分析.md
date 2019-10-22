
* [Spring Bean的生命周期](#spring-bean%E7%9A%84%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F)
* [SpringBean作用域](#springbean%E4%BD%9C%E7%94%A8%E5%9F%9F)
* [SpringBeans与线程安全](#springbeans%E4%B8%8E%E7%BA%BF%E7%A8%8B%E5%AE%89%E5%85%A8)
* [参考](#%E5%8F%82%E8%80%83)

# Spring Bean的生命周期

> 1. `实例化Bean`：当客户向容器请求一个尚未初始化的bean时，BeanFactory会获取该Bean的定义信息（BeanFactory启动时已经将xml读取将其映射成对象存在HashMap中）并使用反射调用其空参构造实例化出对象。
>
> 2. 属性注入：使用DI，按照Bean定义信息中配置的属性进行注入。
>
> 3. 处理Aware接口：Spring会检查该对象是否实现了aware系列的接口，并将相关的信息注入给Bean。
>
>    > **简单科普一下Aware接口：**
>    > ①如果这个Bean已经实现了BeanNameAware接口，会调用它实现的setBeanName(String beanId)方法，此处传递的就是Spring配置文件中Bean的id值；
>    > ②如果这个Bean已经实现了BeanFactoryAware接口，会调用它实现的setBeanFactory()方法，传递的是Spring工厂自身。
>    > ③如果这个Bean已经实现了ApplicationContextAware接口，会调用setApplicationContext(ApplicationContext)方法，传入Spring上下文；
>    > 也就是说在初始化时会注入Bean在Spring容器中的一些属性信息。
>
> 4. processBeforeInitialization()：检测是否实现BeanPostProcessor接口的processBeforeInitialization方法，如果有则执行。注意它是初始化结束第一个执行的方法。
>
> 5. afterPropertiesSet()：检测是否实现InitializingBean接口的afterPropertiesSet方法，如果有则执行。
>
> 6. init-method：检查Bean定义文件中的init-method配置，如果配置了它则执行配置的方法。
>
> 7. processAfterInitialization()：检测是否实现BeanPostProcessor接口的processAfterInitialization方法，如果有则执行。
>    ` 初始化完毕进入使用`
>
> 8. destroy()：在容器关闭时，如果Bean有实现DisposableBean接口的destroy方法，则执行他的destroy方法。
>
> 9. destroy-method：如果在Bean定义文件中定义了destroy-method，则执行配置的方法。

>![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190512142339115.png)
>看到了一个比较好的总结：
>![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/2019051511411494.png)

# SpringBean作用域 	
> - singleton：默认为单例每个容器中只有唯一的一个bean实例，每次请求返回的都是同一个Bean实例
> - prototype：多例，每次从容器中获取Bean时都会返回一个新的实例。
> - request：为每一个HTTP Request创建一个实例，在请求完成以后bean就会失效。
> - session：与request类似，每个HTTP Session中都有一个bean实例，session过期后bean也会失效
> - global-session：同一个全局的Session共享一个Bean

# SpringBeans与线程安全

> Spring框架并没有对单例bean进行任何多线程的封装处理。关于单例bean的线程安全和并发问题需要开发者自行去搞定。但实际上，大部分的Spring bean并没有可变的状态(比如Serview类和DAO类)，所以在某种程度上说Spring的单例bean是线程安全的。如果你的bean有多种状态的话（比如 View Model 对象），就需要自行保证线程安全。

> 最浅显的解决办法就是将多态bean的作用域由“singleton”变更为“prototype”



# 参考

> https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247484247&idx=1&sn=e228e29e344559e469ac3ecfa9715217&chksm=ebd74256dca0cb40059f3f627fc9450f916c1e1b39ba741842d91774f5bb7f518063e5acf5a0#rd

> 牛客网