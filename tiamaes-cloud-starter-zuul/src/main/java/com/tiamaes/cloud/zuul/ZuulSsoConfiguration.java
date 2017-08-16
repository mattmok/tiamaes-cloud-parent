package com.tiamaes.cloud.zuul;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

//@Configuration
//@EnableOAuth2Sso
public class ZuulSsoConfiguration extends WebSecurityConfigurerAdapter{
	
	@Value("${security.oauth2.client.accessTokenUri}")
	private String oauth2ClientAccessTokenUri;
	@Value("${server.address}")
	private String address;
	@Value("${server.port}")
	private String port;
	@Value("${server.contextPath}")
	private String contextPath;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().anyRequest().authenticated();
		http.logout().logoutUrl("/logout").logoutSuccessUrl(getRedirectUri()).deleteCookies("SESSION", "JSESSIONID");
	}
	
	
	public String getRedirectUri(){
		StringBuilder redirectUri = new StringBuilder();
		redirectUri.append(oauth2ClientAccessTokenUri.substring(0, oauth2ClientAccessTokenUri.indexOf("/oauth/token")));
		redirectUri.append("/logout?redirectUrl=http://");
		redirectUri.append(address + ":" + port + contextPath);
		return redirectUri.toString();
	}
}
