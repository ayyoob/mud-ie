package com.mudie.sdn.controller.mgt.impl.faucet;

import java.util.List;
import java.util.Map;

public class SwitchFaucetConfig {

	List<String> include;
	Map<Integer, AclsIn> vlans;

	public Map<Integer, AclsIn> getVlans() {
		return vlans;
	}

	public void setVlans(Map<Integer, AclsIn> vlans) {
		this.vlans = vlans;
	}

	public List<String> getInclude() {
		return include;
	}

	public void setInclude(List<String> include) {
		this.include = include;
	}
}
