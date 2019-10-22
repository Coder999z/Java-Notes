
* [占位符](#%E5%8D%A0%E4%BD%8D%E7%AC%A6)
  * [\#\{\}占位符](#%E5%8D%A0%E4%BD%8D%E7%AC%A6-1)
  * [$\{\}拼接符](#%E6%8B%BC%E6%8E%A5%E7%AC%A6)
  * [\#\{\}和$\{\}的区别](#%E5%92%8C%E7%9A%84%E5%8C%BA%E5%88%AB)
* [转义字符的处理](#%E8%BD%AC%E4%B9%89%E5%AD%97%E7%AC%A6%E7%9A%84%E5%A4%84%E7%90%86)
* [动态SQL](#%E5%8A%A8%E6%80%81sql)
  * [if](#if)
  * [choose](#choose)
  * [where](#where)
  * [trim](#trim)
  * [set](#set)
  * [foreach](#foreach)
* [关于主键返回](#%E5%85%B3%E4%BA%8E%E4%B8%BB%E9%94%AE%E8%BF%94%E5%9B%9E)
* [ResultType和ResultMap](#resulttype%E5%92%8Cresultmap)
  * [ResultType](#resulttype)
  * [ResultMap](#resultmap)
* [MyBatis的懒加载](#mybatis%E7%9A%84%E6%87%92%E5%8A%A0%E8%BD%BD)
* [别名](#%E5%88%AB%E5%90%8D)

# 占位符
> MyBatis中有两种占位符：#{}和${}。
>
> - #{}解析传递进来的参数数据
> - ${}对传递进来的参数原样拼接在SQL中

## #{}占位符
> `主要用来设置参数`，可以是基本数据类型，也可以是java bean。
>
> 如果传递的是`基本数据类型`，并且只有`一个`参数，那么#{}中的变量名可以任意命名，但是在多个参数时变量名必须与传入的一致才能正确获取，所以建议`统一按照传入的参数名来获取`。
>
> ```java
> User getUser(String name,int age);
> ```
>
> ```xml
> <select id="getUser" resultType="com.demo.bean.User">
>         select * from user where name=#{name} and age=#{age};
> </select>
> ```
>
> 如果传递的是pojo类型作为参数，那么此pojo类中必须提供get方法。#{}中的参数应该写的是属性名，如果有多个pojo类型，并且其中具有相同的属性名则写参数名点属性名。
>
> ```java
> public class Teacher {
>     String tname;
>     int tno;
> 	//省略get/set
> }
> 
> public interface TeacherMapper{
> 	@Select("select * from teacher where tno=#{tno}")
> 	Teacher getTeacherByAnnoction(Teacher teacher);
> }
> 
> //测试类
> public class DemoTest{
> 	@Autowired
> 	TeacherMapper mapper;
> 
> 	@Test
> 	public void test(){
> 		Teacher teacher =  new Teacher();
> 		teacher.setTno(2);
> 		mapper.getTeacherByAnnoction(teacher);
> 	}
> }
> ```
>
> 如果有多个pojo类作为参数，那么需要使用 参数名点属性名方式获取：
>
> ```java
> //多个student类
> public class Student {
>     int tno;
>     //省略get/set
> }
> 
> public interface TeacherMapper{
> 	@Select("select * from teacher where tno=#{teacher.tno}")
> 	Teacher getTeacherByAnnoction(Teacher teacher,Student student);
> }
> 
> //测试类
> public class DemoTest{
> 	@Autowired
> 	TeacherMapper mapper;
> 
> 	@Test
> 	public void test(){
> 		Teacher teacher =  new Teacher();
> 		teacher.setTno(2);
> 		Student student = new Student();
> 		student.setTno(1);
> 		mapper.getTeacherByAnnoction(teacher,student);
> 	}
> }
> 
> ```
>
> 当然，上面多参数的例子仅仅只是举例，并不推荐这样使用，如果有多种复杂参数的需求的话推荐使用Map进行传递。
>
> ```java
> public interface TeacherMapper{
> 	@Select("select * from teacher where tno=#{teacher.tno}")
> 	Teacher getTeacherByAnnoction(Map map);
> }
> 
> //测试类
> public class SpringbootApplicationTests {
> 
>     @Autowired
>     TeacherMapper mapper;
> 
>     @Test
>     public void contextLoads() {
>         Teacher teacher = new Teacher();
>         teacher.setTno(2);
>         
>         HashMap map = new HashMap();
>         map.put("teacher", teacher);
>         mapper.getTeacherByAnnoction(map);
>     }
> }
> ```
>
> 从例子中可以看出#{param}中的参数时HashMap中的key，如果是基本数据类型则直接取值，如果是pojo类则点属性就可以取得。

> **注意：**
> 动态表名，动态列名，动态排序不能使用#{}。它只能作为参数数据传递。以下例子中都是错误的写法：
>
> ```sql
> select * from #{table};
> select #{column} from table;
> select * from table order by #{column}
> select * from table where #{column}=#{param}
> ```
>
> 原因在于使用#{}相当于JDBC中的PreparedStatement预编译，编译后的参数使用占位符代替，而我们传入的参数只能作为参数执行了，表名列名在编译时已经固定。

## ${}拼接符
> ${}拼接符中传入的内容会被直接拼接在SQL语句中。
> 因此它可以用来动态得设置表名，列名，排序列名，或者动态地拼接SQL语句，但是它`有SQL注入的风险！`
>
> 
> 如果只有一个参数，并且是基本数据类型的话${}中变量名必须为value。
>
> ```java
> @Select(select * from teacher ${value})
> Teacher getTeacher(String sql);
> 
> @Test
> public void test(){
> 	mapper.getTeacher("where id=1");
> }
> 
> ```
>
> 上述例子中，传入的参数会直接与SQL语句拼成select * from teacher where id=1;
> 如果有多个参数，要求 $ {}中的参数名必须与传入的一致，或者使用Map传入，${}中的参数名必须与map中的Key一致。
>
> ```java
> public interface TeacherMapper{
> 	@Select(select ${param1} from ${param2})
> 	List<Teacher> getTeacher(String param1,String param2);
> 
> 	@Select(select ${param1} from ${param2})
> 	List<Teacher> getTeacherByMap(Map map);
> 
> }
> 
> @Test
> public void test(){
> 	//方式1
> 	mapper.getTeacher("*","teacher");
> 
> 	//方式2
> 	HashMap map = new HashMap();
> 	map.put("param1","*");
> 	map.put("param2","teacher");
> 	mapper.getTeacherByMap(map);
> 
> }
> ```
>
> 如果传入的是pojo类，用法和#{}相同，不做详细介绍。



## #{}和${}的区别
> - 主要区别在于功能上，一个传递参数一个拼接SQL。#{}是有预编译的更加安全，SQL语句较为固定的情况下效率更高。
>
> - 能使用#{}尽量不用${}因为它有SQL注入风险。需要拼接SQL语句可以在MyBatis的xml文件中设置动态SQL。


# 转义字符的处理
> 在MyBatis的配置文件中使用的是xml格式，如果需要用到大于小于会和标签的尖括号冲突。还有一些其他的转义字符。通常有三种处理方式：
>
> 1. **使用${}拼接：**（不推荐）
>
> ```xml
> 	<select id="getTeacher"  resultType="com.springboot.bean.Teacher">
>         select * from teacher where tno ${value} 2 limit 0,1;
>     </select>
> ```
>
> ```java
> public interface TeacherMapper {
>     Teacher getTeacherById(String value);
> }
> 
> @Test
>     public void contextLoads() {
>         mapper.getTeacherById("<");
>     }
> ```
>
> 2. 使用转义字符
>
> | 原字符 | 转义字符 | 含义   |
> | ------ | -------- | ------ |
> | <      | &lt;     | 小于   |
> | >      | &gt;     | 大于   |
> | &      | &amp;    | 与     |
> | '      | &apos;   | 单引号 |
> | "      | &quot;   | 双引号 |
>
> 需要注意的是`双引号不能漏
>
> ```xml
> 	<select id="getTeacher"  resultType="com.springboot.bean.Teacher">
> 		<!-- tno < 2 -->
>         select * from teacher where tno &lt; 2 limit 0,1;
>     </select>
> ```
>
> 3. 使用<![CDATA[ ]]>符号，在里面的符号将不被解析。
>
> ```xml
> 	<select id="getTeacherById"  resultType="com.springboot.bean.Teacher">
>         select * from teacher where <![CDATA[ tno < 2 ]]> limit 0,1;
>     </select>	
> ```

# 动态SQL
> 动态SQL也是MyBatis中的一大特色~

## if
> if和日常用的if判断逻辑相同。
>
> ```xml
> 	<select id="getTeacher"  resultType="com.springboot.bean.Teacher">
>         select * from teacher where status=1
>         <if test="course != null">
> 		and course=#{course}
> 		</if>
>     </select>
> ```
>
> 以上的例子中表示查询所有在岗的教师（status=1），如果传入的参数科目（course）不为null，那么查询执行科目在岗的教师。

## choose
> 类似于switch语句，在多个中选择符合条件的一个
>
> ```xml
> 	<select id="getTeacher"  resultType="com.springboot.bean.Teacher">
>         select * from teacher
>         <choose>
>     		<when test="id!= null">
>       			where id=#{id}
> 	   		</when>
> 	   		<when test="tname != null">
>       			where tname=#{tname }
> 	   		</when>
>         </choose>
>     </select>
> ```
>
> 以上例子中，如果传入的id不为null则根据id查，如果tname不为null则根据tname查。如果两个参数都不为null，则按照配置的顺序选择第一个命中的。

## where
> 先看一个例子：
>
> ```xml
> 	<select id="getTeacher"  resultType="com.springboot.bean.Teacher">
>         select * from teacher
>         where
>         <if test="id!= null">
> 			id=#{id}
> 		</if>
> 		<if test="tname != null">
> 			and tname=#{tname}
> 		</if>
>     </select>
> ```
>
> 如果出现第一个if条件不符合，那么语句为：
>
> ```sql
> select * from teacher where and tname=#{tname}
> ```
>
> 如果出现两个if条件都不符合，那么语句为：
>
> ```sql
> select * from teacher where
> ```
>
> 这两种情况都会导致抛出异常。MyBatis中提供了where标签用于解决这样的问题。
>
> ```xml
> 	<select id="getTeacher"  resultType="com.springboot.bean.Teacher">
>         select * from teacher
>         <where>
>         	<if test="id!= null">
> 				id=#{id}
> 			</if>
> 			<if test="tname != null">
> 				and tname=#{tname}
> 			</if>
> 		</where>
>     </select>
> ```
>
> 添加了where标签后，在标签内的语句如果没有一个返回SQL那么将自动去除where关键字。语句就变为了
>
> ```sql
> select * from teacher;
> ```
>
> 若有正确返回SQL，那么将会自动拼接where关键字。并且会自动删除头部的and/or。例如只有第一个if不符合条件时，语句会被拼接成：
>
> ```sql
> select * from teacher where tname=#{tname};
> ```

## trim
> trim标签用于添加或者去除SQL中多余的字符。
>
> | 属性               | 描述                                |
> | ------------------ | ----------------------------------- |
> | prefix             | 用于给SQL语句拼接前缀               |
> | suffix             | 用于给SQL语句拼接后缀               |
> | prefixesToOverride | 去除SQL语句头部的指定关键字或者字符 |
> | suffixesToOverride | 去除SQL语句尾部的指定关键字或者字符 |
>
> 实际上where标签也是个trim标签，它等价于：
>
> ```xml
> <trim prefix="where" prefixOverrides="AND |OR ">
> ....
> </trim>
> ```
>
> 例子：
>
> ```xml
> <insert id="insertUser" useGenerateKeys="true" keyProperty="id">
> 	insert into user
> 	(
> 		<if test="addr != null">
> 			addr,
> 		</if>
> 		<if test="phone!= null">
> 			phone
> 		</if>
> 	) values(省略)
> </insert>
> ```
>
> 在以上的例子中，如果phone == null ，那么SQL语句将会多出一个逗号变成：
>
> ```sql 
> insert into user(addr,) values(省略);
> ```
>
> 使用trim标签解决：
>
> ```xml
> <insert id="insertUser" useGenerateKeys="true" keyProperty="id">
> 	insert into user
> 	(
> 	<trim suffixesToOverride=",">
> 		<if test="addr != null">
> 			addr,
> 		</if>
> 		<if test="phone!= null">
> 			phone
> 		</if>
> 	</trim>
> 	) values(省略)
> </insert>
> ```
>
> 分析一下情况，如果两个if都返回true或者只有第二个if返回true，那么返回的SQL中没有逗号无需处理。如果只有第一个if返回了true，那么在句末会多出一个逗号，trim会为我们剔除。

## set
> 理解了where和trim后set也就非常的简单了。set等同于：
>
> ```xml
> <trim prefix="set" suffixOverrides=",">
> 	....
> </trim>
> ```
>
> 不再过多解释。
> 看个栗子：
>
> ```xml
> <update id="updateUser">
>   update user
>     <set>
>       <if test="username != null">username=#{username},</if>
>       <if test="password != null">password=#{password}</if>
>     </set>
>   where id=#{id}
> </update>
> ```

## foreach
>  顾明思议，它是用来遍历的。
>
> 1. 当遍历集合或者数组时
>
> ```xml
> <select id="selectUsers" resultType="arraylist">
>   SELECT *
>   FROM user
>   WHERE id in
>   <foreach item="item" index="index" collection="list"
>       open="(" separator="," close=")">
>         #{item}
>   </foreach>
> </select>
> ```
>
> 解释一下属性的含义：
>
> | 属性       | 含义                                           |
> | ---------- | ---------------------------------------------- |
> | collection | 指定传入的集合或者数组名称                     |
> | item       | 每次遍历出的元素                               |
> | index      | 索引                                           |
> | open       | 将该属性的值拼接到遍历出的字符串的开始         |
> | close      | 将该属性的值拼接到遍历出的字符串的末尾         |
> | separator  | 将该属性的值添加在遍历出来的每个值之间做分隔符 |
>
> 假设传入的数组为
>
> ```java
> int[] ids = new int[]{1,2,3};
> ```
>
> SQL语句为
>
> ```sql
> select * from user where id in (1,2,3)
> ```
>
> 2. 当遍历Map时，index为map的key，item为map的value。


# 关于主键返回
> 当我们在MySQL中配合了主键自增策略后，向表中添加数据要如何获得生成的主键呢。
> 有两种方法，这里只介绍推荐的一种。
>
> ```xml
> 	<insert id="insertTeacher" useGeneratedKeys="true" keyProperty="tno">
>         insert into teacher(tname) values(#{tname})
>     </insert>
> ```
>
> useGeneratedKeys="true" 表示主键自增
> keyProperty="tno" 表示将返回的主键赋值到bean中的哪个属性。同时bean中必须提供set方法。
>
> ```java
> public interface UserMapper{
> 	int insertTeacher(Teacher teacher);
> }
> 
> @Test
> public void test{
> 	Teacher teacher = new Teacher();
> 	teacher.setTname("tom");
> 	mapper.insertTeacher(teacher);
> 	//执行完毕后新添加的tom的主键就会被复制到teacher的tno属性中了
> 	int	id =  teacher.getTno();
> }
> ```

# ResultType和ResultMap
## ResultType
> - **概述：** 用于指定输出的结果类型，可以是基本数据类型也可以是bean，集合，map等。需要注意的是如果输出的为bean类型，那么表中的列名必须和bean中的属性名相同才能封装。
>   </br>
>
> - **去除下划线：**在数据库中的列名常用到下滑线分割单词，而Java bean的属性名是驼峰命名的，基于Springboot的项目可以在application.xml中配置一条属性，让MyBatis自动将下换线去除转成驼峰命名。
>
>   ```
>   mybatis.configuration.map-underscore-to-camel-case=true
>   ```
>
>   

## ResultMap
> **概述：** Result用于手动指定从返回结果中选择哪些属性封装到对应的哪个bean的哪个属性中。常用于返回的结果的属性名与bean中的属性名不同，或者一对多，多对多等关系中。
>
> - **示例1：** 用于返回的属性名与bean中属性名不同的映射
>
> ```xml
> <resultMap id="userResultMap" type="User">
>   <id property="id" column="user_id" />
>   <result property="username" column="user_name"/>
> </resultMap>
> 
> <select id="selectUsers" resultMap="userResultMap">
>   select user_id, user_name from user where id=#{id}
> </select>
> ```
>
> - **高级结果映射：**
>   </br>
>
> 1. **association**：关联，用于一对一关系的映射。
>    例：一个教师对应一个课程。
>
> ```java
> public class Teacher {
>     String tname;
>     int tno;
>     Course course;
>     //省略get、set
> }
> 
> public class Course {
>     int tno;
>     String cname;
>     int cno;
>     //省略get、set
> }
> ```
>
> 需求为根据id查询教师和它所教的课程
>
> ```xml
> <resultMap id="teacher" type="com.springboot.bean.Teacher" autoMapping="true">
>         <association property="course" javaType="com.springboot.bean.Course">
>             <id property="cno" column="cno"/>
>             <result property="cname" column="cname"/>
>             <result property="tno" column="tno"/>
>         </association>
> </resultMap>
> 
> <select id="getTeacher" resultMap="teacher">
>         select * from teacher t inner join course c on t.tno=1 and t.tno=c.cno
> </select>
> ```
>
> 2. **collection：** 用于一对多的映射
>
> ```java
> public class Teacher {
>     String tname;
>     int tno;
>     Course course;
>     List<Student> students;
> 	//省略get/set
> }
> ```
>
> 执行的逻辑为查询tno=1的教师所教的课程和他的学生。
>
> ```xml
> <resultMap id="teacher" type="com.springboot.bean.Teacher" >
>         <id property="tno" column="tno"/>
>         <result property="tname" column="tname"/>
>         <association property="course" javaType="com.springboot.bean.Course">
>             <id property="cno" column="cno"/>
>             <result property="cname" column="cname"/>
>         </association>
>         <!-- property配置teacher中的属性名
> 			ofType配置集合中的元素类型
> 			javaType配置集合的类型
> 		-->
>         <collection property="students" ofType="com.springboot.bean.Student" javaType="java.util.List">
>             <id property="sno" column="sno"/>
>             <result property="sname" column="sname"/>
>         </collection>
>     </resultMap>
> 
> 
>     <select id="getTeacher" resultMap="teacher">
>         select * from student a,sc b,teacher c,course d where c.tno=1 and c.tno=d.tno and d.cno = b.cno and b.sno=a.sno;
>     </select>
> ```
>
> 3. 关联的**内嵌select查询**
>    例子：
>
> ```xml
>     <resultMap id="teacher" type="com.springboot.bean.Teacher" >
>         <id property="tno" column="tno"/>
>         <result property="tname" column="tname"/>
>         
>         <association property="course" javaType="com.springboot.bean.Course" 
>         select="com.springboot.dao.TeacherMapper.getCourseByTno" column="tno">
>             <id property="cno" column="cno"/>
>             <result property="cname" column="cname"/>
>         </association>
>         
>         <collection property="students" ofType="com.springboot.bean.Student" javaType="java.util.List" 
>         select="com.springboot.dao.TeacherMapper.getStudentByCno" column="tno">
>             <id property="sno" column="sno"/>
>             <result property="sname" column="sname"/>
>         </collection>
>     </resultMap>
> 
>     <select id="getTeacher" resultMap="teacher">
>         select * from teacher where tno=#{tno}
>     </select>
> 
>     <select id="getCourseByTno" resultType="com.springboot.bean.Course">
>         select * from course where tno=#{tno}
>     </select>
>     <select id="getStudentByCno" resultType="com.springboot.bean.Student">
>         select * from student a,sc b,course c where c.tno=#{tno} and c.cno = b.cno and b.sno=a.sno;
>     </select>
> ```
>
>  例子的逻辑为指定教师的tno查询他教的课程和学生，在association和collection中添加了两个属性“select”和“column”。
>
> | 属性   | 用途                    |
> | ------ | ----------------------- |
> | select | 指定接口中执行SQL的方法 |
> | column | 指定传入参数的列        |
>
> 并且查询的语句从一条拆分成了三条。内嵌查询通常和`懒加载`配合使用，否则它的执行效率要低于联表查询。下文会详细介绍懒加载。
>
> - **1+N问题：** 当我们将语句拆分成多个单表查询语句后可能出现这样一种情况：例如用户表中有订单集合属性，订单集合中的每个订单又有详细信息属性，那么在查询时SQL语句执行的数量为：1（用户基本信息）+N（订单集合的数量）。说白了就是每条订单都需要单独的查询一次详细信息，那么将会带来很大的性能问题。
> - **如何解决？** ①懒加载能一定程度上缓解这个问题，但是需要注意的是避免快速触发懒加载。②将懒加载加到嵌套的查询中，还是这个例子，可以在查询订单详细信息上也加上懒加载，这样只有查看某一条的详细信息时才会加载。 ③内嵌select查询的粒度不要太细，就是说后面的N条查询转成联表查询。

# MyBatis的懒加载
> - **概述：**懒加载机制是指在需要使用时再进行加载，例如查询了一个User的基本信息，用户中有基本信息订单集合（order）等属性，如果我只需要基本信息，那么订单集合信息就暂时不查询，等到我需要获取时再去加载，这样能提高性能。`值得思考的是在前后端分离的趋势下，懒加载是否还有意义呢？`
> - **使用方式：**
>   springboot开启全局懒加载：
>
> ```java
> mybatis.configuration.lazy-loading-enabled=true
> mybatis.configuration.aggressive-lazy-loading=false
> ```
>
> 单独配置需要懒加载的功能：
> 例子与上一个相同，为指定教师的tno查询他教的课程和学生。
>
> ```xml
>     <resultMap id="teacher" type="com.springboot.bean.Teacher" >
>         <id property="tno" column="tno"/>
>         <result property="tname" column="tname"/>
>         <association property="course" javaType="com.springboot.bean.Course" fetchType="lazy"
>         select="com.springboot.dao.TeacherMapper.getCourseByTno" column="tno">
>             <id property="cno" column="cno"/>
>             <result property="cname" column="cname"/>
>         </association>
>         <collection property="students" ofType="com.springboot.bean.Student" javaType="java.util.List" fetchType="lazy"
>         select="com.springboot.dao.TeacherMapper.getStudentByCno" column="tno">
>             <id property="sno" column="sno"/>
>             <result property="sname" column="sname"/>
>         </collection>
>     </resultMap>
> 
> 	 <select id="getTeacher" resultMap="teacher">
>         select * from teacher where tno=#{tno}
>     </select>
> 
>     <select id="getCourseByTno" resultType="com.springboot.bean.Course">
>         select * from course where tno=#{tno}
>     </select>
>     <select id="getStudentByCno" resultType="com.springboot.bean.Student">
>         select * from student a,sc b,course c where c.tno=#{tno} and c.cno = b.cno and b.sno=a.sno;
>     </select>
> ```
>
> 多添加了一个属性为fetchType="lazy"，表示此关联的映射执行懒加载。
>
> ```java
> 	@Test
>     public void contextLoads() {
>         System.out.println("查询teacher");
>         Teacher teacher = mapper.getTeacher(1);
>         System.out.println();
>         Course course = teacher.getCourse();
>         System.out.println("查询teacher对应的course");
>     }
> ```
>
> 测试结果：
>
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190606200731912.png)可以发现在获取course时才执行了查询course的SQL


# 别名
> 在MyBatis中配置类型都需要全限定类名，但是使用基本数据类型和一些集合的写法却十分简单，这是因为MyBatis中为他们设定了默认的别名。
>
> 我们可以在mybatis-config.xml中配置别名，这样在mapper.xml中就可以直接使用别名了。
>
> ```xml
> <?xml version="1.0" encoding="UTF-8"?>
> <!DOCTYPE configuration
>         PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
>         "http://mybatis.org/dtd/mybatis-3-config.dtd">
> <configuration>
> 	
> 	<!--省略其他配置项 -->
> 	
>     <typeAliases>
>     	 <typeAlias alias="user" type="com.deom.bean.User"/> 
>     </typeAliases>
> 
> </configuration>
> 
> ```
>
> 如果类较多不像每个都配置怎么办，可以配置扫描某个路径下的@Alias("name")注解
>
> ```xml
> 	<typeAliases>
> 		<package name="com.demo.bean"/>
> 	</typeAliases>
> ```
>
> ```java
> @Alias("user")
> public class User{
> 	//省略
> }
> ```
>
> 在SpringBoot中更简单，进行如下配置以后bean目录下的所有类都会根据@Alias("name")注解设定别名
>
> ```properties
>   mybatis.type-aliases-package: com.demo.bean
> ```
>
> 

