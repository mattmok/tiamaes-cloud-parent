package com.tiamaes.cloud.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.tiamaes.cloud.redis.factory.RedisMessageListenerFactory;
import com.tiamaes.cloud.redis.listener.KeyExpiredEventMessageListener;
import com.tiamaes.cloud.redis.listener.RedisKeyExpiredListener;

import redis.clients.jedis.Jedis;

@Configuration
@ConditionalOnClass({ JedisConnection.class, RedisOperations.class, Jedis.class })
@AutoConfigureAfter({ JacksonAutoConfiguration.class,
		org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class })
public class RedisAutoConfiguration {
	private static Logger logger = LoggerFactory.getLogger(RedisAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean(name = "objectMapper")
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
		if (logger.isDebugEnabled()) {
			logger.debug("Overriding bean definition for bean 'objectMapper' with a different definition");
		}
		ObjectMapper objectMapper = builder.createXmlMapper(false).build();
		objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
		return objectMapper;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory,
			@Qualifier("objectMapper") ObjectMapper objectMapper) {
		RedisSerializer<?> serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(redisConnectionFactory);
		template.setDefaultSerializer(new StringRedisSerializer());
		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);
		template.setEnableTransactionSupport(false);
		return template;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory,
			@Qualifier("objectMapper") ObjectMapper objectMapper) {
		RedisSerializer<?> serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setDefaultSerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(serializer);
		redisTemplate.setHashValueSerializer(serializer);
		redisTemplate.setEnableTransactionSupport(false);
		return redisTemplate;
	}

	@Configuration
	@ConditionalOnExpression("!'${spring.redis.host:}'.isEmpty()")
	public static class RedisStandAloneAutoConfiguration {

		@Bean
		@ConditionalOnBean(RedisKeyExpiredListener.class)
		public RedisMessageListenerContainer customizeRedisListenerContainer(
				RedisConnectionFactory redisConnectionFactory) {
			RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
			redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
			return redisMessageListenerContainer;
		}

		@Bean
		@ConditionalOnBean(RedisKeyExpiredListener.class)
		@ConditionalOnMissingBean(KeyExpiredEventMessageListener.class)
		public KeyExpiredEventMessageListener keyExpiredEventMessageListener(
				@Qualifier("customizeRedisListenerContainer") RedisMessageListenerContainer container,
				RedisKeyExpiredListener publisher) {
			KeyExpiredEventMessageListener listener = new KeyExpiredEventMessageListener(container);
			listener.setPublisher(publisher);
			return listener;
		}
	}

	@Configuration
	@ConditionalOnExpression("!'${spring.redis.cluster.nodes:}'.isEmpty()")
	public static class RedisClusterAutoConfiguration {

		@Bean
		@ConditionalOnBean(RedisKeyExpiredListener.class)
		public RedisMessageListenerFactory redisMessageListenerFactory(BeanFactory beanFactory,
				RedisConnectionFactory redisConnectionFactory, RedisKeyExpiredListener publisher) {
			RedisMessageListenerFactory beans = new RedisMessageListenerFactory();
			beans.setBeanFactory(beanFactory);
			beans.setRedisKeyExpiredListener(publisher);
			beans.setRedisConnectionFactory(redisConnectionFactory);
			return beans;
		}
	}
}
