package com.networkseer.sdn.controller.mgt.impl.faucet;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Actions {
	private Integer allow;
	private Integer mirror;

	public Integer getAllow() {
		return allow;
	}

	public void setAllow(Integer allow) {
		this.allow = allow;
	}

	public Integer getMirror() {
		return mirror;
	}

	public void setMirror(Integer mirror) {
		this.mirror = mirror;
	}
}
