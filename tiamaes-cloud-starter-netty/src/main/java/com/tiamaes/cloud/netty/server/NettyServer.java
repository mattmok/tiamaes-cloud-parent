package com.tiamaes.cloud.netty.server;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;

@Configuration
public class NettyServer implements SmartLifecycle {
	private static Logger logger = LoggerFactory.getLogger(NettyServer.class);
	@Autowired
	@Qualifier("serverBootstrap")
	private ServerBootstrap serverBootstrap;

	@Autowired
	@Qualifier("tcpSocketAddress")
	private InetSocketAddress inetSocketAddress;

	private Channel serverChannel;

	private AtomicBoolean running = new AtomicBoolean(false);

	@Override
	public void start() {
		int port = inetSocketAddress.getPort();

		try {
			serverChannel = serverBootstrap.bind(inetSocketAddress).sync().channel();
			
			running.set(true);
			logger.info(String.format("NettyServer started on port(s): %s (tcp)", port));
		} catch (InterruptedException e) {
			logger.error("Could not initialize NettyServer", e);
		}
	}

	@Override
	public void stop() {
		serverChannel.close();
		running.set(false);
		logger.info("NettyServer has been stopped.");
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}
}
