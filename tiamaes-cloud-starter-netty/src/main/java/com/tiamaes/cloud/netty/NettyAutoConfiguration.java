package com.tiamaes.cloud.netty;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tiamaes.cloud.netty.repository.ChannelRepository;
import com.tiamaes.cloud.netty.server.NettyServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Configuration
@Import(NettyServer.class)
@EnableConfigurationProperties(NettyProperties.class)
public class NettyAutoConfiguration {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(NettyAutoConfiguration.class);

	@Autowired
	private ChannelInitializer<SocketChannel> channelInitializer;

	@Bean
	public NettyProperties nettyProperties() {
		return new NettyProperties();
	}

	@Bean(name = "serverBootstrap")
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ServerBootstrap bootstrap(NettyProperties nettyProperties) {
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup(nettyProperties), workerGroup(nettyProperties))
				.channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.DEBUG))
				.childHandler(channelInitializer);
		Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions(nettyProperties);
		Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
		for (ChannelOption option : keySet) {
			serverBootstrap.option(option, tcpChannelOptions.get(option));
		}
		return serverBootstrap;
	}

	@Bean(name = "tcpChannelOptions")
	public Map<ChannelOption<?>, Object> tcpChannelOptions(NettyProperties nettyProperties) {
		Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();
		options.put(ChannelOption.SO_KEEPALIVE, nettyProperties.isSoKeepalive());
		options.put(ChannelOption.SO_BACKLOG, nettyProperties.getSoBacklog());
		return options;
	}

	@Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
	public NioEventLoopGroup bossGroup(NettyProperties nettyProperties) {
		return new NioEventLoopGroup(nettyProperties.getBossThreadCount());
	}

	@Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
	public NioEventLoopGroup workerGroup(NettyProperties nettyProperties) {
		return new NioEventLoopGroup(nettyProperties.getWorkerThreadCount());
	}

	@Bean(name = "tcpSocketAddress")
	public InetSocketAddress tcpPort(NettyProperties nettyProperties) {
		return new InetSocketAddress(nettyProperties.getTcpPort());
	}

	@Bean
	public ChannelRepository channelRepository() {
		return new ChannelRepository();
	}
}
