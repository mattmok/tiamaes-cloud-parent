package com.tiamaes.cloud.feign;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.web.context.request.RequestContextHolder;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import feign.hystrix.HystrixFeign;
import feign.hystrix.SetterFactory;

@Configuration
public class FeignAutoConfiguration  {
	private static Logger logger = LoggerFactory.getLogger(FeignAutoConfiguration.class);

	@Configuration
	@ConditionalOnClass(Session.class)
	@ConditionalOnBean(SessionRepository.class)
	@AutoConfigureBefore({SessionAutoConfiguration.class, org.springframework.cloud.netflix.feign.FeignAutoConfiguration.class})
	protected class SessionConfiguration{

		@Configuration
		@ConditionalOnClass({ HystrixCommand.class, HystrixFeign.class })
		protected class HystrixFeignConfiguration {
			@Bean
			@Scope("prototype")
			@ConditionalOnMissingBean
			@ConditionalOnProperty(name = "feign.hystrix.enabled", matchIfMissing = true)
			public Feign.Builder feignHystrixBuilder() {
				HystrixFeign.Builder hystrixBuilder = new HystrixFeign.Builder();
				hystrixBuilder.setterFactory(new SetterFactory() {
					@Override
					public Setter create(Target<?> target, Method method) {
						String groupKey = target.name();
						String commandKey = Feign.configKey(target.type(), method);
						// @// @formatter:off
						return HystrixCommand.Setter
								.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
								.andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
								.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
								.withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)
								.withExecutionTimeoutInMilliseconds(3000));
						// @formatter:on
					}
				});
				return hystrixBuilder;
			}
		}
		
		@Bean
		public RequestInterceptor requestInterceptor() {
			logger.debug("FeignSessionRequestInterceptor has been created.");
			return new RequestInterceptor() {
				@Override
				public void apply(RequestTemplate template) {
					try {
						String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
						if (StringUtils.isNotBlank(sessionId)) {
							template.header("Cookie", "SESSION=" + sessionId);
						}
					} catch (IllegalStateException e) {
					}
				}
			};
		}
	}
}
