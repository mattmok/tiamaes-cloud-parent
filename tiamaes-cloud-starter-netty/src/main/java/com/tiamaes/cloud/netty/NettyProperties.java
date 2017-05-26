package com.tiamaes.cloud.netty;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.netty")
public class NettyProperties {
	private int tcpPort;
	private int bossThreadCount;
	private int workerThreadCount;
	private boolean soKeepalive;
	private int soBacklog;

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public int getBossThreadCount() {
		return bossThreadCount;
	}

	public void setBossThreadCount(int bossThreadCount) {
		this.bossThreadCount = bossThreadCount;
	}

	public int getWorkerThreadCount() {
		return workerThreadCount;
	}

	public void setWorkerThreadCount(int workerThreadCount) {
		this.workerThreadCount = workerThreadCount;
	}

	public boolean isSoKeepalive() {
		return soKeepalive;
	}

	public void setSoKeepalive(boolean soKeepalive) {
		this.soKeepalive = soKeepalive;
	}

	public int getSoBacklog() {
		return soBacklog;
	}

	public void setSoBacklog(int soBacklog) {
		this.soBacklog = soBacklog;
	}
}