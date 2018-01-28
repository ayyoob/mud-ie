package com.networkseer.vxlan.listener;

import java.net.InetAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VxlanListener {
	private static final int VXLAN_PORT = 4976;
	private static final Logger log = LoggerFactory.getLogger(VxlanListener.class);

	public void run() {
		final NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			final Bootstrap b = new Bootstrap();
			b.group(group).channel(NioDatagramChannel.class)
					.option(ChannelOption.SO_BROADCAST, true)
					.handler(new ChannelInitializer<NioDatagramChannel>() {
						@Override
						public void initChannel(final NioDatagramChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new IncommingPacketHandler());
						}
					});

			// Bind and start to accept incoming connections.
			InetAddress address  = InetAddress.getLocalHost();
			log.info("Initializing vxlan packet listner on port " + VXLAN_PORT);
			b.bind(address,VXLAN_PORT).sync().channel().closeFuture().await();

		} catch (Exception e) {
			log.error("Error thrown on vx lan listener: ", e);
		}
	}
}
