package com.networkseer.vxlan.listener.internal;

import com.networkseer.common.*;
import com.networkseer.vxlan.listener.VxlanListener;

import java.util.List;

public class VxlanListenerSeerPluginImpl implements SeerPlugin {

	@Override
	public void activate() {
		new VxlanListener().run();
	}

	@Override
	public void deactivate() {

	}

	@Override
	public List<String> getModuleDependencies() {
		return null;
	}
}
