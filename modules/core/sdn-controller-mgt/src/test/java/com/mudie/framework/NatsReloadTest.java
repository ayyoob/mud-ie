package com.mudie.framework;

import com.mudie.sdn.controller.mgt.impl.NatsClient;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

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
