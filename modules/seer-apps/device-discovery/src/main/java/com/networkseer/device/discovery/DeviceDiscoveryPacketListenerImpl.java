package com.networkseer.device.discovery;
import com.networkseer.common.packet.PacketConstants;
import com.networkseer.device.discovery.dhcp.DHCP;
import com.networkseer.device.discovery.dhcp.DHCPOption;
import com.networkseer.sdn.controller.mgt.OFController;
import com.networkseer.common.openflow.OFFlow;
import com.networkseer.sdn.controller.mgt.exception.OFControllerException;
import com.networkseer.seer.mgt.dto.Device.Status;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.networkseer.common.packet.PacketListener;
import com.networkseer.common.packet.SeerPacket;
import com.networkseer.device.discovery.internal.DeviceDiscoveryDataHolder;
import com.networkseer.seer.mgt.dto.Device;
import com.networkseer.seer.mgt.dto.DeviceRecord;
import com.networkseer.seer.mgt.exception.SeerManagementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeviceDiscoveryPacketListenerImpl implements PacketListener {
	private static final Logger log = LoggerFactory.getLogger(DeviceDiscoveryPacketListenerImpl.class);

	private LoadingCache<DeviceIdentifier, Device.Status> deviceCache;
	LinkedList<DeviceIdentifier> deviceQueue = new LinkedList<DeviceIdentifier>();
	private static final int COMMON_WAN_PRIORITY = 1000;
	private static final int COMMON_LAN_PRIORITY = 100;
	private static final int COMMON_DROP_PRIORITY = 1100;

	public DeviceDiscoveryPacketListenerImpl() {
		deviceCache = CacheBuilder.newBuilder()
				.concurrencyLevel(4)
				.weakKeys()
				.maximumSize(10000)
				.expireAfterWrite(3, TimeUnit.MINUTES)
				.build(new CacheLoader<DeviceIdentifier, Device.Status>() {
							@Override
							public Device.Status load(DeviceIdentifier key) throws Exception {
								return null;
							}
						});

		ScheduledExecutorService deviceExecutor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> {
			DeviceIdentifier deviceIdentifier = deviceQueue.remove();

			while(deviceIdentifier != null) {
				try {
					Status cacheStatus = deviceCache.get(deviceIdentifier);
					if (cacheStatus == null || cacheStatus == Status.BLOCKED
							|| (cacheStatus == Status.CREATED && deviceIdentifier.getDeviceName() != null)) {
						DeviceRecord deviceRecord = DeviceDiscoveryDataHolder.getSeerMgtService()
								.getDeviceRecord(deviceIdentifier.getVlanId(), deviceIdentifier.getDeviceMac());
						Status currentStatus;
						if (deviceRecord.getDevice() == null) {
							//add device with created status.
							Device device = new Device();
							device.setMac(deviceIdentifier.getDeviceMac());
							device.setName(deviceIdentifier.getDeviceName());
							device.setSwitchId(deviceRecord.getaSwitch().getId());
							if (deviceIdentifier.getDeviceName() != null) {
								device.setStatus(Status.IDENTIFIED);
							} else {
								device.setStatus(Status.CREATED);
							}
							currentStatus = device.getStatus();
							DeviceDiscoveryDataHolder.getSeerMgtService().addDevice(device);
						} else if (deviceRecord.getDevice().getStatus() == Status.CREATED
								&& deviceIdentifier.getDeviceName() != null) {
							DeviceDiscoveryDataHolder.getSeerMgtService()
									.updateDeviceNameAndStatus(deviceIdentifier.getDeviceName(),
									Status.IDENTIFIED, deviceRecord.getDevice().getId());
							currentStatus = Status.IDENTIFIED;
						} else {
							currentStatus = deviceRecord.getDevice().getStatus();
						}

						Status deviceStatus = deviceCache.get(deviceIdentifier);
						if (deviceStatus == null) {
							if (!deviceRecord.getGroup().isSecurityAppEnabled()){
								deviceCache.put(deviceIdentifier, currentStatus);
								OFController ofController = DeviceDiscoveryDataHolder.getOfController();
								if (currentStatus == Status.CREATED || currentStatus == Status.IDENTIFIED) {
									List<OFFlow> ofFlowList = new ArrayList<>();
									String switchMac = deviceRecord.getaSwitch().getDpId().substring(6);
									String deviceMac =deviceIdentifier.getDeviceMac();
									OFFlow ofFlow = new OFFlow();
									ofFlow.setSrcMac(deviceMac);
									ofFlow.setDstMac(switchMac);
									ofFlow.setPriority(COMMON_WAN_PRIORITY);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlowList.add(ofFlow);

									ofFlow = new OFFlow();
									ofFlow.setSrcMac(switchMac);
									ofFlow.setDstMac(deviceMac);
									ofFlow.setPriority(COMMON_WAN_PRIORITY);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlowList.add(ofFlow);

									ofFlow = new OFFlow();
									ofFlow.setDstMac(deviceMac);
									ofFlow.setPriority(COMMON_LAN_PRIORITY);
									ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
									ofFlowList.add(ofFlow);
									ofController.addFlows(deviceRecord.getaSwitch().getDpId(), ofFlowList);
								} else {
									List<OFFlow> ofFlowList = new ArrayList<>();
									String deviceMac =deviceIdentifier.getDeviceMac();
									OFFlow ofFlow = new OFFlow();
									ofFlow.setSrcMac(deviceMac);
									ofFlow.setPriority(COMMON_DROP_PRIORITY);
									ofFlow.setOfAction(OFFlow.OFAction.DROP);
									ofFlowList.add(ofFlow);

									ofFlow = new OFFlow();
									ofFlow.setDstMac(deviceMac);
									ofFlow.setPriority(COMMON_DROP_PRIORITY);
									ofFlow.setOfAction(OFFlow.OFAction.DROP);
									ofFlowList.add(ofFlow);
									ofController.addFlows(deviceRecord.getaSwitch().getDpId(), ofFlowList);
								}
							}


						}

					}


				} catch (SeerManagementException |ExecutionException | OFControllerException e) {
					log.error(e.getMessage(), e);
				}
				deviceIdentifier = deviceQueue.remove();
			}
		};
		deviceExecutor.scheduleWithFixedDelay(task, 10, 20, TimeUnit.SECONDS);
	}

	@Override
	public void processPacket(SeerPacket seerPacket) {
		String srcMac = seerPacket.getSrcMac();
		String destMac = seerPacket.getDstMac();
		String deviceName = extractDHCPName(seerPacket);

		if (!srcMac.contains(seerPacket.getVlanId())) {
			DeviceIdentifier deviceIdentifier = new DeviceIdentifier(seerPacket.getVlanId(), srcMac);
			deviceIdentifier.setDeviceName(deviceName);
			addToQueue(deviceIdentifier);
		}

		if (!destMac.contains(seerPacket.getVlanId()) && !seerPacket.isDstIgnore()) {
			DeviceIdentifier deviceIdentifier = new DeviceIdentifier(seerPacket.getVlanId(), destMac);
			deviceIdentifier.setDeviceName(deviceName);
			addToQueue(deviceIdentifier);
		}
	}

	private String extractDHCPName(SeerPacket seerPacket) {
		if (PacketConstants.UDP_PROTO.equals(seerPacket.getIpProto()) && seerPacket.isDstIgnore()
				&& PacketConstants.DHCP_PORT.equals(seerPacket.getDstPort())) {
			seerPacket.getPayload();
			DHCP dhcp = new DHCP();
			dhcp.deserialize(seerPacket.getPayload(), 0, seerPacket.getPayload().length );
			byte opcode = dhcp.getOpCode();
			if (opcode == DHCP.OPCODE_REQUEST) {
				DHCPOption dhcpOption = dhcp.getOption(
						DHCP.DHCPOptionCode.OptionCode_Hostname);
				if (dhcpOption != null) {
					return new String(dhcpOption.getData());
				}
			}
		}
		return null;
	}


	private void addToQueue(DeviceIdentifier deviceIdentifier) {
		Status cacheStatus = null;
		try {
			cacheStatus = deviceCache.get(deviceIdentifier);
			if (cacheStatus == null || cacheStatus == Status.BLOCKED
					|| (cacheStatus == Status.CREATED && deviceIdentifier.getDeviceName() != null)) {
				deviceQueue.add(deviceIdentifier);
			}
		} catch (ExecutionException e) {
			log.error(e.getMessage(), e);
		}
	}

	public class DeviceIdentifier {

		private String vlanId;
		private String deviceMac;
		private String deviceName;

		public DeviceIdentifier(String vlanId, String deviceMac) {
			this.vlanId = vlanId;
			this.deviceMac = deviceMac;
		}

		public String getDeviceName() {
			return deviceName;
		}

		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}

		public String getVlanId() {
			return vlanId;
		}

		public void setVlanId(String vlanId) {
			this.vlanId = vlanId;
		}

		public String getDeviceMac() {
			return deviceMac;
		}

		public void setDeviceMac(String deviceMac) {
			this.deviceMac = deviceMac;
		}

		@Override
		public int hashCode() {
			int result = this.vlanId.hashCode();
			result = 31 * result + ("@" + this.deviceMac).hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof DeviceIdentifier) && vlanId.equals(
					((DeviceIdentifier) obj).vlanId) && deviceMac.equals(
					((DeviceIdentifier) obj).deviceMac);
		}
	}

}
