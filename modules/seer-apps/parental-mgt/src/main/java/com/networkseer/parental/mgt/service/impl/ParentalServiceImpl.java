package com.networkseer.parental.mgt.service.impl;

import com.networkseer.parental.mgt.dao.ParentalDAO;
import com.networkseer.parental.mgt.dao.ParentalManagementDAOFactory;
import com.networkseer.parental.mgt.dao.impl.ParentalDAOImpl;
import com.networkseer.parental.mgt.dto.DnsEntry;
import com.networkseer.parental.mgt.exception.ParentalManagementException;
import com.networkseer.parental.mgt.service.ParentalService;

import java.util.List;

public class ParentalServiceImpl implements ParentalService {
	private ParentalDAO parentalDAO;

	public ParentalServiceImpl() {
		parentalDAO = new ParentalDAOImpl();
	}

	@Override
	public void addTags(List<DnsEntry> dnsEntries) throws ParentalManagementException {
		try {
			ParentalManagementDAOFactory.beginTransaction();
			parentalDAO.addTags(dnsEntries);
			ParentalManagementDAOFactory.commitTransaction();
		} catch (Exception e) {
			ParentalManagementDAOFactory.rollbackTransaction();
			throw new ParentalManagementException("Error occurred while adding tags", e);
		} finally {
			ParentalManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<String> getUntaggedDns() throws ParentalManagementException {
		try {
			ParentalManagementDAOFactory.openConnection();
			return parentalDAO.getUntaggedDns();
		} catch (Exception e) {
			throw new ParentalManagementException(e);
		} finally {
			ParentalManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public void addDnsEntries(List<DnsEntry> dnsEntries) throws ParentalManagementException {
		try {
			ParentalManagementDAOFactory.beginTransaction();
			parentalDAO.addDnsEntries(dnsEntries);
			ParentalManagementDAOFactory.commitTransaction();
		} catch (Exception e) {
			ParentalManagementDAOFactory.rollbackTransaction();
			throw new ParentalManagementException("Error occurred while adding dns", e);
		} finally {
			ParentalManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<DnsEntry> getDnsEntries(String dpId, String deviceMac) throws ParentalManagementException {
		try {
			ParentalManagementDAOFactory.openConnection();
			return parentalDAO.getDnsEntries(dpId, deviceMac);
		} catch (Exception e) {
			throw new ParentalManagementException(e);
		} finally {
			ParentalManagementDAOFactory.closeConnection();
		}
	}

}
