package com.networkseer.vxlan.listener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is based on the implementation described in https://sam.andrews-home.com/post/netty-doesnt-suck-at-udp-servers
 * linux version should be > 3.9
 */
public class VxlanListener extends Thread {
	private static final int VXLAN_PORT = 4789;
	private static final Logger log = LoggerFactory.getLogger(VxlanListener.class);
	private static final String INET_HOST_ADDR = "0.0.0.0";
	private static final int THREADS = Runtime.getRuntime().availableProcessors() * 2; // Default EventLoopGroup Size

	@Override
	public void run() {
		final NioEventLoopGroup group = new NioEventLoopGroup();
		if (OsCheck.getOperatingSystemType()== OsCheck.OSType.Windows) {
			startOnWindows();
		} else {
			startOnLinux();
		}
	}

	public void startOnLinux() {
		final NioEventLoopGroup group = new NioEventLoopGroup(THREADS);
		InetAddress address = null;
		try {
			final Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioDatagramChannel.class)
					.option(ChannelOption.SO_BROADCAST, true)
					.option(EpollChannelOption.SO_REUSEPORT, true)
					.handler(new IncommingPacketHandler());

			// Bind and start to accept incoming connections.
			address  = InetAddress.getLocalHost();
			log.info("Initializing vxlan packet listner on port " + VXLAN_PORT);
			List<ChannelFuture> futures = new ArrayList<>(THREADS);
			// Bind THREADS times
			for(int i = 0; i < THREADS; ++i) {
				futures.add(bootstrap.bind(INET_HOST_ADDR, VXLAN_PORT).await());
			}
			// Now wait for all to be closed (if ever)
			for (final ChannelFuture future : futures) {
				future.channel().closeFuture().await();
			}
		} catch (InterruptedException e) {
			log.error("Listener thread interrupted ..", e);
		} catch (UnknownHostException e) {
			log.error("Invalid hostname" + address != null ? address.getHostName() : "");
		} finally {
			group.shutdownGracefully();
		}
	}

	public void startOnWindows() {
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
		} finally {
			group.shutdownGracefully();
		}
	}
}
