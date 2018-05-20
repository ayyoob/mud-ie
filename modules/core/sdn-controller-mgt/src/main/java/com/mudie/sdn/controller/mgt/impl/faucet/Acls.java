package com.mudie.sdn.controller.mgt.impl.faucet;

import java.util.List;
import java.util.Map;

public class Acls {

	private Map<String, List<RuleWrapper>> acls;

	public Map<String, List<RuleWrapper>> getAcls() {
		return acls;
	}

	public void setAcls(Map<String, List<RuleWrapper>> acls) {
		this.acls = acls;
	}
}
