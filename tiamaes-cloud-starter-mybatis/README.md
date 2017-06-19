# tiamaes-cloud-starter-mybatis
# 1.Overview
该项目是对spring 和mybatis的基础整合，并进行了一部分的自动配置和改动，以方便集成到我们自己的项目中去，详细用法请参考官方文档。 
# 2.Quick Start
##### 1.Dependencies
	<dependencies>
		<dependency>
			<groupId>com.tiamaes.cloud</groupId>
			<artifactId>tiamaes-cloud-starter-mybatis</artifactId>
		</dependency>
	</dependencies>
##### 2.Configure
	spring:
	  datasource:
	    url: jdbc:mysql://localhost:3306/mysql?useUnicode=true&characterEncoding=utf8
	    username: username
	    password: password
	    driver-class-name: com.mysql.jdbc.Driver
##### 3.code
######3.1 com.tiamaes.cloud.XXX.persistence.UserMapper.java

	public interface UserMapper {
		User loadUserDetailByUsername(@Param("username")String username);
	}
######3.2 com.tiamaes.cloud.XXX.persistence.UserMapper.xml

	<?xml version="1.0" encoding="UTF-8"?>
	<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
	
	<mapper namespace="com.tiamaes.cloud.XXX.persistence.UserMapper">
		<select id="loadUserDetailByUsername" parameterType="String" resultType="com.tiamaes.bike.common.bean.system.User">
		<![CDATA[
			SELECT T.id, T.username, T.password, T.nickname, T.mobile, T.email, T.createDate   
			FROM TM_USERS T WHERE T.username = #{username}
		]]>
	</mapper>
######3.3 com.tiamaes.cloud.Application.java

	@SpringBootApplication
	@MapperScan("com.tiamaes.cloud.**.persistence")
	public class Application{
	
		public static void main(String[] args) {
			SpringApplication.run(Application.class, args);
		}
	}
# 3.Usage
#####1.注意事项
######1.1	 Mapper.java和Mapper.xml需要在同一目录下，eg: persistence,Mapper.java在src/main/java中，Mapper.xml在src/main/resources.否则当打包时会出现异常信息。
######1.2 @MapperScan注解需要添加到项目的根目录下的配置类上，eg: com.tiamaes.cloud.Application。因为spring boot启动时会扫描 @SpringBootApplication注解类的目录及子目录下的文件，如果业务类不在这些目录中，则不会被扫描到，导致接口无法注入到项目中。

#####2.使用分页
项目中默认已经集成了分页插件，不需要刻意去写分页的SQL语句，只需要写查询全部的SQL语句。在需要分页的地方，在调用Mapper接口之前调用以下代码：  

eg:	已经完成Mapper接口与对应的SQL语句
######2.1 com.tiamaes.cloud.XXX.persistence.AlarmMapper.java
	public interface AlarmMapper {
		AlarmRecord getListOfAlarmRecords(Parameters parameters);
	}
######2.2 com.tiamaes.cloud.XXX.persistence.AlarmMapper.xml
	<?xml version="1.0" encoding="UTF-8"?>
	<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
	<mapper namespace="com.tiamaes.cloud.XXX.persistence.AlarmMapper">
		<select id="getListOfAlarmRecords" parameterType="com.tiamaes.cloud.Parameters" resultType="com.tiamaes.XXX.bean.AlarmRecord">
			SELECT t.id,
			       t.vehicleId,
			       t.vehicleName,
			       t.simNo,
			       t.warnCode,
			       t.warnContent,
			       t.directionType,
			       t.createDate,
			       t.lng,
			       t.lat
			  FROM TB_WARNING_INFO t
			ORDER BY t.createDate DESC 
		</select>
	</mapper>
######2.3 直接调用接口时，查询数据库中所有记录，需要分页时只需要在之前调用以下语句：
	@Service
	public class AlarmService implements AlarmServiceInterface {
		@Resource
		private AlarmMapper alarmMapper;
		
		@Override
		@Transactional(propagation = Propagation.NOT_SUPPORTED)
		public List<AlarmRecord> getListOfAlarmRecords(Parameters<AlarmRecord> parameters, Pagination<AlarmRecord> pagination) {
			Assert.notNull(pagination, "分页对象不能为空");
			PageHelper.startPage(pagination);	//分页 
			return alarmQueryMapper.getListOfAlarmRecords(parameters);
		}
	}
	
	@RestController
	@RequestMapping("/alarm")
	public class AlarmController {
		@Resource
		private AlarmServiceInterface alarmService;
		
		@RequestMapping(value = {"/page/{number:\\d+}","/page/{number:\\d+}/{pageSize:\\d+}"}, method = { RequestMethod.GET, RequestMethod.POST }, produces = {"application/json" })
		public @ResponseBody PageInfo<AlarmRecord> page(@RequestBody AlarmRecord alarmRecord, @PathVariable Map<String,String> pathVariable,@CurrentUser User operator) {
			int number = pathVariable.get("number") == null ? 1 : Integer.parseInt(pathVariable.get("number"));		//当前页码
			int pageSize = pathVariable.get("pageSize") == null ? 20 : Integer.parseInt(pathVariable.get("pageSize"));	//每页条数
			Pagination<AlarmRecord> pagination = new Pagination<>(number, pageSize);						//根据页码和每页条数生成分页对象
			Parameters<AlarmRecord> parameters = new Parameters<>();
			parameters.setUser(operator);
			parameters.setTarget(alarmRecord);
			
			List<AlarmRecord> list = alarmQueryService.getListOfAlarmRecords(parameters, pagination);
			return new PageInfo<>(list);
		}
		
	}
# 4.Example
[https://github.com/mattmok/spring-cloud-example-parent/tree/master/spring-cloud-example-mybatis](https://github.com/mattmok/spring-cloud-example-parent/tree/master/spring-cloud-example-mybatis)

# 5.More
1. [http://www.mybatis.org/mybatis-3/zh/](http://www.mybatis.org/mybatis-3/zh/)
2. [https://github.com/mybatis/spring](https://github.com/mybatis/spring)
3. [http://www.codingpedia.org/ama/spring-mybatis-integration-example/](http://www.codingpedia.org/ama/spring-mybatis-integration-example/)
4. [http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)


# 6.Authors
chenlili@tiamaes.com
# 7.License
Tiamaes cloud project is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
