package com.networkseer.common.config;

import java.util.List;

public class MudConfig {

	private List<MUDController> mudControllers;
	private boolean mudPacketLogging;
	private int mudReactiveIdleTimeout;
	private int summerizationTimeInSeconds;

	public List<MUDController> getMudControllers() {
		return mudControllers;
	}

	public void setMudControllers(List<MUDController> mudControllers) {
		this.mudControllers = mudControllers;
	}

	public boolean isMudPacketLogging() {
		return mudPacketLogging;
	}

	public void setMudPacketLogging(boolean mudPacketLogging) {
		this.mudPacketLogging = mudPacketLogging;
	}

	public int getMudReactiveIdleTimeout() {
		return mudReactiveIdleTimeout;
	}

	public void setMudReactiveIdleTimeout(int mudReactiveIdleTimeout) {
		this.mudReactiveIdleTimeout = mudReactiveIdleTimeout;
	}

	public int getSummerizationTimeInSeconds() {
		return summerizationTimeInSeconds;
	}

	public void setSummerizationTimeInSeconds(int summerizationTimeInSeconds) {
		this.summerizationTimeInSeconds = summerizationTimeInSeconds;
	}
}
