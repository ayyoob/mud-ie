package com.mudie.sdn.controller.mgt.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mudie.sdn.controller.mgt.impl.faucet.Action;
import com.mudie.sdn.controller.mgt.impl.faucet.Rule;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModFlowMsg {

	private List<Action> actions;
	private long dpid;
	private int cookie;
	private int priority;
	private Rule match;

	@JsonProperty("idle_timeout")
	long idleTimeout;

	public long getDpid() {
		return dpid;
	}

	public void setDpid(long dpid) {
		this.dpid = dpid;
	}

	public int getCookie() {
		return cookie;
	}

	public void setCookie(int cookie) {
		this.cookie = cookie;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Rule getMatch() {
		return match;
	}

	public void setMatch(Rule match) {
		this.match = match;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public long getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}
}
