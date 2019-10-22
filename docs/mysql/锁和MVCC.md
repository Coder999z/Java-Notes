
* [概述](#%E6%A6%82%E8%BF%B0)
* [InnoDB的MVCC](#innodb%E7%9A%84mvcc)
* [MVCC锁相关](#mvcc%E9%94%81%E7%9B%B8%E5%85%B3)
* [SQL语句的加锁分析](#sql%E8%AF%AD%E5%8F%A5%E7%9A%84%E5%8A%A0%E9%94%81%E5%88%86%E6%9E%90)
* [死锁](#%E6%AD%BB%E9%94%81)
* [模拟死锁](#%E6%A8%A1%E6%8B%9F%E6%AD%BB%E9%94%81)
* [参考](#%E5%8F%82%E8%80%83)


# 概述 

# InnoDB的MVCC
> - **概述：** MVCC（Multi-Version Concurrency Control）多版本并发控制，MVCC 是一种并发控制的方法，一般在数据库管理系统中，实现对数据库的并发访问，它在不同的数据库引擎中有不同的实现。`MySQL中MVCC只能在Repeatable Read（读可重复读）、Read Committed（读可提交）这两个隔离级别下工作。`
> - **用途：** MVCC实现的是`普通读取不加锁`，并且`读写不冲突`，根据28定律，通常大部分为读操作，避免了读操作的加锁可以大大`提高性能`。
> - **原理：** 
>
> MVCC是通过`保存了数据库某个时间的快照`来实现的。也就是说当几个事务`开启的时间不同`，可能会出现`同一时刻`、`不同事务`读取`同一张表`的`同一行`记录是不一样的。这个机制也是可重复读的实现。

> 先看一个例子：
>
> 在一个与MySQL的连接中启动事务，读取tno为1的教师姓名，结果为tom（还未commit）
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609094946380.png)
> 再启动第二个连接，将tno为1的教师名改成了jery
>
> ```sql
> begin;
> update teacher set tname="jery" where tno=1;
> commit;
> ```
>
> 此时，事务已经提交，我们再次从第一个连接的事务中查询tno为1的教师姓名
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609095421748.png)
> 结果依然为tom，并没有读取到最新修改的数据jery，原因就在于每个事务读取的都是`专有的快照`。

> 2. 在InnoDB引擎的数据库中，每一行记录后都有几个隐藏列来记录信息：
>
> 先了解一下两个概念：
> >**系统版本号：** 每当启动一个事务时，系统版本号会递增。
> >**事务版本号**  事务开始时的系统版本号作为该事务的版本号，事务的版本号用于在select操作中与记录的DATA_TRX_ID字段做对比。
>
> 隐藏列：
> > `DATA_TRX_ID：` 记录了某行记录的系统版本号，每当事务commit对该行的修改操作时就会将。
> > `DATA_ROLL_PTR：` 记录了此行记录的回滚记录指针，找之前的历史版本就是通过它。
> > `DELETE BIT：` 标记此记录是否正在有事务删除它，最后真正的删除操作是在事务commit后。
>
> 3. 增删改查中的MVCC操作：
> > `select：`①执行select操作时，InnoDB会查找到对应的数据行，并对比DATA_TRX_ID（版本号），要求数据行的版本必须`小于等于`事务的版本，如果当前数据行版本大于此事务版本，那么InnoDB会进入undo log中查找。`确保当前事务读取的是事务之前存在的，或者是由当前事务创建或修改的行`。 ② InnoDB会查找到对应的数据行后，查看DELETE BIT是否被定义，只允许未定义，或者删除的版本要大于此事务版本号。`保证在执行此事务之前还未被删除`。 **当且仅当这两个条件都成立才允许返回select结果！**
> > 
> > `insert：` InnoDB创建新记录，并以当前系统的版本号为新增记录的DATA_TRX_ID，如果需要回滚则丢弃undo log。
> > 
> > `delete：` InnoDB寻找到需要删除的记录，将此记录的DELETE BIT设置为`系统当前版本号`，若事务回滚则去除DELETE BIT定义的版本号，若事务提交则删除行。
> > 
> > `update：` InnoDB寻找到需要更新的行记录，复制了一条新的记录，新记录的版本ID为当前系统版本号，新记录的回滚指针指向原记录，将原记录的删除ID也设置为当前系统版本号。提交后则删除原记录，若回滚则删除复制的记录，并清除原记录的删除ID。

> 现在分析一下上一个例子：
> 假设当前tno=1的教师记录的DATA_TRX_ID = 2，那么第一个事务开启时系统版本号假设为3，在第一个事务中执行的查询操作只会读取DATA_TRX_ID <= 3的记录。此时第二个事务开启了，假设事务版本号为4，它执行了对该行数据的更新操作并提交了，新的记录中DATA_TRX_ID >= 4（期间可能还有其他事务的发送，使系统版本号增加）。

# MVCC锁相关
> - 在MVCC中，读操作可以分成：`快照读` (snapshot read)与`当前读` (current read)。
>
> **快照读：** 读取的是记录的可见版本，不加锁。
> **当前读：** 读取的是记录的最新版本，并且会对读取的记录加上锁（有共享和排他锁），确保其他事务不会并发地修改这条记录。
>
> >`快照读：`简单的select操作属于快照读，不会加锁。 select * from table where id=1;
> >
> >`当前读：`添加了关键字的特殊查询操作，或者update、delete、insert都属于当前读，需要加锁。这里的锁分为共享锁和排他锁（忘记概念了？[传送门](https://blog.csdn.net/weixin_43184769/article/details/89708302)）。
> >select * from table where ? lock in share mode;           
> >select * from table where ? for update;
> >insert into table values (…);
> >update table set ? where ?;
> >delete from table where ?;
> >以上语句中除了第一条是共享锁（`S锁`），其他都是排他锁（`X锁`）
>
> - **为什么增删改也是当前读？** 因为要进行增删改之前都得先找到符合条件的行，找的过程不就是读嘛~为了保证数据的线程安全性，需要对当前行进行加锁，有时也会出现锁表。
> - **lock in share mode**和**for update**有何区别？
>
> 1. 前者为记录添加的是`S锁`，后者添加的是`X锁`。共享锁和快照锁都`不会影响快照读`。
> 2. 根据S锁和X锁的规则，当记录中有S锁时，其他事务允许快照读，或再添加一个S锁，但是不允许添加X锁，必须等所有S锁都被释放以后才能上X锁。
> 3. 当记录中有X锁时，只允许快照读，不允许再添加X锁和S锁，直到该X锁释放（事务commit）。

# SQL语句的加锁分析
> 首先介绍InnoDB中的锁。

> **Record lock：** 给单挑索引的记录上锁，它锁的是索引而不是记录本身。如果没有指定主键索引，那么InnoDB会创建一个隐藏的主键索引，它本身是一个索引组织表。
> 
> **Gap lock：** 间隙锁，它是存在于某一条记录和前一条或者后一条之间间隙的锁，它只要是用于解决RR隔离级别下的幻读问题。举个例子：在b和a，b和c之间加入了间隙锁，那么b的前后相邻的位置都不能插入记录。
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609182515232.png)
>
> ```sql
> delete from t1 where id = 10;
> ```
>
> 1. 在id是主键+隔离级别RC。（RR相同）
>    ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609175109927.png)
>    `主键是唯一的，只需要在id=10的这条记录的主键上加X锁即可`
> 2. id是唯一索引+隔离级别RC。（RR相同）
>    ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609175249657.png)
>    关于索引的总结可以看我的另一篇博客，有助于理解：传送门
>
> 
>
> - 这里根据唯一索引找到索引表中的记录，再根据记录中的主键去寻找真正的数据行，加了两个锁分别在id=10的主键上和name=d的唯一索引上。
> - **为什么要两个列都加上锁？** 如果只给唯一索引上了锁，那么并发事务来了个where条件为name=d的update操作，那么此update并不知道该记录已经被delete操作锁定，违背了同一记录上的更新和删除操作`串行执行`的约束。
>
> 3. id是非唯一索引+隔离级别为RC
>    ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609180133917.png)
>    同理，非唯一索引可能搜索到的结果有好几个记录，那么对所有满足的记录都加上锁。主键和非唯一索引都会上锁。
> 4. id不是索引+隔离级别RC
>
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609180326626.png)
> 由于条件中的id不是索引，那么InnoDB将会根据主键进行全表的`遍历扫描`，所有的记录的主键都会被加上X锁，即便在MySQL中有相关的优化，它会判断每条记录是否满足条件，如果不满足则会释放锁，直到最后加锁的是符合条件的记录。但是仍然无法避免对不满足条件的主键的加锁、释放锁的步骤。
>
> 5. id是非唯一索引+隔离级别为RR
>    
>    先回顾一下隔离级别，RC中允许存在幻读和不可重复读，RR中解决了幻读和不可重复读，其中可重复读的实现是通过快照，幻读的解决则是通过MVCC。这个情况就是对幻读预防的原理。
>
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609183103899.png)
> 我们将例子中的SQL语句换为查询会更好理解：
>
> ```sql
> begin;
> select * from T1 where id=10 for update;
> commit;
> ```
>
> 如图所示，在X锁的基础上加入了gap锁，它将非唯一索引之间、之前、之后的间隙都锁定上了，这意味着在这一次事务commit之前，其他事务不能再插入id=10的记录，更不可能去删除。那么在这一次的事务中重复执行该当前读语句，只能读取到快照的版本或者该事务自身修改的记录，也就杜绝了幻读！
>
> 6. id不是索引+隔离级别RR
>
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609184426419.png)
> 这个的情况和RC的类似，只是更可怕了，除了全表的X锁还有全表的gap锁，虽然也有类似的优化机制，会主动释放与条件不符合的索引的锁，但是性能依然不可观。这也是我们写SQL语句时需要避免的情况。
>
> RR隔离级别是如何解决幻读的？
>
> > 通过gap锁，将可能重复的记录之间的间隙锁上，其他事务无法并发的往间隙中进行插入。通过X锁锁定索引，其他事务无法并发进行删除。通过读取快照，每次只能读取到在此事务之前的历史版本或此事务修改的数据，实现可重复读。


