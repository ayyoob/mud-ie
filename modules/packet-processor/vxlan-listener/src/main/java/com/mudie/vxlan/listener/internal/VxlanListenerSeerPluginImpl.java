package com.mudie.vxlan.listener.internal;

import com.mudie.common.SeerPlugin;
import com.mudie.vxlan.listener.VxlanListener;
import com.mudie.common.config.SeerConfiguration;
import com.mudie.common.packet.PacketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class VxlanListenerSeerPluginImpl implements SeerPlugin {

	private static final Logger log = LoggerFactory.getLogger(VxlanListenerSeerPluginImpl.class);

	@Override
	public void activate(SeerConfiguration seerConfiguration) {
		List<PacketListener> packetListeners = new ArrayList<>();
		ServiceLoader<PacketListener> serviceLoader = ServiceLoader.load(PacketListener.class);
		for (PacketListener provider : serviceLoader) {
			packetListeners.add(provider);
		}
		VxLanListenerDataHolder.setPacketListeners(packetListeners);

		new VxlanListener().start();
	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		return null;
	}
}
