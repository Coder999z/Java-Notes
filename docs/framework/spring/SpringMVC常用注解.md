

* [@RestController](#restcontroller)
* [@ResponseBody](#responsebody)
* [@RequestBody](#requestbody)
* [@RequestMapping](#requestmapping)
  * [快捷使用的方式](#%E5%BF%AB%E6%8D%B7%E4%BD%BF%E7%94%A8%E7%9A%84%E6%96%B9%E5%BC%8F)
* [@RequestParam](#requestparam)
* [@PathViriable](#pathviriable)
* [@CookieValue](#cookievalue)

> 天天在项目中用的注解，今天来研究一下它们。建议先理解[SpringMVC原理篇](https://github.com/Coder999z/Java-Notes/blob/master/docs/framework/spring/SpringMVC工作原理.md)

@Controller

> **注解位置：** 标注在Controller类上
> **作用：** 将注解的类交给SpringIOC容器管理，并指明该实例是一个Controller。
> **参数：**
>
> ```java
> // 指定Spring Bean Name
> @Controller(value = "")
> ```
>
> **原理：** 在SpringIOC容器启动`初始化`单例时能够根据此注解扫描到此类，初始化后会对Controller进行URL`映射`。
> **验证：**如果将Controller类的注解换成@Component，启动时不会报错，但是在浏览器中输入路径时会出现404，说明Spring没有对所有的bean进行URL映射。

# @RestController
> **注解位置：** 标注在Controller类上
>
> **参数：** 同@Controller
>
> **作用：** 同@Controller，只是将请求返回的数据格式统一为JSON。相当于@Controller + @ResponseBody
>
> **原理：** 结合@Controller和@ResponseBody理解。

# @ResponseBody
> **注解位置：** 标注在类上或者方法上
>
> **参数：** 无
>
> **作用：** 将标注的方法的返回值转换为指定格式直接返回。如果标注在Controller类上，则表示此类中所有方法返回值都会被处理。
>
> **原理：** 在controller方法返回后，会通过`转换器`（HttpMessageConverter的合适实现类）将返回的对象转换成指定的格式（JSON、XML）`放入Response Body中`，并且在处理完以后`不会再执行视图处理器`，而是直接将数据写入输出流中。
>
> **注意：** 如果返回对象,按utf-8编码。如果返回String，默认按iso8859-1编码，页面可能出现乱码。因此在注解中我们可以手动修改编码格式，例如@RequestMapping(value="/cat/query",produces="text/html;charset=utf-8")


# @RequestBody
> **注解位置：**标注在方法的形参上
> **参数：**
>
> ```java
> // required 表示此参数是否为必须的，默认为true，如果设置为false那么在没有收到此参数时不会报错
> @RequestBody(required = true)
> ```
>
> **作用：** 将Request Body中的指定格式数据解析后封装到形参对象中。
>
> **原理：** SpringMVC处理请求中有一个步骤是给需要调用的Controller中的指定方法的形参赋值，在这一步中会反射获取形参上是否有注解，如果有此注解则会调用`转换器`（HttpMessageConverter的合适实现类）解析Request Body中的数据解析后进行封装。
>
> **注意：** 从Request体内获取参数，那么就一定是Post请求。

# @RequestMapping
> **注解位置：** 标注在类或者方法上
> **作用：** 用于映射请求的路径与对应的方法。
> **参数：**
>
> ```java
> 	// 指定请求的地址，可以配置多个地址对应一个方法
> 	@AliasFor("path")
>     String[] value() default {};
> 	//同 value
>     @AliasFor("value")
>     String[] path() default {};
> 
> 	// 指定Http请求方法（GET、POST、PUT等），满足才处理
>     RequestMethod[] method() default {};
> 
> 	// 指定Request中必须包含的参数，可以由多个，可以加判断（age != 10），满足才处理
>     String[] params() default {};
> 	
> 	// 指定request的header中必须包含指定值才能处理请求。
> 	// 例如防盗链，token等
>     String[] headers() default {};
> 
> 	// 指定request  提交的内容  的类型。满足才处理
> 	// 例如：consumes="application/json"
>     String[] consumes() default {};
> 	// 指定返回内容的类型，仅当request请求头中的(Accept)类型中包含该指定类型才返回。
>     String[] produces() default {};
> ```
>
> **原理：**  
>
> 1. 在SpringIOC容器初始化完毕后对Controller与URL进行映射的阶段，获取URL有两种方式，其一就是根据注解，通过反射机制获取注解中的值然后进行处理。
> 2. SpringMVC处理请求中有一个步骤是处理适配器寻找指定的处理方法，其中的寻找方式正是根据请求的URL与@RequestMapping中配置的路径进行比对，获取相应方法执行。

## 快捷使用的方式

>```
>@GetMapping
>@PostMapping
>@PutMapping
>@DeleteMapping
>@PatchMapping
>```
>
>这5个注解只默认指定了请求的方法，还需配置路径，其他的参数和@RequestMapping相同。


# @RequestParam
> **注解位置：** 标注在方法的形参上
> **作用：** 用于获取传入参数的值。
> **参数：** 
>
> ```java
> 	// 参数的名称，与request中的参数同名。
>     @AliasFor("name")
>     String value() default "";
> 	// 是否为必须的参数
>     boolean required() default true;
> 	// 设置此参数的默认值。
>     String defaultValue() default 
> ```
>
> **原理：** SpringMVC处理请求中有一个步骤是给需要调用的Controller中的指定方法的形参赋值，通过反射获得形参上的注解，如果有@RequestParam，则根据注解中的value获取指定参数进行封装。

# @PathViriable
> **注解位置：** 标注在方法的形参上
> **作用：** 用于获取定义在路径中的参数值。
> **参数：**  value：指定参数名
> **使用示例：**
> 栗子中定义了路径中的参数名为name，配合@PathViriable注解来获取参数。
>
> ```java
> 	@RequestMapping("/{name}/demo.action")
>     public String demo(@PathVariable(value = "name") String name){
>         return "index";
>     }
> ```
>
> **原理：** SpringMVC处理请求中有一个步骤是给需要调用的Controller中的指定方法的形参赋值，通过反射获得形参上的注解，如果有@PathViriable注解则会根据注解去从URL中获取值。

# @CookieValue
> **注解位置：** 标注在方法的形参上
> **作用：** 用于获取request中的cookie值作为形参数值。
> **参数：**
>
> ```java
> @AliasFor("name")
> 	// 对应cookie的key
>     String value() default "";
>     
>     boolean required() default true;
> 
>     String defaultValue() default;
> ```
>
> **原理：** SpringMVC处理请求中有一个步骤是给需要调用的Controller中的指定方法的形参赋值，通过反射获得形参上的注解，如果有@CookieValue则会根据注解的参数value作为Key，从request带来的cookie中去获取value，作为形参的值。