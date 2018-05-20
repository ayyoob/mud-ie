package com.networkseer.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networkseer.common.openflow.OFFlow;
import com.networkseer.sdn.controller.mgt.impl.ModFlowMsg;
import com.networkseer.sdn.controller.mgt.impl.NatsClient;
import com.networkseer.sdn.controller.mgt.impl.faucet.Action;
import com.networkseer.sdn.controller.mgt.impl.faucet.Rule;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class NatsReloadTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public NatsReloadTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( NatsReloadTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    	public static void main(String[] args) throws IOException {
		NatsClient natsClient = new NatsClient("nats://149.171.37.71:4222");
		natsClient.publish("faucet.msg", "reload");
	}
}
