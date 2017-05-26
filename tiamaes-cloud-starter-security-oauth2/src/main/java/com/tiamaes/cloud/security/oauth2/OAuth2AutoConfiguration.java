package com.tiamaes.cloud.security.oauth2;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;

import feign.RequestInterceptor;

@Configuration
@AutoConfigureBefore(org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration.class)
public class OAuth2AutoConfiguration {

	@Configuration
	@ConditionalOnClass(RequestInterceptor.class)
	protected class FeignConfiguration {

		@Bean
		public RequestInterceptor oauth2FeignRequestInterceptor(OAuth2ClientContext oAuth2ClientContext, OAuth2ProtectedResourceDetails resource) {
			ConfigurationManager.getConfigInstance()
				.setProperty("hystrix.command.default.execution.isolation.strategy", ExecutionIsolationStrategy.SEMAPHORE);
			return new OAuth2FeignRequestInterceptor(oAuth2ClientContext, resource);
		}
	}
}
