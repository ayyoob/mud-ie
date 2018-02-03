package com.networkseer.parental.mgt.service;

import com.networkseer.parental.mgt.dto.DnsEntry;
import com.networkseer.parental.mgt.exception.ParentalManagementException;

import java.util.List;

public interface ParentalService {

	void addTags(List<DnsEntry> dnsEntries) throws ParentalManagementException;

	List<String> getUntaggedDns() throws ParentalManagementException;

	void addDnsEntries(List<DnsEntry> dnsEntries) throws ParentalManagementException;

	List<DnsEntry> getDnsEntries(String dpId, String deviceMac) throws ParentalManagementException;

}
