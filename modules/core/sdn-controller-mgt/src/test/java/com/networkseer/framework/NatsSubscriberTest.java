package com.networkseer.framework;

import com.networkseer.sdn.controller.mgt.impl.NatsClient;
import com.networkseer.sdn.controller.mgt.internal.SdnControllerDataHolder;

public class NatsSubscriberTest {

	public static void main(String[] args) throws InterruptedException {
		NatsClient natsClient = new NatsClient("nats://149.171.37.71:4222");
		SdnControllerDataHolder.setNatsClient(natsClient);
		Thread thread = new Thread(natsClient);
		thread.start();
		Thread.sleep(1000000L);
	}
}
