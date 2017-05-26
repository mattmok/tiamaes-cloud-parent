package com.tiamaes.cloud.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

@EnableKafka
@Configuration
@AutoConfigureAfter({ JacksonAutoConfiguration.class, org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class })
public class KafkaAutoConfiguration {
	
	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper objectMapper;
	
	@Bean
	@ConditionalOnMissingBean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
		return objectMapper;
	}

	@Bean
	@Primary
	public KafkaTemplate<?, ?> kafkaTemplate(
			ProducerFactory<Object, Object> kafkaProducerFactory,
			ProducerListener<Object, Object> kafkaProducerListener) {
		KafkaTemplate<Object, Object> template = new KafkaTemplate<>(kafkaProducerFactory);
		template.setProducerListener(kafkaProducerListener);
		template.setMessageConverter(new StringJsonMessageConverter(objectMapper));
		return template;
	}

	@Bean
	public KafkaListenerContainerFactory<?> stringListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		if(recordFilterStrategy != null){
			factory.setRecordFilterStrategy(recordFilterStrategy);
		}
		return factory;
	}

	@Bean
	public KafkaListenerContainerFactory<?> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setMessageConverter(new StringJsonMessageConverter(objectMapper));
		if(recordFilterStrategy != null){
			factory.setRecordFilterStrategy(recordFilterStrategy);
		}
		return factory;
	}
	
	@Autowired(required = false)
	public RecordFilterStrategy<String, String> recordFilterStrategy;

}
