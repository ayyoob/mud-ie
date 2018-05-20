package com.mudie.mud.processor;

import com.mudie.common.openflow.OFFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceMUDFlowMap {
	private static final Logger log = LoggerFactory.getLogger(DeviceMUDFlowMap.class);
	private static int MAXIP_PER_DNS = 20;

	private Map<String, List<String>> dnsIpMap = new HashMap<>();
	private List<OFFlow> fromInternetDynamicFlows = new ArrayList<>();
	private List<OFFlow> toInternetDynamicFlows = new ArrayList<>();
	private List<OFFlow> fromInternetStaticFlows = new ArrayList<>();
	private List<OFFlow> toInternetStaticFlows = new ArrayList<>();
	private List<OFFlow> fromLocalStaticFlows = new ArrayList<>();
	private List<OFFlow> toLocalStaticFlows = new ArrayList<>();
	private long longCacheTime;
	private long lastCheckedTimestamp;

	public Map<String, List<String>> getDnsIpMap() {
		return dnsIpMap;
	}

	public void setDnsIpMap(Map<String, List<String>> dnsIpMap) {
		this.dnsIpMap = dnsIpMap;
	}

	public List<OFFlow> getFromInternetDynamicFlows() {
		return fromInternetDynamicFlows;
	}

	public void setFromInternetDynamicFlows(List<OFFlow> fromInternetDynamicFlows) {
		this.fromInternetDynamicFlows = fromInternetDynamicFlows;
	}

	public List<OFFlow> getToInternetDynamicFlows() {
		return toInternetDynamicFlows;
	}

	public void setToInternetDynamicFlows(List<OFFlow> toInternetDynamicFlows) {
		this.toInternetDynamicFlows = toInternetDynamicFlows;
	}

	public List<OFFlow> getFromInternetStaticFlows() {
		return fromInternetStaticFlows;
	}

	public void setFromInternetStaticFlows(List<OFFlow> fromInternetStaticFlows) {
		this.fromInternetStaticFlows = fromInternetStaticFlows;
	}

	public List<OFFlow> getToInternetStaticFlows() {
		return toInternetStaticFlows;
	}

	public void setToInternetStaticFlows(List<OFFlow> toInternetStaticFlows) {
		this.toInternetStaticFlows = toInternetStaticFlows;
	}

	public List<OFFlow> getFromLocalStaticFlows() {
		return fromLocalStaticFlows;
	}

	public void setFromLocalStaticFlows(List<OFFlow> fromLocalStaticFlows) {
		this.fromLocalStaticFlows = fromLocalStaticFlows;
	}

	public List<OFFlow> getToLocalStaticFlows() {
		return toLocalStaticFlows;
	}

	public void setToLocalStaticFlows(List<OFFlow> toLocalStaticFlows) {
		this.toLocalStaticFlows = toLocalStaticFlows;
	}

	public void addDnsIps(String dns, List<String> ips) {
		if (dnsIpMap.get(dns) == null) {
			dnsIpMap.put(dns, ips);
		} else {
			List<String> ipholder = dnsIpMap.get(dns);
			if (ips.size() > MAXIP_PER_DNS) {
				log.error("ips size is too long" + ips.size() + " for " + dns);
			}
			if (ipholder.size() + ips.size() > MAXIP_PER_DNS) {
				int toRemove = MAXIP_PER_DNS - (ipholder.size() + ips.size());
				if (toRemove > 0) {
					for (int i = 0; i < toRemove; i++) {
						ipholder.remove(0);
					}
				}
			}
			ipholder.addAll(ips);
			dnsIpMap.put(dns, ipholder);
		}
	}

	public String getDns(String ip) {
		for (String dns : dnsIpMap.keySet()) {
			if (dnsIpMap.get(dns).contains(ip)) {
				return dns;
			}
		}
		return null;
	}

	public long getLongCacheTime() {
		return longCacheTime;
	}

	public void setLongCacheTime(long longCacheTime) {
		this.longCacheTime = longCacheTime;
	}

	public long getLastCheckedTimestamp() {
		return lastCheckedTimestamp;
	}

	public void setLastCheckedTimestamp(long lastCheckedTimestamp) {
		this.lastCheckedTimestamp = lastCheckedTimestamp;
	}
}
