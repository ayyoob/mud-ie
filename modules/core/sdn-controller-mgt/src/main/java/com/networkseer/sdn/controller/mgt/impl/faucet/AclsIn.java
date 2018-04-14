package com.networkseer.sdn.controller.mgt.impl.faucet;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AclsIn {

	@JsonProperty("acls_in")
	private List<String> aclsIn;

	public List<String> getAclsIn() {
		return aclsIn;
	}

	public void setAclsIn(List<String> aclsIn) {
		this.aclsIn = aclsIn;
	}
}
