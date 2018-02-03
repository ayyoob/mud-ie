package com.networkseer.parental.mgt.internal;

import com.networkseer.parental.mgt.dto.DnsEntry;
import com.networkseer.parental.mgt.service.ParentalService;
import com.networkseer.parental.mgt.service.impl.ParentalServiceImpl;
import com.networkseer.seer.mgt.service.SeerMgtService;

import java.util.ArrayList;
import java.util.List;

public class ParentalManagementDataHolder {
	private static SeerMgtService seerMgtService;
	private static List<DnsEntry> dnsEntries = new ArrayList();
	private static ParentalService parentalService = new ParentalServiceImpl();

	public static SeerMgtService getSeerMgtService() {
		return seerMgtService;
	}

	public static void setSeerMgtService(SeerMgtService seerMgtService) {
		ParentalManagementDataHolder.seerMgtService = seerMgtService;
	}

	public static List<DnsEntry> getDnsEntries() {
		List<DnsEntry> data;
		synchronized (ParentalManagementDataHolder.class) {
			data = new ArrayList<>(dnsEntries);
			dnsEntries.clear();
		}
		return data;
	}

	public static void addDnsEntry(DnsEntry dnsEntry) {
		ParentalManagementDataHolder.dnsEntries.add(dnsEntry);
	}

	public static ParentalService getParentalService() {
		return parentalService;
	}

	public static void setParentalService(ParentalService parentalService) {
		ParentalManagementDataHolder.parentalService = parentalService;
	}

	public static void setDnsEntries(List<DnsEntry> dnsEntries) {
		ParentalManagementDataHolder.dnsEntries = dnsEntries;
	}
}
