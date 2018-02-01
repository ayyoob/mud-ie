package com.networkseer.sdn.controller.mgt.impl.floodlight;

public class FlowDTO {

	private String aswitch;
	private String active="true";
	private int priority;
	private int hard_timeout;
	private String name;
	private String eth_src;
	private String eth_dst;
	private String ip_proto;
	private String eth_type;
	private String tp_dst;
	private String tp_src;
	private String actions;
	private String ipv4_src;
	private String ipv4_dst;

	public String getAswitch() {
		return aswitch;
	}

	public String getActive() {
		return active;
	}

	public int getPriority() {
		return priority;
	}

	public int getHard_timeout() {
		return hard_timeout;
	}

	public String getName() {
		return name;
	}

	public String getEth_src() {
		return eth_src;
	}

	public String getEth_dst() {
		return eth_dst;
	}

	public String getIp_proto() {
		return ip_proto;
	}

	public String getEth_type() {
		return eth_type;
	}

	public String getTp_dst() {
		return tp_dst;
	}

	public String getTp_src() {
		return tp_src;
	}

	public String getActions() {
		return actions;
	}

	public String getIpv4_src() {
		return ipv4_src;
	}

	public String getIpv4_dst() {
		return ipv4_dst;
	}

	public void setAswitch(String aswitch) {
		this.aswitch = aswitch;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setHard_timeout(int hard_timeout) {
		this.hard_timeout = hard_timeout;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEth_src(String eth_src) {
		this.eth_src = eth_src;
	}

	public void setEth_dst(String eth_dst) {
		this.eth_dst = eth_dst;
	}

	public void setIp_proto(String ip_proto) {
		this.ip_proto = ip_proto;
	}

	public void setEth_type(String eth_type) {
		this.eth_type = eth_type;
	}

	public void setTp_dst(String tp_dst) {
		this.tp_dst = tp_dst;
	}

	public void setTp_src(String tp_src) {
		this.tp_src = tp_src;
	}

	public void setActions(String actions) {
		this.actions = actions;
	}

	public void setIpv4_src(String ipv4_src) {
		this.ipv4_src = ipv4_src;
	}

	public void setIpv4_dst(String ipv4_dst) {
		this.ipv4_dst = ipv4_dst;
	}

	@Override
	public String toString() {
		return "{" +
				"  switch='" + aswitch + '\'' +
				", active='" + active + '\'' +
				", priority=" + priority +
				", hard_timeout=" + hard_timeout +
				", name='" + name + '\'' +
				(eth_src.equals("*")? ", eth_src='" + eth_src + '\'':"") +
				(eth_dst.equals("*")? ", eth_dst='" + eth_dst + '\'':"") +
				(eth_dst.equals("*")? ", ip_proto='" + ip_proto + '\'':"") +
				(eth_dst.equals("*")? ", eth_type='" + eth_type + '\'':"") +
				(eth_dst.equals("*")? ", tp_dst='" + tp_dst + '\'':"") +
				(eth_dst.equals("*")? ", tp_src='" + tp_src + '\'':"") +
				(eth_dst.equals("*")? ", ipv4_src='" + ipv4_src + '\'':"") +
				(eth_dst.equals("*")? ", ipv4_dst='" + ipv4_dst + '\'':"") +
				", actions='" + actions + '\'' +
				'}';
	}
}
