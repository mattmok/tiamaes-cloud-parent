# tiamaes-cloud-starter-security
# 1.Overview
该项目是对spring security 和spring mvc的基础整合，并进行了一部分的自动配置和改动，以方便集成到我们自己的项目中去，具体用法请参考官方文档。  

在身份验证层，Spring Security的支持多种认证模式。这些验证模型绝大多数都是要么由第三方提供，或由相关的标准组织，如互联网工程任务组开发。另外 Spring Security 提供自己的一组认证功能。具体而言，Spring Security目前支持所有这些技术集成的身份验证：

#####1.HTTP BASIC 认证头(基于 IETF RFC-based 标准)
#####2.HTTP Digest 认证头 (基于 IETF RFC-based 标准)
#####3.HTTP X.509 客户端证书交换 (IETF RFC-based 标准)
#####4.LDAP (一个非常常见的方法来跨平台认证需要，尤其是在大型环境)
#####5.Form-based authentication (用于简单的用户界面)
#####6.OpenID 认证
#####7.Authentication based on pre-established request headers (such as Computer - Associates Siteminder)根据预先建立的请求头进行验证
#####8.JA-SIG Central Authentication Service ( CAS, 一个开源的SSO系统)
#####9.Transparent authentication context propagation for Remote Method Invocation (RMI) and HttpInvoker (Spring远程协议)
#####10.Automatic "remember-me" authentication (所以你可以勾选一个框以避免预定的时间段再认证)
#####11.Anonymous authentication (让每一个未经验证的访问自动假设为一个特定的安全标识)
#####12.Run-as authentication (在一个访问应该使用不同的安全标识时非常有用)
#####13.Java Authentication and Authorization Service (JAAS)
#####14.JEE container autentication (所以如果愿意你可以任然使用容器管理的认证)
#####15.Java Open Source Single Sign On (JOSSO)
# 2.Quick Start
##### 1.Dependencies
	<dependencies>
		<dependency>
			<groupId>com.tiamaes.cloud</groupId>
			<artifactId>tiamaes-cloud-starter-security</artifactId>
		</dependency>
	</dependencies>
##### 2.Configure
	security:
	  ignored: /css/**,/js/**,/images/**,/webjars/**,**/favicon.ico
	  basic:
	    enabled: false
##### 3.code
	@SpringBootApplication
	public class Application{
		public static void main(String[] args) {
			SpringApplication.run(Application.class, args);
		}
	}
# 3.Usage
#####1.主要配置
######1.1	 忽略路径
	security.ignored: /css/**,/js/**,/images/**,/webjars/**,**/favicon.ico
security默认会将以上路径设置为忽略，即不对以上目录或文件添加权限控制。
######1.2 basic认证
	security.basic.enabled: false
默认启用basic认证，设置为false时关闭basic认证，默认用户名为user,密码参见项目中启动时打印日志。  

	eg: Using default security password: c61c806d-0fc1-4b23-9a80-8b3fca61be83
	
	*注意：该用户仅仅为测试使用，因些用户密码每次启动都会进行改变，实际使用过程中，请使用自己的用户管理模块来进行配置。*
	
	security.user.username: user		//修改默认用户
	security.user.password: password	//修改默认密码
	
开启项目单独的用户配置，只需要注入自定义的UserDetailsService实体：  

	@Bean
	protected UserDetailsService userDetailsService(){
		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				User user = ...;
				if(user == null){
					throw new UsernameNotFoundException();
				}
				return user;
			}
		};
	}
######1.3.表单认证
开启认证时，则需要根据实际情况覆盖以下配置：  

	security:
	  form:
	    enabled: true
	    login-page: /login.html		//登陆页面
	    login-processing-url: /login		//登陆请求路径
	    default-success-url: /index.html	//默认登陆成功页面
	    parameters:
	      username: username		//表单中username的参数名称
	       password: password		//表单中password的参数名称
	       
如果要实现自定义的登陆成功或失败业务逻辑，请注入以下类：

	@Bean
	public AuthenticationSuccessHandler successHandler() {
		return new AuthenticationSuccessHandler() {
			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
					throws IOException, ServletException {
				//TODO your code
			}
		};
	}
	
	@Bean
	public AuthenticationFailureHandler failureHandler() {
		return new AuthenticationFailureHandler() {
			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				// TODO your code
			}
		};
	}
#####2.使用自定义的密码解释器
项目中使用了	StandardPasswordEncoder，如果你想要实现自己的PasswordEncoder，需要在配置文件中注入：  

	@Bean
	protected PasswordEncoder passwordEncoder() {
		return new PasswordEncoder(){
			@Override
			public String encode(CharSequence rawPassword) {
				// TODO Auto-generated method stub
				return null;
			}
			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}
#####3.序列化方法
当前项目中使用jackson进行序列化和反序列化，在项目中已经配置了两个不同的ObjectMapper:

	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper objectMapper;
	
	@Autowired
	@Qualifier("jacksonObjectMapper")
	private ObjectMapper jacksonObjectMapper;
	
其中，objectMapper由于使用了以下配置项:

	objectMapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
	
会导致序列化的字符串结果中会包含@class字段，这种方式更有利于我们以编程方式进行对象的序列化和反序列化。因此，在进行类似kafka、redis等操作时，请注入objectMapper。
使用原则为：
######3.1.当对象是程序或组件使用时，请使用objectMapper进行序列化
######3.1.当对象是用户使用时，请使用jacksonObjectMapper进行序列化
	
#####4.权限配置
默认开启了@prePostEnabled和@securedEnabled支持。
######4.1	 @Secured

	@Secured("ADMIN")	//允许拥有ADMIN角色的角户访问
######4.2 @PreAuthorize

	@PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")	//允许拥有ADMIN或USER角色的用户访问
	@PreAuthorize("hasRole('ADMIN') AND hasRole('USER')")	//允许同时拥有ADMIN和USER角色的用户访问
	


# 4.Example
[https://github.com/mattmok/spring-cloud-example-parent/tree/master/spring-cloud-example-security](https://github.com/mattmok/spring-cloud-example-parent/tree/master/spring-cloud-example-security)

# 5.More
1. [https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/](https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/)
2. [http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
3. [https://vincentmi.gitbooks.io/spring-security-reference-zh/content/1_introduction.html](https://vincentmi.gitbooks.io/spring-security-reference-zh/content/1_introduction.html)

# 6.Authors
chenlili@tiamaes.com
# 7.License
Tiamaes cloud project is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
