package com.networkseer.parental.mgt;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.networkseer.common.packet.PacketConstants;
import com.networkseer.common.packet.PacketListener;
import com.networkseer.common.packet.SeerPacket;
import com.networkseer.parental.mgt.dto.DnsEntry;
import com.networkseer.parental.mgt.exception.ParentalManagementException;
import com.networkseer.parental.mgt.internal.ParentalManagementDataHolder;
import com.networkseer.seer.mgt.dto.Group;
import com.networkseer.seer.mgt.exception.SeerManagementException;
import org.pcap4j.packet.DnsPacket;
import org.pcap4j.packet.DnsQuestion;
import org.pcap4j.packet.IllegalRawDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DNSPacketListenerImpl implements PacketListener {
	private static final Logger log = LoggerFactory.getLogger(DNSPacketListenerImpl.class);

	private static final String DPID_PREFIX = "00:00:";
	private LoadingCache<CacheKey, Boolean> dnsCache;

	public DNSPacketListenerImpl() {
		dnsCache = CacheBuilder.newBuilder()
				.concurrencyLevel(4)
				.weakKeys()
				.maximumSize(10000)
				.expireAfterWrite(2, TimeUnit.MINUTES)
				.build(new CacheLoader<CacheKey, Boolean>() {
							@Override
							public Boolean load(CacheKey key) throws Exception {
								return Boolean.TRUE;
							}
						});

		ScheduledExecutorService addDnsExecutor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> {
			try {
				List<DnsEntry> dnsEntries = ParentalManagementDataHolder.getDnsEntries();
				ParentalManagementDataHolder.getParentalService().addDnsEntries(dnsEntries);
			} catch (ParentalManagementException e) {
				log.error(e.getMessage(), e);
			}
		};
		addDnsExecutor.scheduleWithFixedDelay(task, 1, 1, TimeUnit.MINUTES);

		ScheduledExecutorService addTagExecutor = Executors.newScheduledThreadPool(1);

//		Runnable addTagTask = () -> {
//			try {
//				List<String> Dns = ParentalManagementDataHolder.getParentalService().getUntaggedDns();
//				List<DnsEntry> dnsEntries = processDnsTag(Dns);
//				ParentalManagementDataHolder.getParentalService().addTags(dnsEntries);
//			} catch (ParentalManagementException e) {
//				log.error(e.getMessage(), e);
//			}
//		};
//		addTagExecutor.scheduleWithFixedDelay(addTagTask, 1, 10, TimeUnit.MINUTES);
	}
	@Override
	public void processPacket(SeerPacket seerPacket) {
		if (seerPacket.getIpProto()!= null && seerPacket.getIpProto().equals(PacketConstants.UDP_PROTO)
				&& seerPacket.getDstPort().equals(PacketConstants.DNS_PORT) ) {
			try {
				DnsPacket dnsPacket = DnsPacket.newPacket(seerPacket.getPayload(), 0, seerPacket.getPayload().length);
				List<DnsQuestion> dnsQuestions = dnsPacket.getHeader().getQuestions();
				if (dnsQuestions.size() > 0) {
					String dnsName = dnsQuestions.get(0).getQName().getName();
					String deviceMac = seerPacket.getSrcMac();
					String dpId = DPID_PREFIX + seerPacket.getDstMac();
					if (isParentalSupported(dpId, deviceMac)) {
						DnsEntry dnsEntry = new DnsEntry();
						dnsEntry.setDnsDomain(dnsName);
						dnsEntry.setDeviceMac(deviceMac);
						dnsEntry.setDpId(dpId);
						dnsEntry.setTimestamp(System.currentTimeMillis());
						ParentalManagementDataHolder.addDnsEntry(dnsEntry);
					}
				}
			} catch (IllegalRawDataException e) {
				log.error("Failed to process DNS packet", e);
			} catch (SeerManagementException e) {
				log.error("Failed to retrieve meta group information", e);
			}
		}
	}

	private boolean isParentalSupported(String dpId, String deviceMac) throws SeerManagementException {
		try {
			Boolean entry = dnsCache.get(new CacheKey(dpId, deviceMac));
			if (entry != null) {
				return entry;
			}
		} catch (ExecutionException e) {
			log.error("Failed to retrieve value from cache", e);
		}

		Group group = ParentalManagementDataHolder.getSeerMgtService().getGroup(dpId, deviceMac);
		if (group!= null && group.isParentalAppEnabled()) {
			dnsCache.put(new CacheKey(dpId, deviceMac), Boolean.TRUE);
			return true;
		} else {
			dnsCache.put(new CacheKey(dpId, deviceMac), Boolean.FALSE);
		}
		return false;
	}

	public class CacheKey {
		private String dpId;
		private String deviceMac;

		public CacheKey(String dpId, String deviceMac) {
			this.dpId = dpId;
			this.deviceMac = deviceMac;
		}

		public String getDpId() {
			return dpId;
		}

		public void setDpId(String dpId) {
			this.dpId = dpId;
		}

		public String getDeviceMac() {
			return deviceMac;
		}

		public void setDeviceMac(String deviceMac) {
			this.deviceMac = deviceMac;
		}

		@Override
		public int hashCode() {
			int result = this.dpId.hashCode();
			result = 31 * result + ("@" + this.deviceMac).hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof CacheKey) && dpId.equals(
					((CacheKey) obj).dpId) && deviceMac.equals(
					((CacheKey) obj).deviceMac);
		}
	}

	private List<DnsEntry> processDnsTag(List<String> dns) {
		return null;
	}
}
