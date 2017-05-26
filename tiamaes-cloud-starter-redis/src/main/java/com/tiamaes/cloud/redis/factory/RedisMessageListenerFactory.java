package com.tiamaes.cloud.redis.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.tiamaes.cloud.redis.listener.KeyExpiredEventMessageListener;
import com.tiamaes.cloud.redis.listener.RedisKeyExpiredListener;

import redis.clients.jedis.JedisShardInfo;

public class RedisMessageListenerFactory implements BeanFactoryAware, ApplicationListener<ContextRefreshedEvent> {
		
		private DefaultListableBeanFactory beanFactory;
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = (DefaultListableBeanFactory) beanFactory;
		}
		
		private RedisConnectionFactory redisConnectionFactory;
		public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
			this.redisConnectionFactory = redisConnectionFactory;
		}
		
		private RedisKeyExpiredListener redisKeyExpiredListener;
		public void setRedisKeyExpiredListener(RedisKeyExpiredListener redisKeyExpiredListener) {
			this.redisKeyExpiredListener = redisKeyExpiredListener;
		}


		public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
			RedisClusterConnection redisClusterConnection = redisConnectionFactory.getClusterConnection();
			if (redisClusterConnection != null) {
				Iterable<RedisClusterNode> nodes = redisClusterConnection.clusterGetNodes();
				for(RedisClusterNode node : nodes){
					if (node.isMaster()) {
						String containerBeanName = "messageContainer" + node.hashCode();
						if(beanFactory.containsBean(containerBeanName)){
							return;
						}
						JedisConnectionFactory factory = new JedisConnectionFactory(new JedisShardInfo(node.getHost(), node.getPort()));
						BeanDefinitionBuilder containerBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RedisMessageListenerContainer.class);
						containerBeanDefinitionBuilder.addPropertyValue("connectionFactory", factory);
						containerBeanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
						containerBeanDefinitionBuilder.setLazyInit(false);
						beanFactory.registerBeanDefinition(containerBeanName, containerBeanDefinitionBuilder.getRawBeanDefinition());
						
						RedisMessageListenerContainer container = beanFactory.getBean(containerBeanName, RedisMessageListenerContainer.class);
						
						BeanDefinitionBuilder listenerDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(KeyExpiredEventMessageListener.class);
						listenerDefinitionBuilder.addConstructorArgReference(containerBeanName);
						listenerDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
						
						String listenerBeanName = "messageListener" + node.hashCode();
						if(beanFactory.containsBean(listenerBeanName)){
							return;
						}
						beanFactory.registerBeanDefinition(listenerBeanName, listenerDefinitionBuilder.getRawBeanDefinition());
						
						KeyExpiredEventMessageListener listener = beanFactory.getBean(listenerBeanName, KeyExpiredEventMessageListener.class);
						listener.setPublisher(redisKeyExpiredListener);
						container.start();
					}
				}
			}
		}
	}