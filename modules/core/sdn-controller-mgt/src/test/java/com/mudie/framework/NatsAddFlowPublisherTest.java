package com.mudie.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudie.common.openflow.OFFlow;
import com.mudie.sdn.controller.mgt.impl.ModFlowMsg;
import com.mudie.sdn.controller.mgt.impl.NatsClient;
import com.mudie.sdn.controller.mgt.impl.faucet.Action;
import com.mudie.sdn.controller.mgt.impl.faucet.Rule;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class NatsAddFlowPublisherTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public NatsAddFlowPublisherTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( NatsAddFlowPublisherTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    	public static void main(String[] args) throws IOException, InterruptedException {
		NatsClient natsClient = new NatsClient("nats://149.171.37.71:4222");
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
			System.out.println(msg);
			natsClient.publish("faucet.msg", msg);
		} catch (IOException e) {
		}
	}
}
