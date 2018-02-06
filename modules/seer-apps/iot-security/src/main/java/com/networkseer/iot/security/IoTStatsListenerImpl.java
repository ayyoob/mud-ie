package com.networkseer.iot.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.networkseer.common.openflow.OFFlow;
import com.networkseer.common.stats.OFFlowStatsListener;
import com.networkseer.iot.security.dto.DeviceIdentifier;
import com.networkseer.iot.security.internal.IoTSecurityDataHolder;
import com.networkseer.sdn.controller.mgt.exception.OFControllerException;
import com.networkseer.seer.mgt.dto.DeviceRecord;
import com.networkseer.seer.mgt.exception.SeerManagementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class IoTStatsListenerImpl implements OFFlowStatsListener {
	private static final Logger log = LoggerFactory.getLogger(IoTStatsListenerImpl.class);


	public IoTStatsListenerImpl() {
		LoadingCache<DeviceIdentifier, List<OFFlow>> statsCache = CacheBuilder.newBuilder()
				.concurrencyLevel(4)
				.weakKeys()
				.maximumSize(10000)
				.expireAfterWrite(3, TimeUnit.MINUTES)
				.build(new CacheLoader<DeviceIdentifier, List<OFFlow>>() {
					@Override
					public List<OFFlow> load(DeviceIdentifier key) throws Exception {
						return null;
					}
				});
		IoTSecurityDataHolder.setStatsCache(statsCache);
	}

	@Override
	public void processFlowStats(Map<String, List<OFFlow>> ofFlowStats) {
		try {
			List<DeviceRecord> deviceRecords = IoTSecurityDataHolder.getSeerMgtService().getIoTDeviceRecord();
			for (DeviceRecord deviceRecord: deviceRecords) {
				String dpId = deviceRecord.getaSwitch().getDpId();
				List<OFFlow> flows = ofFlowStats.get(deviceRecord.getaSwitch().getDpId());
				if (flows != null) {
					String deviceMac = deviceRecord.getDevice().getMac();
					List<OFFlow> deviceFlows = extractDeviceFlow(deviceRecord.getDevice().getMac(), flows);
					if (deviceFlows.size() > 0) {
						if (IoTUtil.MINIMUM_IOT_FLOW_RULES <= deviceFlows.size()) {
							IoTSecurityDataHolder.getStatsCache().put(new DeviceIdentifier(dpId, deviceMac), deviceFlows);
						} else {
							//push iot device flows.
							IoTUtil.initializeDeviceFlows(dpId, deviceMac);
						}
					}
				}

			}
		} catch (SeerManagementException e) {
			log.error(e.getMessage(), e);
		} catch (OFControllerException e) {
			log.error("Failed add flows", e);
		}
	}

	private List<OFFlow> extractDeviceFlow(String deviceMac, List<OFFlow> flows) {
		List<OFFlow> deviceFlowList = new ArrayList<>();
		for (OFFlow ofFlow : flows) {
			if (ofFlow.getSrcMac().equals(deviceMac) || ofFlow.getDstMac().equals(deviceMac)) {
				deviceFlowList.add(ofFlow);
			}
		}
		return deviceFlowList;
	}
}
