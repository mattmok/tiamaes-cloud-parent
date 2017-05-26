package com.tiamaes.cloud.redis.listener;

import org.springframework.data.redis.core.RedisKeyExpiredEvent;

public interface RedisKeyExpiredListener {
	
	public void onMessage(RedisKeyExpiredEvent<String> event);
}
