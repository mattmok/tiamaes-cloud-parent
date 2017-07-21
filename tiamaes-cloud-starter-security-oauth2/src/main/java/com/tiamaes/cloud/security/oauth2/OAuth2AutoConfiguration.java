package com.tiamaes.cloud.security.oauth2;

import java.security.KeyPair;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import feign.RequestInterceptor;

@Configuration
@AutoConfigureBefore(org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration.class)
public class OAuth2AutoConfiguration {
	
	@Configuration
	@ConditionalOnBean(AuthorizationServerEndpointsConfiguration.class)
	public class OAuth2AuthorizationServer extends AuthorizationServerConfigurerAdapter {

		@Autowired
		private DataSource dataSource;
		@Autowired
		private PasswordEncoder passwordEncoder;
		@Autowired
		private AuthenticationManager authenticationManager;

		@Bean
		@ConditionalOnMissingBean
		public JwtAccessTokenConverter jwtAccessTokenConverter() {
			JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
			KeyPair keyPair = new KeyStoreKeyFactory(new ClassPathResource("keystore.jks"), "foobar".toCharArray())
					.getKeyPair("test");
			converter.setKeyPair(keyPair);
			return converter;
		}

		@Bean
		@ConditionalOnMissingBean
		protected AuthorizationCodeServices authorizationCodeServices() {
		    return new JdbcAuthorizationCodeServices(dataSource);
		}
		
		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			clients.jdbc(dataSource).passwordEncoder(passwordEncoder);
		}

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints.authenticationManager(authenticationManager).accessTokenConverter(jwtAccessTokenConverter());
		}

		@Override
		public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
			oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
		}

	}
	
	
	
//	@Configuration
//	@SessionAttributes("authorizationRequest")
//	@ConditionalOnBean(AuthorizationServerEndpointsConfiguration.class)
//	public static class OAuth2MvcConfigurer extends WebMvcConfigurerAdapter{
//
//		@Override
//		public void addViewControllers(ViewControllerRegistry registry) {
//			registry.addViewController("/oauth/confirm_access").setViewName("authorize");
//		}
//	}

	@Configuration
	@ConditionalOnClass(RequestInterceptor.class)
	protected class FeignConfiguration {

		@Bean
		public RequestInterceptor oauth2FeignRequestInterceptor(OAuth2ClientContext oAuth2ClientContext, OAuth2ProtectedResourceDetails resource) {
			return new OAuth2FeignRequestInterceptor(oAuth2ClientContext, resource);
		}
	}
}
