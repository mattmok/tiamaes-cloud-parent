package com.tiamaes.cloud.security.oauth2;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.JwtAccessTokenConverterConfigurer;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.util.StringUtils;

import com.tiamaes.security.core.DefaultGrantedAuthority;
import com.tiamaes.security.core.userdetails.User;

import feign.RequestInterceptor;

@Configuration
@AutoConfigureBefore(org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration.class)
public class OAuth2AutoConfiguration {
	
	private static final String NICKNAME = "nickname";
	
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
			JwtAccessTokenConverter converter = new JwtAccessTokenConverter(){
				private JsonParser objectMapper = JsonParserFactory.create();
				
				@Override
				public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
					DefaultOAuth2AccessToken result = new DefaultOAuth2AccessToken(accessToken);
					Map<String, Object> info = new LinkedHashMap<String, Object>(accessToken.getAdditionalInformation());
					String tokenId = result.getValue();
					if (!info.containsKey(TOKEN_ID)) {
						info.put(TOKEN_ID, tokenId);
					}
					else {
						tokenId = (String) info.get(TOKEN_ID);
					}
					User user = null;
					if(authentication.getPrincipal() instanceof User){
						user = (User)authentication.getPrincipal();
					}
					
					if(user != null){
						info.put(NICKNAME, user.getNickname());
					}
					
					result.setAdditionalInformation(info);
					result.setValue(encode(result, authentication));
					OAuth2RefreshToken refreshToken = result.getRefreshToken();
					if (refreshToken != null) {
						DefaultOAuth2AccessToken encodedRefreshToken = new DefaultOAuth2AccessToken(accessToken);
						encodedRefreshToken.setValue(refreshToken.getValue());
						// Refresh tokens do not expire unless explicitly of the right type
						encodedRefreshToken.setExpiration(null);
						try {
							Map<String, Object> claims = this.objectMapper
									.parseMap(JwtHelper.decode(refreshToken.getValue()).getClaims());
							if (claims.containsKey(TOKEN_ID)) {
								encodedRefreshToken.setValue(claims.get(TOKEN_ID).toString());
							}
						}
						catch (IllegalArgumentException e) {
						}
						Map<String, Object> refreshTokenInfo = new LinkedHashMap<String, Object>(
								accessToken.getAdditionalInformation());
						refreshTokenInfo.put(TOKEN_ID, encodedRefreshToken.getValue());
						refreshTokenInfo.put(ACCESS_TOKEN_ID, tokenId);
						if(user != null){
							refreshTokenInfo.put(NICKNAME, user.getNickname());
						}
						encodedRefreshToken.setAdditionalInformation(refreshTokenInfo);
						DefaultOAuth2RefreshToken token = new DefaultOAuth2RefreshToken(
								encode(encodedRefreshToken, authentication));
						if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
							Date expiration = ((ExpiringOAuth2RefreshToken) refreshToken).getExpiration();
							encodedRefreshToken.setExpiration(expiration);
							token = new DefaultExpiringOAuth2RefreshToken(encode(encodedRefreshToken, authentication), expiration);
						}
						result.setRefreshToken(token);
					}
					return result;
				}
			};
			KeyPair keyPair = new KeyStoreKeyFactory(new ClassPathResource("keystore.jks"), "foobar".toCharArray()).getKeyPair("test");
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
	
	
	@Configuration
	@ConditionalOnMissingBean(AuthorizationServerEndpointsConfiguration.class)
	public class OAuth2ResourceServerConfigurer {
		
		@Bean
		public JwtAccessTokenConverterConfigurer jwtAccessTokenConverterConfigurer(){
			return new JwtAccessTokenConverterConfigurer(){
				@Override
				public void configure(JwtAccessTokenConverter converter) {
					DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
					accessTokenConverter.setUserTokenConverter(new DefaultUserAuthenticationConverter(){
						@Override
						public Authentication extractAuthentication(Map<String, ?> map) {
							Authentication authentication = super.extractAuthentication(map);
							
							if (map.containsKey(NICKNAME)) {
								String nickname = (String)map.get(NICKNAME);
								String username = (String)authentication.getPrincipal();
								Collection<? extends GrantedAuthority> authorities = getAuthorities(map);
								List<DefaultGrantedAuthority> list = new ArrayList<>();
								for(GrantedAuthority authority : authorities){
									list.add(new DefaultGrantedAuthority(authority.getAuthority()));
								}
								User user = new User(username, null, nickname, true, true, true, true, list);
								return new UsernamePasswordAuthenticationToken(user, "N/A", authorities);
							}
							return authentication;
						}
						
						
						private Collection<? extends GrantedAuthority> getAuthorities(Map<String, ?> map) {
							Object authorities = map.get(AUTHORITIES);
							if (authorities instanceof String) {
								return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
							}
							if (authorities instanceof Collection) {
								return AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils
										.collectionToCommaDelimitedString((Collection<?>) authorities));
							}
							throw new IllegalArgumentException("Authorities must be either a String or a Collection");
						}
					});
					converter.setAccessTokenConverter(accessTokenConverter);
				}
			};
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
