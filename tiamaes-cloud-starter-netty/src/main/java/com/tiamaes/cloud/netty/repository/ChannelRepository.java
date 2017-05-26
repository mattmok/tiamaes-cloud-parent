package com.tiamaes.cloud.netty.repository;

import java.util.HashMap;

import io.netty.channel.Channel;

public class ChannelRepository {
	/**
	 * key: terminalId value: channel
	 */
	private HashMap<String, Channel> channelCache = new HashMap<String, Channel>();

	public ChannelRepository put(String key, Channel value) {
		channelCache.put(key, value);
		return this;
	}

	public Channel get(String key) {
		return channelCache.get(key);
	}

	public void remove(String key) {
		this.channelCache.remove(key);
	}

	public int size() {
		return this.channelCache.size();
	}
}
