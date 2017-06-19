package com.tiamaes.cloud.security;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.SecurityProperties.Basic;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.util.Assert;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tiamaes.cloud.security.execption.CustomHandlerExceptionResolver;
import com.tiamaes.cloud.security.provisioning.InMemoryUserManagerConfigurer;
import com.tiamaes.security.core.DefaultGrantedAuthority;
import com.tiamaes.security.core.userdetails.User;

@Configuration
@AutoConfigureAfter({ JacksonAutoConfiguration.class, SecurityAutoConfiguration.class })
@ConditionalOnClass({ AuthenticationManager.class, GlobalAuthenticationConfigurerAdapter.class })
public class WebSecurityAutoConfiguration implements InitializingBean {
	private static Logger logger = LogManager.getLogger(WebSecurityAutoConfiguration.class);

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
	public class MvcConfig extends WebMvcConfigurerAdapter {
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

	@Configuration
	@EnableConfigurationProperties(SecurityFormProperties.class)
	@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
	@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
	public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

		@Autowired
		protected PasswordEncoder passwordEncoder;
		@Autowired
		private UserDetailsService userDetailsService;
		@Autowired
		private SecurityProperties securityProperties;
		@Autowired
		private SecurityFormProperties securityFormProperties;
		@Autowired(required = false)
		private AuthenticationSuccessHandler successHandler;
		@Autowired(required = false)
		private AuthenticationFailureHandler failureHandler;

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
				http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).disable();
			}
			
			if(securityFormProperties.isEnabled()){
				FormLoginConfigurer<HttpSecurity> formLogin = http.formLogin()
	            	.loginPage(securityFormProperties.getLoginPage())
	            	.loginProcessingUrl(securityFormProperties.getLoginProcessingUrl())
	            	.usernameParameter(securityFormProperties.getParameters().getUsername())
	            	.passwordParameter(securityFormProperties.getParameters().getPassword())
	            	.defaultSuccessUrl(securityFormProperties.getDefaultSuccessUrl())
	            	.permitAll();
				
				if(successHandler != null){
					formLogin.successHandler(successHandler);
				}
				if(failureHandler != null){
					formLogin.failureHandler(failureHandler);
				}
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
	}
}
