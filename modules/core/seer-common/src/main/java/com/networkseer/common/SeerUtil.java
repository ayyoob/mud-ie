package com.networkseer.common;

public class SeerUtil {

	public static String getSwitchMacFromDpID(String dpid) {
		//0xf4f26d229e7c
		String mac = dpid.substring(dpid.length() - 12);
		return String.join(":", mac.split("(?<=\\G.{2})"));
	}

	public static String getDpidFromMac(String mac) {
		String dpid = mac.replace(":", "");
		return  dpid;
	}
}
