package com.tiamaes.cloud.feign;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.web.context.request.RequestContextHolder;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class FeignAutoConfiguration {
	private static Logger logger = LoggerFactory.getLogger(FeignAutoConfiguration.class);
	
	@Configuration
	@ConditionalOnClass(Session.class)
	@ConditionalOnBean(SessionRepository.class)
	@AutoConfigureBefore(SessionAutoConfiguration.class)
	protected class SessionConfiguration {

		@Bean
	    public RequestInterceptor requestInterceptor() {
			logger.debug("FeignSessionRequestInterceptor has been created.");
			return new RequestInterceptor(){
				@Override
				public void apply(RequestTemplate template) {
					try{
		        		String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
		        		if (StringUtils.isNotBlank(sessionId)) {
		        			template.header("Cookie", "SESSION=" + sessionId);
		        		}
		        	}catch(IllegalStateException e){
		        	}
				}
			};
	    }
	}
}
