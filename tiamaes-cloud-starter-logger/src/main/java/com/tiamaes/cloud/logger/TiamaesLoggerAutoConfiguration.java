package com.tiamaes.cloud.logger;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

@Configuration
@ConditionalOnClass(TiamaesLoggerAdvice.class)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
public class TiamaesLoggerAutoConfiguration {
	private static Logger logger = LogManager.getLogger();
	
	@Bean
	protected TiamaesLoggerAdvice tiamaesLoggerAdvice(){
		if(logger.isDebugEnabled()){
			logger.debug("TiamaesLogger is loading ...");
		}
		return new TiamaesLoggerAdvice();
	}
	
	@Primary
	@Bean(name = "objectMapper")
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
		ObjectMapper objectMapper = builder.createXmlMapper(false).build();
		objectMapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
		return objectMapper;
	}

	@Bean(name = "com.tiamaes.logger.TiamaesLoggerAutoConfiguration.KafkaProperties")
	public KafkaProperties kafkaProperties() {
		return new KafkaProperties();
	}

	@Bean
	@ConditionalOnMissingBean(name = {"kafkaTemplate"})
	public KafkaTemplate<String, String> kafkaTemplate(ObjectMapper objectMapper) {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties().getBrokers());
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		KafkaTemplate<String, String> template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
		template.setMessageConverter(new StringJsonMessageConverter(objectMapper));
		return template;
	}
	
	
	@ConfigurationProperties(prefix = "spring.kafka")
	public class KafkaProperties {
		private String brokers;
		private String zookeepers;

		public String getBrokers() {
			return brokers;
		}

		public void setBrokers(String brokers) {
			this.brokers = brokers;
		}

		public String getZookeepers() {
			return zookeepers;
		}

		public void setZookeepers(String zookeepers) {
			this.zookeepers = zookeepers;
		}
	}
}
