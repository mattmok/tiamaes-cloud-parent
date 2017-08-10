package com.tiamaes.cloud.security;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.SecurityProperties.Basic;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tiamaes.cloud.security.SecurityProperties.Form;
import com.tiamaes.cloud.security.execption.CustomHandlerExceptionResolver;
import com.tiamaes.cloud.security.provisioning.InMemoryUserManagerConfigurer;
import com.tiamaes.security.core.DefaultGrantedAuthority;
import com.tiamaes.security.core.userdetails.User;

@Configuration
@AutoConfigureAfter({ JacksonAutoConfiguration.class, SecurityAutoConfiguration.class })
@ConditionalOnClass({ AuthenticationManager.class, GlobalAuthenticationConfigurerAdapter.class })
public class WebSecurityAutoConfiguration implements InitializingBean {
	private static Logger logger = LoggerFactory.getLogger(WebSecurityAutoConfiguration.class);

	@Autowired
	@Qualifier("jacksonObjectMapper")
	private ObjectMapper jacksonObjectMapper;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(jacksonObjectMapper != null){
			jacksonObjectMapper.disable(SerializationFeature.INDENT_OUTPUT);
			jacksonObjectMapper.setSerializationInclusion(Include.NON_NULL);
		}
	}
	
	@Bean
	@ConditionalOnMissingBean
	protected PasswordEncoder passwordEncoder() {
		return new StandardPasswordEncoder();
	}
	
	@Bean(name = "objectMapper")
	@ConditionalOnMissingBean(name = "objectMapper")
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
		ObjectMapper objectMapper = builder.createXmlMapper(false).build();
		objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
		return objectMapper;
	}

	@Bean
	public HandlerExceptionResolver handlerExceptionResolver() {
		return new CustomHandlerExceptionResolver(jacksonObjectMapper);
	}

	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(jacksonObjectMapper);
		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(new MediaType("application", "json", Charset.forName("UTF-8")));
		supportedMediaTypes.add(new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8")));
		converter.setSupportedMediaTypes(supportedMediaTypes);
		return converter;
	}

	@Bean
	public HttpMessageConverters httpMessageConverters(
			MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
		return new HttpMessageConverters(mappingJackson2HttpMessageConverter);
	}

	@Bean
	@ConditionalOnMissingBean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder,
			SecurityProperties securityProperties) {
		SecurityProperties.User securityUser = securityProperties.getUser();

		List<String> roles = securityUser.getRole();
		List<DefaultGrantedAuthority> authorities = new ArrayList<DefaultGrantedAuthority>(roles.size());
		for (String role : roles) {
			Assert.isTrue(!role.startsWith("ROLE_"), role + " cannot start with ROLE_ (it is automatically added)");
			authorities.add(new DefaultGrantedAuthority("ROLE_" + role, role));
		}
		if (securityUser.isDefaultPassword()) {
			logger.info("\n\nUsing default security password: " + securityUser.getPassword() + "\n");
		}

		User user = new User(securityUser.getName(), passwordEncoder.encode(securityUser.getPassword()), authorities);

		List<User> users = new ArrayList<User>();
		users.add(user);

		InMemoryUserManagerConfigurer<AuthenticationManagerBuilder> configurer = new InMemoryUserManagerConfigurer<AuthenticationManagerBuilder>(
				users);
		return configurer.getUserDetailsService();
	}
	
	@Configuration
	public class MvcConfigurer extends WebMvcConfigurerAdapter {
		@Resource
		private HandlerExceptionResolver handlerExceptionResolver;
		@Resource
		private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

		@Override
		public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
			super.configureMessageConverters(converters);
			converters.add(mappingJackson2HttpMessageConverter);
		}

		@Override
		public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
			super.addArgumentResolvers(argumentResolvers);
			argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
		}

		@Override
		public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
			super.configureHandlerExceptionResolvers(exceptionResolvers);
			exceptionResolvers.add(handlerExceptionResolver);
		}

		@Override
		public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
			super.configureDefaultServletHandling(configurer);
			configurer.enable();
		}

		@Override
		public void addCorsMappings(CorsRegistry registry) {
			registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "PATCH")
					.allowedHeaders("X-XSRF-TOKEN", "X-Requested-With").maxAge(3600);
		}

	}
	
	@Bean
	@Primary
	public SecurityProperties securityProperties(){
		return new com.tiamaes.cloud.security.SecurityProperties();
	}
	
	@Configuration
	@ConditionalOnProperty(prefix = "security.form", name = "enabled", matchIfMissing = false)
	public static class FormAuthenticationHandlerConfigurer{
		
		@Bean
		@ConditionalOnMissingBean(AuthenticationSuccessHandler.class)
		public AuthenticationSuccessHandler successHandler(){
			return new SavedRequestAwareAuthenticationSuccessHandler();
		}
		
		@Bean
		@ConditionalOnMissingBean(AuthenticationFailureHandler.class)
		public AuthenticationFailureHandler failureHandler(){
			return new SimpleUrlAuthenticationFailureHandler();
		}
		
		@Bean
		@ConditionalOnMissingBean(LogoutSuccessHandler.class)
		public SimpleUrlLogoutSuccessHandler logoutSuccessHandler(){
			SimpleUrlLogoutSuccessHandler urlLogoutHandler = new SimpleUrlLogoutSuccessHandler();
			urlLogoutHandler.setDefaultTargetUrl("/login.html");
			urlLogoutHandler.setTargetUrlParameter("redirectUrl");
			urlLogoutHandler.setUseReferer(true);
			return urlLogoutHandler;
		}
	}
	
	/**
	 * https://github.com/spring-projects/spring-security-oauth/issues/789 
	 *
	 * In general you might have to configure the request matchers for the app (i.e. /login, /oauth/* etc.) and the resources (/me etc.).
	 * By default the resource server has a matcher for everything except /oauth/*, but you have added a WebSecurityConfigurerAdapter with 
	 * default order, which matches all requests, and hence your resource server filter chain is never used. You could change the order of
	 * your custom one (or since it is not explicitly configureing HTTP security, just remove it).
	 * 
	 * @author Chen
	 *
	 */
	@Configuration
	@EnableConfigurationProperties(com.tiamaes.cloud.security.SecurityProperties.class)
	@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
	public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter implements Ordered{
		
		@Autowired
		protected ObjectProvider<AuthorizationServerEndpointsConfiguration> authorizationServerEndpoints;
		@Autowired
		protected PasswordEncoder passwordEncoder;
		@Autowired
		private UserDetailsService userDetailsService;
		@Autowired
		private SecurityProperties securityProperties;
		@Autowired(required = false)
		private AuthenticationSuccessHandler successHandler;
		@Autowired(required = false)
		private AuthenticationFailureHandler failureHandler;
		@Autowired(required = false)
		public SimpleUrlLogoutSuccessHandler logoutSuccessHandler;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			Basic basic = securityProperties.getBasic();
			if (basic.isEnabled()) {
				http.httpBasic();
				http.authorizeRequests().anyRequest().authenticated();
			} else {
				http.httpBasic().disable();
				http.authorizeRequests().anyRequest().permitAll();
			}
			if (securityProperties.isEnableCsrf()) {
				http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
			} else {
				http.csrf().disable();
			}

			if (securityProperties instanceof SecurityProperties) {
				Form from = ((com.tiamaes.cloud.security.SecurityProperties) securityProperties).getForm();
				//@formatter:off
				if(from.isEnabled()){
					http.formLogin()
						.loginPage(from.getLoginPage())
						.loginProcessingUrl(from.getLoginProcessingUrl())
						.usernameParameter(from.getParameters().getUsername())
						.passwordParameter(from.getParameters().getPassword())
						.defaultSuccessUrl(from.getDefaultSuccessUrl())
						.successHandler(successHandler)
						.failureHandler(failureHandler)
						.permitAll();
					http.logout().logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler).deleteCookies("SESSION", "JSESSIONID");
				}
				//@formatter:on
			}
		}

		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
			provider.setPasswordEncoder(passwordEncoder);
			provider.setUserDetailsService(userDetailsService);
			auth.authenticationProvider(provider);
			super.configure(auth);
		}

		@Override
		public int getOrder() {
			int i = SecurityProperties.ACCESS_OVERRIDE_ORDER;
			if(authorizationServerEndpoints.getIfAvailable() != null){
				i = ManagementServerProperties.ACCESS_OVERRIDE_ORDER;
			}
			return i;
		}
	}
	
	@Bean
	@Primary
	public SpringResourceTemplateResolver defaultTemplateResolver(ApplicationContext applicationContext, ThymeleafProperties properties) {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setApplicationContext(applicationContext);
		resolver.setPrefix(properties.getPrefix());
		resolver.setSuffix(properties.getSuffix());
		resolver.setTemplateMode("HTML");
		if (properties.getEncoding() != null) {
			resolver.setCharacterEncoding(properties.getEncoding().name());
		}
		resolver.setCacheable(properties.isCache());
		Integer order = properties.getTemplateResolverOrder();
		if (order != null) {
			resolver.setOrder(order);
		}
		
		Method setCheckExistence = ReflectionUtils.findMethod(resolver.getClass(), "setCheckExistence", boolean.class);
		ReflectionUtils.invokeMethod(setCheckExistence, resolver, properties.isCheckTemplate());
		return resolver;
	}
	
}
