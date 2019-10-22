
* [组件介绍](#%E7%BB%84%E4%BB%B6%E4%BB%8B%E7%BB%8D)
  * [DispatcherServlet（核心控制器）](#dispatcherservlet%E6%A0%B8%E5%BF%83%E6%8E%A7%E5%88%B6%E5%99%A8)
  * [HandlerMapping（处理映射器）](#handlermapping%E5%A4%84%E7%90%86%E6%98%A0%E5%B0%84%E5%99%A8)
  * [HandlerAdapter（处理适配器）](#handleradapter%E5%A4%84%E7%90%86%E9%80%82%E9%85%8D%E5%99%A8)
  * [Handler（Controller）](#handlercontroller)
  * [View resolver（视图解析器）](#view-resolver%E8%A7%86%E5%9B%BE%E8%A7%A3%E6%9E%90%E5%99%A8)
  * [View（视图）](#view%E8%A7%86%E5%9B%BE)
* [SpringMVC工作原理](#springmvc%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86)
  * [准备工作](#%E5%87%86%E5%A4%87%E5%B7%A5%E4%BD%9C)
  * [一个Request来了](#%E4%B8%80%E4%B8%AArequest%E6%9D%A5%E4%BA%86)

![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190520094834235.png)

# 组件介绍
## DispatcherServlet（核心控制器）
> **作用：** 它是SpringMVC的核心，所有的请求第一步都会先到达这里，再由它调用其他组件处理。我们在web.xml中配置的核心Servlet就是它。


## HandlerMapping（处理映射器）
> **作用：** 用于查找请求的URL对应的Handler（Controller），SpringMVC中针对配置文件方式、注解方式等提供了不同的映射器来处理。

##  HandlerAdapter（处理适配器）
> **作用：** 根据映射器中找到的Handler，通过HandlerAdapter去执行Handler，这是适配器模式的应用。

## Handler（Controller）
> `Handler需要工程师开发`，并且必须遵守Controller开发的规则进行开发，这样适配器才能正确的执行。例如实现Controller接口，将Controller注册到SpringIOC容器中等。

## View resolver（视图解析器）
> **作用：** 将Handler中返回的逻辑视图（ModelAndView）解析为一个具体的视图（View）对象。

## View（视图）
> **作用：** View最后对页面进行渲染将结果返回给用户。springmvc框架提供了很多的View视图类型，包括：jstlView、freemarkerView、pdfView等

# SpringMVC工作原理
## 准备工作
> 在Spring容器`初始化`时会建立所有的URL和Controller的对应关系，保存到Map<URL,Controller>中，这样request就能快速根据URL定位到Controller。

---

**实现：** 

> 1. 在SpringIOC容器初始化完所有单例bean后。
>
> 2. SpringMVC会遍历所有的bean，获取controller中对应的URL（这里获取URL的实现类有多个，用于处理不同形式配置的Controller）
>
> 3. 然后将每一个URL对应一个controller存入Map<URL,Controller>中。
>
>    
>
>    `这一步的作用在于，为HandlerMapping组件建立所有URL和controller的对应关系。`

## 一个Request来了
> 1. **监听端口，获得请求**： Tomcat监听到了8080端口的请求，进行了接收、解析、封装，根据路径调用了web.xml中配置的核心控制器DispatcherServlet。
> 2. **获取Handler**： 进入DispatcherServlet，核心控制器调用HandlerMapping去根据请求的URL获取对应的Handler。准备工作就是为了它。这里有个细节，如果获取的Handler为null则返回404。
> 3. **调用适配器执行Handler** ：
> 4. 适配器中根据request的URL去Handler中寻找对应的处理方法（Controller的URL与方法的URL拼接后对比）
> 5. 获取到对应方法后，需要将request中的参数与方法参数上的数据进行绑定。（根据反射获取方法的参数名和注解，再根据注解或者根据参数名对照进行绑定）（这里的绑定比较抽象，我的理解是找到所需的对应参数，然后再反射调用方法时传入）
> 6. 绑定完参数后，反射调用方法获取ModelAndView（如果Handler中返回的是String、View等对象，SpringMVC也会将它们重新封装成一个ModelAndView）
> 7. **调用视图解析器解析** 将ModelAndView解析成View对象
> 8. **渲染视图**  将View对象中的返回地址，参数信息等放入RequestDispatcher，最后进行转发。