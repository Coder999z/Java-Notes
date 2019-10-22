
* [Maven](#maven)
* [application\.xml](#applicationxml)
* [主类配置](#%E4%B8%BB%E7%B1%BB%E9%85%8D%E7%BD%AE)
* [配置类](#%E9%85%8D%E7%BD%AE%E7%B1%BB)

> 实习中接到了新的需求，考虑到适配性，可能会涉及到不同访问方对应不同的数据源，本篇主要叙述MyBatis多数据源的配置方法和其中遇到的坑。

> **环境：**
> jdk1.8
> MyBatis2.1.0

## Maven
> ```xml
> 		<dependency>
>             <groupId>org.mybatis.spring.boot</groupId>
>             <artifactId>mybatis-spring-boot-starter</artifactId>
>             <version>2.1.0</version>
>         </dependency>
> ```

## application.xml
> 这里的坑就多了

> ```properties
> mybatis.mapper-locations= classpath:mapper/*.xml
> 
> #数据源1
> spring.datasource.vip-local.jdbc-url=jdbc:oracle:thin:@192.168.16.1:1521:orcl
> spring.datasource.vip-local.username= test2
> spring.datasource.vip-local.password= test2
> spring.datasource.vip-local.driver-class-name= oracle.jdbc.driver.OracleDriver
> spring.datasource.vip-local.hikari.maximum-pool-size= 10
> 
> #数据源2
> spring.datasource.vip-docker.jdbc-url=jdbc:oracle:thin:@192.168.16.12:49161:xe
> spring.datasource.vip-docker.username= dev
> spring.datasource.vip-docker.password= dev
> spring.datasource.vip-docker.driver-class-name= oracle.jdbc.driver.OracleDriver
> spring.datasource.vip-docker.hikari.maximum-pool-size= 10
> ```
>
> 1. `classpath不能漏，在Idea中，mapper配置文件的文件夹必须在resource路径下`
> 2. 在单数据源时用户名的配置为：
>    `spring.datasource.username= test2`
>    多数据源下需要区分数据源，我们假设数据源名称为vip-local，那么就配置成如下：
>    `spring.datasource.vip-local.username= test2`
>    规律自寻

## 主类配置
> 配置不自动配置数据源，这步不能少。exclude = {DataSourceAutoConfiguration.class}。
> 配置mapper接口扫描路径
>
> ```java
> @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
> @MapperScan("com.xxx.xxx.xxx.dao")
> public class VipApplication {
>     public static void main(String[] args) {
>         SpringApplication.run(VipApplication.class, args);
>     }
> 
> }
> ```

## 配置类
>  现在需要手动配置数据源，这里可以分成两个类来写，这样方便指定哪些mapper使用哪个数据源，也可以写在同一个类中。
>
> ```java
> @Configuration
> public class DataSourceConfig {
> 
>     @Bean(name = "docker-datasource")
>     //此配置的是配置文件中的前缀，一个前缀对应一个数据源 
>     @ConfigurationProperties(prefix = "spring.datasource.vip-docker")
>     public DataSource dataSource2(){
>         return DataSourceBuilder.create().build();
>     }
> 
>     @Bean(name = "vip-docker")
>     // @Qualifier需要手动指定DataSource，否则配置了多数据源会报错不知道选哪个。
>     public SqlSessionFactory dsSqlSessionFactory2(@Qualifier("docker-datasource") DataSource datasource) throws Exception {
>         SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
>         factoryBean.setDataSource(datasource);
>         // 每个数据源需要指定xml文件路径，否则调用接口时会报错。
>         factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
>         SqlSessionFactory factoryObj = factoryBean.getObject();
>         return factoryObj;
>     }
> 
> 
>     @Bean(name = "local-datasource")
>     @Primary
>     @ConfigurationProperties(prefix = "spring.datasource.vip-local")
>     public DataSource getDateSource1() {
>         return DataSourceBuilder.create().build();
>     }
> 
>     @Bean(name = "vip-local")
>     @Primary
>     public SqlSessionFactory test1SqlSessionFactory(@Qualifier("local-datasource") DataSource datasource)
>             throws Exception {
>         SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
>         bean.setDataSource(datasource);
>         //多数据源需要手动配置配置文件
>         bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
>         SqlSessionFactory factory = bean.getObject();
>         return factory;
>     }
> }
> ```



> 至此基本的配置已经完成了。多数据源的用途很广，可以为`不同接口配置不同的数据源`，可以在程序`结合AOP实现数据库的读写分离`，作为接口提供服务时，可以根据接入方提供不同的数据源访问，十分方便。