# 死锁
> 简单的表结构。
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609191729744.png)
> **情况一：**
> 
> 现有两个事务启动，T1和T2，对teacher表进行操作。顺序如图所示：
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609192520219.png)
> 执行到③时T1等待T2，执行到④时T2又会等待T1，互相等待就造成了死锁。
>
> 
>
> **情况二：**
> 
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/2019060919302023.png)
> 两个事务都只有一条SQL语句，但是仍然有可能造成死锁，原因在于事务对索引的加锁是`逐个加锁`。下面详细分析出现死锁时的情况：
>
> 1. session1的加锁顺序为（1，hdc，100），（6，hdc，10）。session的加锁顺序以此类推。
> 2. 那么在特定的情况下出现了如下的顺序：S1（1，hdc，100）--->  S2（6，hdc，10）----> S1（6，hdc，10）---> S2（1，hdc，100）。也就出现了死锁。
>
> 
>
> **简单总结：** 从上面的两个例子中可以发现，死锁的发生关键在于并发下事务加锁的顺序。
>
> # 模拟死锁
>
> 现在使用Springboot+Mybatis简单搭建环境操作数据库来模拟死锁。
> 
> teacher表结构：
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609200208809.png)
> TeacherMappper.xml
>
> ```xml
> 	<select id="getTeacherByLock" resultType="com.springboot.bean.Teacher">
>         select * from teacher where tno=#{tno} for update;
>     </select>
> 
>     <update id="updateTeacherByLock">
>         update teacher set tname=#{tname} where tno=#{tno};
>     </update>
> ```
>
> TeacherMapper.java接口中的方法：
>
> ```java
>     Teacher getTeacherByLock(int tno);
> 
>     Teacher updateTeacherByLock(Teacher teacher);
> ```
>
> TeacherService.java中模拟死锁的业务逻辑，这里为了方便调试起见写了两个方法：
>
> ```java
> @Service
> public class TeacherService {
>     @Autowired
>     TeacherMapper mapper;
> 
>     @Transactional
>     public void deadLock(){
>         //锁tno=1的
>         Teacher teacher = mapper.getTeacherByLock(1);
>         //更新tno=2的
>         Teacher teacher2 = new Teacher();
>         teacher2.setTno(2);
>         teacher2.setTname("aaa");
>         mapper.updateTeacher(teacher2);
>         System.out.println();
>     }
> 
>     @Transactional
>     public void deadLock2(){
> 
>         //锁tno=2的
>         Teacher teacher = mapper.getTeacherByLock(2);
> 
>         //更新tno=1的
>         Teacher teacher2 = new Teacher();
>         teacher2.setTno(1);
>         teacher2.setTname("bbb");
>         mapper.updateTeacher(teacher2);
>         System.out.println();
>     }
> 
> }
> 
> ```
>
> 测试类：
>
> ```java
> @Test
>     public void contextLoads() {
>         service.deadLock();
>     }
> 
> 
>     @Test
>     public void contextLoads2() {
>         service.deadLock2();
>     }
> ```
>
> 以断点调试的方式启动，断点打在service层的方法中
> ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609200555759.png)
>
> 分别debug启动两个test。调试顺序为：
>
> 1. test1执行锁tno=1的索引
> 2. test2执行锁tno=2的索引
> 3. test1执行更新tno=2的tname，此时会进入等待test2释放锁，超时时间可以设置。
> 4. test2执行更新tno=1的tname，此时会出现报错信息，同时test2事务回滚。
>    ![在这里插入图片描述](https://github.com/Coder999z/Java-Notes/blob/master/img/1/20190609202832883.png)在test2尝试为tno=1的索引加锁时，InnoDB检测到了死锁并回滚了事务。

# 参考

> https://www.cnblogs.com/crazylqy/p/7611069.html

> https://www.cnblogs.com/crazylqy/p/7611069.html