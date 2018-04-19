package com.networkseer.framework;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    //	public static void main(String[] args) throws IOException, InterruptedException {
//		NatsClient natsClient = new NatsClient(NATS_URL_PREFIX + "localhost:4222");
////		ModFlowMsg addFlowMsg = new ModFlowMsg();
////		addFlowMsg.setDpid(Long.decode("0x"+"f4f26d229e7c"));
////		Rule rule = new Rule();
////		OFFlow ofFlow = new OFFlow();
////		ofFlow.setSrcMac("ab:ff:ff:ff:ff:ff");
////		ofFlow.setEthType("0x86dd");
////		ofFlow.setIpProto("17");
////		ofFlow.setDstPort("547");
////		ofFlow.setDstIp("ff00::/8");
////		ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
////		rule.setOFFlow(ofFlow);
////		List<Action> actions = new ArrayList<>();
////		if (ofFlow.getOfAction() == OFFlow.OFAction.NORMAL) {
////			Action action = new Action();
////			action.setType("OUTPUT");
////			action.setPort(Long.decode("0xfffffffa"));
////			actions.add(action);
////		} else {
////			throw new NotImplementedException();
////		}
////		addFlowMsg.setCookie(7730494);
////		addFlowMsg.setPriority(15000);
////		addFlowMsg.setIdleTimeout(60);
////		addFlowMsg.setActions(actions);
////		rule.setActions(null);
////		List<Rule> rules = new ArrayList<>();
////		rules.add(rule);
////		addFlowMsg.setMatch(rule);
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			//String msg = mapper.writeValueAsString(addFlowMsg);
//			natsClient.publish("faucet.msg", "reload");
//		} catch (IOException e) {
//		}
//	}
}
