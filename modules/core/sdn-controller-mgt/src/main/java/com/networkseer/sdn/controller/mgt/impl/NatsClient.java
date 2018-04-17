package com.networkseer.sdn.controller.mgt.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networkseer.common.openflow.OFFlow;
import com.networkseer.sdn.controller.mgt.impl.faucet.Action;
import com.networkseer.sdn.controller.mgt.impl.faucet.L2LearnWrapper;
import com.networkseer.sdn.controller.mgt.impl.faucet.Rule;
import com.networkseer.sdn.controller.mgt.internal.SdnControllerDataHolder;
import io.nats.client.Connection;
import io.nats.client.Nats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

	public NatsClient(String url) {
		this.url = url;

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

	public static void main(String[] args) throws IOException, InterruptedException {
		NatsClient natsClient = new NatsClient(NATS_URL_PREFIX + "localhost:4222");
		ModFlowMsg addFlowMsg = new ModFlowMsg();
		addFlowMsg.setDpid(Long.decode("0x"+"f4f26d229e7c"));
		Rule rule = new Rule();
		OFFlow ofFlow = new OFFlow();
		ofFlow.setSrcMac("ab:ff:ff:ff:ff:ff");
		ofFlow.setEthType("0x86dd");
		ofFlow.setIpProto("17");
		ofFlow.setDstPort("547");
		ofFlow.setDstIp("ff00::/8");
		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
		rule.setOFFlow(ofFlow);
		List<Action> actions = new ArrayList<>();
		if (ofFlow.getOfAction() == OFFlow.OFAction.NORMAL) {
			Action action = new Action();
			action.setType("OUTPUT");
			action.setPort(Long.decode("0xfffffffa"));
			actions.add(action);
		} else {
			throw new NotImplementedException();
		}
		addFlowMsg.setCookie(7730494);
		addFlowMsg.setPriority(15000);
		addFlowMsg.setIdleTimeout(60);
		addFlowMsg.setActions(actions);
		rule.setActions(null);
		List<Rule> rules = new ArrayList<>();
		rules.add(rule);
		addFlowMsg.setMatch(rule);
		ObjectMapper mapper = new ObjectMapper();
		try {
			String msg = mapper.writeValueAsString(addFlowMsg);
			natsClient.publish("faucet.msg", msg);
		} catch (IOException e) {
		}
	}
}
