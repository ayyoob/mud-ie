package com.networkseer.parental.mgt.dao;

import com.networkseer.parental.mgt.dto.DnsEntry;
import com.networkseer.parental.mgt.exception.ParentalManagementException;

import java.util.List;

public interface ParentalDAO {

	void addTags(List<DnsEntry> dnsEntries) throws ParentalManagementException;

	List<String> getUntaggedDns() throws ParentalManagementException;

	void addDnsEntries(List<DnsEntry> dnsEntry) throws ParentalManagementException;

	List<DnsEntry> getDnsEntries(String dpId, String deviceMac) throws ParentalManagementException;

}
