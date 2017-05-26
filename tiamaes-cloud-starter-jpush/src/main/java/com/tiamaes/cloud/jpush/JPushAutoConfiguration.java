package com.tiamaes.cloud.jpush;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import cn.jiguang.common.ClientConfig;
import cn.jpush.api.JPushClient;

@Configuration
@EnableConfigurationProperties(JpushProperties.class)
public class JPushAutoConfiguration {
	
	@Bean
	public JpushProperties jpushProperties() {
		return new JpushProperties();
	}
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public JPushClient jPushClient(JpushProperties jpushProperties) {
		ClientConfig config = ClientConfig.getInstance();
		config.setMaxRetryTimes(3);
		return new JPushClient(jpushProperties.getMasterSecret(), jpushProperties.getAppKey(), null, config);
	}
	
	@Bean
	public JPusher JPusher(JPushClient jPushClient){
		return new JPusher(jPushClient);
	}
}
