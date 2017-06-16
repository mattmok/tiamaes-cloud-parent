# tiamaes-cloud-starter-zuul
# 1.Overview
Zuul 是Spring Cloud中的Gateway服务，当使用微服务构建整个 API 服务时，一般会有许许多多职责不同的子服务在运行着，Gateway就是外部到内部的一道门，其主要功能：
#####1.1 服务路由：将前段应用的调用请求路由定位并负载均衡到具体的后端微服务实例，对于前端应用看起来就是1个应用提供的服务，微服务对于前段应用来说就是黑盒，前段应用也不需要关心内部如何分布，由哪个微服务提供。
#####1.2 静态路由：有时候需要通过域名或者其他固定方式提供和配置路由表。
#####1.3 动态路由：通过服务发现服务，动态调整后端微服务的运行实例和路由表，为路由和负载均衡提供动态变化的服务注册信息。
#####1.4 安全：统一集中的身份认证，安全控制。比如登录，签名，黑名单等等，还可以挖掘和开发更高级的安全策略。
#####1.5 弹性：限流和容错，也是另一个层面的安全防护，防止突发的流量或者高峰流量冲击后端微服务而导致其服务不可用，另一方面可以在高峰期通过容错和降级保证核心服务的运行。
#####1.6 监控：实时观察后端微服务的TPS、响应时间，失败数量等准确的信息。
#####1.7 日志：记录所有请求的访问日志数据，可以为日志分析和查询提供统一支持。

# 2.Quick Start
##### 1.Dependencies
	<dependencies>
		<dependency>
			<groupId>com.tiamaes.cloud</groupId>
			<artifactId>tiamaes-cloud-starter-security</artifactId>
		</dependency>
		<dependency>
			<groupId>com.tiamaes.cloud</groupId>
			<artifactId>tiamaes-cloud-starter-zuul</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-eureka</artifactId>
			<optional>true</optional>
		</dependency>
	</dependencies>
##### 2.Configure
	zuul:
	  ignoredServices: '*'
	  routes:
	    simple:
	      path: /simple/**
	      sensitive-headers:
	      stripPrefix: false
	      serviceId: spring-cloud-example-simaple
	    feign:
	      path: /feign/**
	      sensitive-headers:
	      stripPrefix: false
	      serviceId: spring-cloud-example-feig
	    legacy:
	      path: /legacy/**
	      url: http://127.0.0.1:8700/simple/legacy
	    forward:
	      path: /forward/**
	      url: forward:/hello
##### 3.code
	@EnableZuulProxy
	@EnableEurekaClient
	@SpringBootApplication
	public class Application{
		public static void main(String[] args) {
			SpringApplication.run(Application.class, args);
		}
	}
# 3.Usage
#####1.非必需依赖
	<dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-eureka</artifactId>
        <optional>true</optional>
    </dependency>

#####2.主要配置
######2.1	 忽略服务
		zuul.ignoredServices: '*'
Zuul默认会将Eureka发现的所有服务都添加到动态路由中去，可让zuul忽略所有服务，只添加routes指定的路由。
######2.2 路由配置
	zuul.routes.yourRouteName
Zuul.routes支持多个路由列表，可以在这里添加你想要进行的路由操作。

	zuul.routes.yourRouteName.path
指定路由的路径前缀

	zuul.routes.yourRouteName.sensitive-headers:
指定路由中的敏感的消息头，默认为Cookie,Set-Cookie,Authorization，这样可能会导致后续子服务用户授权失效，因此需要设置为空字符串。

	zuul.routes.yourRouteName.stripPrefix: false
指定路由是否跳过前缀，即请求地址中是否带上path指定的字段。

	zuul.routes.yourRouteName.serviceId: spring-cloud-example-simaple
指定路由跳转的服务名称，为子服务注册到Eureka中的服务名称。

	zuul.routes.yourRouteName.url: http://legacy.example.com
当未使用Eureka服务时，可直接添加具体的地址，跳转到指定的服务中
#####3.应用开关 
	@EnableZuulProxy
开启Zuul网关支持

	@EnableEurekaClient
开启Eureka Client服务支持

# 4.Example
[https://github.com/mattmok/spring-cloud-example-parent/tree/master/spring-cloud-example-gateway](https://github.com/mattmok/spring-cloud-example-parent/tree/master/spring-cloud-example-gateway)

# 5.More
1. [https://github.com/Netflix/zuul/wiki](https://github.com/Netflix/zuul/wiki)
2. [http://www.baeldung.com/spring-rest-with-zuul-proxy](http://www.baeldung.com/spring-rest-with-zuul-proxy)
3. [http://cloud.spring.io/spring-cloud-netflix/spring-cloud-netflix.html](http://cloud.spring.io/spring-cloud-netflix/spring-cloud-netflix.html)
4. [http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
5. [https://eacdy.gitbooks.io/spring-cloud-book/content/2 Spring Cloud/2.6 API Gateway.html](https://eacdy.gitbooks.io/spring-cloud-book/content/2%20Spring%20Cloud/2.6%20API%20Gateway.html)
6. [https://medium.com/netflix-techblog/optimizing-the-netflix-api-5c9ac715cf19](https://medium.com/netflix-techblog/optimizing-the-netflix-api-5c9ac715cf19)

# 6.Authors
chenlili@tiamaes.com
# 7.License
Tiamaes cloud project is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
