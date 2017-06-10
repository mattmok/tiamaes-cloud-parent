package com.tiamaes.cloud.zuul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;

import com.tiamaes.cloud.zuul.filter.HttpSessionZuulPreFilter;

@Configuration
@EnableZuulProxy
@EnableConfigurationProperties(ZuulProperties.class)
public class ZuulAutoConfiguration {
	private static Logger logger = LoggerFactory.getLogger(ZuulAutoConfiguration.class);
	
	@Configuration
	@ConditionalOnClass(Session.class)
	@ConditionalOnBean(RedisOperationsSessionRepository.class)
	@AutoConfigureAfter(SessionAutoConfiguration.class)
	protected class SessionConfiguration {

		@Bean
	    public HttpSessionZuulPreFilter httpSessionZuulPreFilter(RedisOperationsSessionRepository repository) {
			logger.debug("HttpSessionZuulPreFilter has been created.");
			return new HttpSessionZuulPreFilter(repository);
	    }
	}

}
