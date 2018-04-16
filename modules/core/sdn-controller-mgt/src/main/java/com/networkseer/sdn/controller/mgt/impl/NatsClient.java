package com.networkseer.sdn.controller.mgt.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networkseer.sdn.controller.mgt.impl.faucet.L2LearnWrapper;
import com.networkseer.sdn.controller.mgt.internal.SdnControllerDataHolder;
import io.nats.client.Connection;
import io.nats.client.Nats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NatsClient implements Runnable {
	private String url;
	private static final String NATS_URL_PREFIX = "nats://";
	private static final String FAUCET_SOCKET_STREAM_SUBJECT = "faucet.sock.stream";
	private static final Logger log = LoggerFactory.getLogger(NatsClient.class);
	private static final int DEFAULT_RETRY_INTERVAL = 10000;
	private Connection subscriber;
	private static boolean disconnected = false;
	private static String L2_LEARN_LABLE = "L2_LEARN";

	public NatsClient() {
		url = NATS_URL_PREFIX + SdnControllerDataHolder.getController().getHostname() + ":" +
				SdnControllerDataHolder.getController().getPort();

	}

	private void connectAndSubscribe() throws IOException {
		subscriber = Nats.connect(url);
		subscriber.subscribe(FAUCET_SOCKET_STREAM_SUBJECT, m -> {
			String data = new String(m.getData());
			if (data.contains(L2_LEARN_LABLE)) {
				ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
				L2LearnWrapper l2LearnWrapper = null;
				try {
					l2LearnWrapper = mapper.readValue(data, L2LearnWrapper.class);
					SdnControllerDataHolder.getL2LearnWrapperMap().put(l2LearnWrapper.getL2Learn().getEthSrc(), l2LearnWrapper);
				} catch (IOException e) {
					log.error("invalid structure for l2 learn result" + data, e);
				}
			}
		});
	}

	public void publish(String subject, String msg) throws IOException {
		Connection publisher = Nats.connect(url);
		publisher.publish(subject, msg.getBytes());
		publisher.close();
	}

	public void disconnect() {
		disconnected = true;
		if (subscriber != null && !subscriber.isClosed()) {
			subscriber.close();
		}
	}

	@Override
	public void run() {
		while(!disconnected) {
			if (subscriber == null || !subscriber.isConnected()) {
				try {
					connectAndSubscribe();
					log.info("Nats Client connected");
				} catch (IOException e) {
					log.error("Failed to subscribe... retrying", e);
					try {
						Thread.sleep(DEFAULT_RETRY_INTERVAL);
					} catch (InterruptedException e1) {
						Thread.interrupted();
					}
				}
			}
		}
	}
}
