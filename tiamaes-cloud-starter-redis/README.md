# tiamaes-cloud-starter-redis
# 1.Overview
该项目是对spring boot和redis的基础整合，并进行了一部分的自动配置和改动，以方便集成到我们自己的项目中去，详细用法请参考官方文档。 
# 2.Quick Start
##### 1.Dependencies
	<dependencies>
		<dependency>
			<groupId>com.tiamaes.cloud</groupId>
			<artifactId>tiamaes-cloud-starter-redis</artifactId>
		</dependency>
	</dependencies>
##### 2.Configure
	spring:
	  redis:
	    host: 192.168.0.124
##### 3.code
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	@Test
	public void testOpsForValue(){
		ValueOperations<String, String> operator1 = redisTemplate.opsForValue();
		
		String key1 = "tiamaes.cloud.redis.value";
		String expected = "192.168.0.124";
		operator1.set(key1, expected);
		
		Object actual1 = operator1.get(key1);
		logger.debug(actual1.toString());
		assertEquals(expected, actual1);
	}
# 3.Usage
#####1. RedisKey定义规则
#####1.1	建议使用keyspace:id 的格式来保存数据
**eg: com.tiamase.cloud.XXX:123456**  

在集群环境中，为避免和其它项目的Key冲突，请使用自己项目的全类名作为Keyspace,对象的主键作为其id。

#####2. 通过注入不同类型的RedisTemplate来操作不同类型的Redis方法。

	ValueOperations<String, String> operator = redisTemplate.opsForValue();
	
	HashOperations<String, String, Object> operator = redisTemplate.opsForHash();
	
	SetOperations<String, String> operator = redisTemplate.opsForSet();
	
#####3. 使用Redis集群配置
	spring:
	  redis:
	    cluster:
	      nodes: localhost:7000,localhost:7001,localhost:7002
	      password:      
		  timeout: 2000
	      max-redirects: 3		//最大重定向次数，不能超过主节点数，否则可能引起死循环
	      
	*注意：当使用Redis集群时，不能使用多键操作*
#####4. 使用Key失效通知
	public interface RedisKeyExpiredListener {
		
		public void onMessage(RedisKeyExpiredEvent<String> event);
	}
当项目中注入RedisKeyExpiredListener接口实例时，将开启Key失效通知事件监听。

	@Component
	public class CustomizeRedisKeyExpiredListener implements RedisKeyExpiredListener {
	
		private static Logger logger = LogManager.getLogger(CustomizeRedisKeyExpiredListener.class);
		@Resource
		private RedisTemplate<String, ?> redisTemplate;
	
		@Override
		public void onMessage(RedisKeyExpiredEvent<String> event) {
			String key = new String(event.getSource());
			String keyspace = event.getKeyspace();
			String id = new String(event.getId());
			logger.trace("Redis key [{}] already expired, the value is [{}]", key, event.getValue());
		}
	
	}

# 4.Example
[https://github.com/mattmok/spring-cloud-example-parent/tree/master/spring-cloud-example-redis](https://github.com/mattmok/spring-cloud-example-parent/tree/master/spring-cloud-example-redis)

# 5.More
1. [http://docs.spring.io/spring-data/redis/docs/current/reference/html/](http://docs.spring.io/spring-data/redis/docs/current/reference/html/)
2. [https://github.com/spring-projects/spring-data-redis](https://github.com/spring-projects/spring-data-redis)
3. [http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)


# 6.Authors
chenlili@tiamaes.com
# 7.License
Tiamaes cloud project is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
