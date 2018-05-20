package com.mudie.mud.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mudie.common.DeviceMudWrapper;
import com.mudie.common.SeerDirectory;
import com.mudie.common.SeerUtil;
import com.mudie.common.mud.*;
import com.mudie.common.openflow.OFFlow;
import com.mudie.common.packet.PacketConstants;
import com.mudie.mud.collector.internal.MUDCollectorDataHolder;
import com.mudie.mud.collector.mudflowdto.DeviceMUDFlowMap;
import com.mudie.mud.collector.mudflowdto.MudFeatureWrapper;
import com.mudie.sdn.controller.mgt.exception.OFControllerException;
import com.mudie.seer.mgt.dto.Device;
import com.mudie.seer.mgt.dto.DeviceRecord;
import com.mudie.seer.mgt.exception.SeerManagementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MudieStatsCollector {
    private static final Logger log = LoggerFactory.getLogger(MudieStatsCollector.class);
    private static final int FIXED_LOCAL_COMMUNICATION = 5;
    private static final int DEFAULT_LOCAL_COMMUNICATION = 4;
    private static final int FIXED_INTERNET_COMMUNICATION = 10;
    private static final int FIXED_LOCAL_CONTROLLER_COMMUNICATION = 11;
    private static final int DEFAULT_INTERNET_COMMUNICATION = 9;
    private static final int DYNAMIC_INTERNET_COMMUNICATION = 15000;
    private static final String MUD_URN = "urn:ietf:params:mud";
    private static String FROM_LOCAL_FEATURE_NAME = "FromLocal%sPort%s";
    private static String TO_LOCAL_FEATURE_NAME = "ToLocal%sPort%s";
    private static String FROM_INTERNET_FEATURE_NAME = "FromInternet%sPort%s";
    private static String TO_INTERNET_FEATURE_NAME = "ToInternet%sPort%s";
    private static String TCP = "Tcp";
    private static String UDP = "Udp";
    private static String ICMP = "Icmp";
    private static String ARP = "Arp";
    private Map<String, MudFeatureWrapper> featureSet = new HashMap();

    public MudieStatsCollector() {
        ScheduledExecutorService deviceExecutor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                //TODO this needs to be optimized....
                List<DeviceRecord> records = MUDCollectorDataHolder.getSeerMgtService()
                        .getAllDevices(Device.Status.IDENTIFIED);
                if (records != null && records.size() > 0) {
                    records.stream().forEach(deviceRecord -> {
                        try {
                            if (featureSet.get(deviceRecord.getDevice().getMac()) != null) {
                                DeviceMudWrapper deviceMudWrapper = new DeviceMudWrapper(deviceRecord.getDevice().getProperty());
                                DeviceMUDFlowMap deviceMUDFlowMap = addMudConfigs(deviceMudWrapper.getMudProfile(),
                                        deviceRecord.getDevice().getMac(),
                                        SeerUtil.getSwitchMacFromDpID(deviceRecord.getaSwitch().getDpId()));
                                Set<OFFlow> staticOfflows = new LinkedHashSet<>();
                                staticOfflows.addAll(deviceMUDFlowMap.getFromInternetStaticFlows());
                                staticOfflows.addAll(deviceMUDFlowMap.getToInternetStaticFlows());
                                staticOfflows.addAll(deviceMUDFlowMap.getFromLocalStaticFlows());
                                staticOfflows.addAll(deviceMUDFlowMap.getToLocalStaticFlows());

                                Set<OFFlow> dynamicOfflows = new LinkedHashSet<>();
                                dynamicOfflows.addAll(deviceMUDFlowMap.getFromInternetDynamicFlows());
                                dynamicOfflows.addAll(deviceMUDFlowMap.getToInternetDynamicFlows());

                                String row = "Timestamp";
                                int flowOrder[] = new int[staticOfflows.size() + dynamicOfflows.size()];
                                int i = 0;

                                for (OFFlow ofFlow : staticOfflows) {
                                    row = row + "," + ofFlow.getName() + "Packet," + ofFlow.getName() + "Byte";
                                    flowOrder[i] = ofFlow.hashCode();
                                    i++;
                                }
                                for (OFFlow ofFlow : dynamicOfflows) {
                                    row = row + "," + ofFlow.getName() + "Packet," + ofFlow.getName() + "Byte";
                                    flowOrder[i] = ofFlow.hashCode();
                                    i++;
                                }
                                MudFeatureWrapper mudieFeatureWrapper = new MudFeatureWrapper(staticOfflows, dynamicOfflows);
                                mudieFeatureWrapper.setFlowOrder(flowOrder);
                                featureSet.put(deviceRecord.getDevice().getMac(), mudieFeatureWrapper);
                                String path = SeerDirectory.getLogDirectory() + File.separator +
                                        deviceRecord.getDevice().getMac().replace(":", "") + "_flowstats.csv";
                                logData(path, row, true);
                            }
                            logDeviceData(deviceRecord.getDevice().getMac(), deviceRecord.getaSwitch().getDpId());
                        } catch (IOException e) {
                            log.error("Failed to process mud profile for device " + deviceRecord.getDevice().getMac(), e);
                        } catch (OFControllerException e) {
                            log.error("Failed to retrieve flow stats for device " + deviceRecord.getDevice().getMac(), e);
                        }


                    });
                }
            } catch (SeerManagementException e) {
                log.error("Failed to retrieve device data.", e);
            }

        };
        deviceExecutor.scheduleWithFixedDelay(task, 10, MUDCollectorDataHolder.getMudConfig()
                .getSummerizationTimeInSeconds(), TimeUnit.SECONDS);
    }

    private void logDeviceData(String deviceMac, String dpId) throws OFControllerException {
        List<OFFlow> deviceFlowStats = MUDCollectorDataHolder.getOfController().getFlowStats(dpId);
//        List<OFFlow> deviceFlowStats = new ArrayList<>();
//        for (OFFlow currentFlow : flowStats) {
//            if (!currentFlow.getSrcMac().equals(deviceMac) && !currentFlow.getDstMac().equals(deviceMac)) {
//                continue;
//            }
//            deviceFlowStats.add(currentFlow);
//        }

        MudFeatureWrapper mudieFeatureWrapper = featureSet.get(deviceMac);
        Map<Integer, OFFlow> currentStaticFlowRecords = new HashMap<>();
        Map<Integer, OFFlow> currentDynamicFlowRecords = new HashMap<>();
        for (OFFlow currentFlow : deviceFlowStats) {
            OFFlow tmpFlow = currentFlow.copy();
            tmpFlow.setPacketCount(currentFlow.getPacketCount());
            tmpFlow.setByteCount(currentFlow.getByteCount());
            currentFlow = tmpFlow;
            if (mudieFeatureWrapper.getLastStaticFlowRecords() == null) {
                OFFlow flow = mudieFeatureWrapper.getStaticFlows().get(currentFlow.hashCode());
                if (flow != null) {
                    currentStaticFlowRecords.put(currentFlow.hashCode(), currentFlow);
                } else {
                    flow = getMatchingFlow(currentFlow, mudieFeatureWrapper.getDynamicFlows());
                    if (flow == null) {
                        continue;
                    }

                    currentDynamicFlowRecords.put(currentFlow.hashCode(), currentFlow);
                }

            } else {
                OFFlow flow = mudieFeatureWrapper.getStaticFlows().get(currentFlow.hashCode());
                if (flow != null) {
                    OFFlow lastFlowRecord = mudieFeatureWrapper.getLastStaticFlowRecords().get(currentFlow.hashCode());
                    if (lastFlowRecord != null && currentFlow.getPacketCount() - lastFlowRecord.getPacketCount() >= 0) {
                        flow.setPacketCount(currentFlow.getPacketCount() - lastFlowRecord.getPacketCount());
                        flow.setByteCount(currentFlow.getByteCount() - lastFlowRecord.getByteCount());
                    } else {
                        flow.setPacketCount(currentFlow.getPacketCount());
                        flow.setByteCount(currentFlow.getByteCount());
                    }
                    currentStaticFlowRecords.put(currentFlow.hashCode(),currentFlow);
                } else {
                    flow = getMatchingFlow(currentFlow, mudieFeatureWrapper.getDynamicFlows());
                    if (flow == null) {
                        continue;
                    }
                    OFFlow lastFlowRecord = getMatchingFlow(currentFlow,mudieFeatureWrapper.getLastReactiveFlowRecords());
                    if (lastFlowRecord!= null && currentFlow.getPacketCount() - lastFlowRecord.getPacketCount() >= 0) {
                        flow.setPacketCount(flow.getPacketCount() + currentFlow.getPacketCount() - lastFlowRecord.getPacketCount());
                        flow.setByteCount(flow.getByteCount() + currentFlow.getByteCount()
                                - lastFlowRecord.getByteCount());
                    } else {
                        flow.setPacketCount(flow.getPacketCount() + currentFlow.getPacketCount());
                        flow.setByteCount(flow.getByteCount() + currentFlow.getByteCount());
                    }
                    currentDynamicFlowRecords.put(currentFlow.hashCode(), currentFlow);
                }
            }
        }

        if (mudieFeatureWrapper.getLastStaticFlowRecords() != null) {
            //log data here.
            publishData(deviceMac, mudieFeatureWrapper);
            mudieFeatureWrapper.resetDynamicFlowMetrics();

        }
        mudieFeatureWrapper.setLastStaticFlowRecords(currentStaticFlowRecords);
        mudieFeatureWrapper.setLastReactiveFlowRecords(currentDynamicFlowRecords);
    }

    private void publishData(String deviceMac, MudFeatureWrapper mudieFeatureWrapper) {
        String row =  "" + System.currentTimeMillis();
        for(int key : mudieFeatureWrapper.getFlowOrder()) {
            if (mudieFeatureWrapper.getStaticFlows().containsKey(key)) {
                row = row + "," + mudieFeatureWrapper.getStaticFlows().get(key).getPacketCount()
                        + "," + mudieFeatureWrapper.getStaticFlows().get(key).getByteCount();
            } else {
                row = row + "," + mudieFeatureWrapper.getDynamicFlows().get(key).getPacketCount()
                        + "," + mudieFeatureWrapper.getDynamicFlows().get(key).getByteCount();
            }

        }
        String path = SeerDirectory.getLogDirectory() + File.separator +
                deviceMac.replace(":","") + "_flowstats.csv";
        logData(path, row);
    }

    private DeviceMUDFlowMap addMudConfigs(String mudPayload, String deviceMac, String switchMac) throws IOException {
        DeviceMUDFlowMap deviceMUDFlowMap = processMUD(deviceMac, switchMac, mudPayload);
        return deviceMUDFlowMap;
    }

    private DeviceMUDFlowMap processMUD(String deviceMac, String switchMac, String mudPayload) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MudSpec mudSpec = mapper.readValue(mudPayload, MudSpec.class);
        DeviceMUDFlowMap deviceMUDFlowMap = loadMudSpec(deviceMac, switchMac, mudSpec);
        installInternetNetworkRules(deviceMac, switchMac, deviceMUDFlowMap);
        installLocalNetworkRules(deviceMac, deviceMUDFlowMap);
        return deviceMUDFlowMap;
    }

    private DeviceMUDFlowMap loadMudSpec(String deviceMac, String switchMac, MudSpec mudSpec) {
        List<String> fromDevicePolicyNames = new ArrayList<>();
        List<String> toDevicePolicyNames = new ArrayList<>();
        for (AccessDTO accessDTO : mudSpec.getIetfMud().getFromDevicePolicy().getAccessList().getAccessDTOList()) {
            fromDevicePolicyNames.add(accessDTO.getName());
        }

        for (AccessDTO accessDTO : mudSpec.getIetfMud().getToDevicePolicy().getAccessList().getAccessDTOList()) {
            toDevicePolicyNames.add(accessDTO.getName());
        }

        List<OFFlow> fromInternetDynamicFlows = new ArrayList<>();
        List<OFFlow> toInternetDynamicFlows = new ArrayList<>();
        List<OFFlow> fromInternetStaticFlows = new ArrayList<>();
        List<OFFlow> toInternetStaticFlows = new ArrayList<>();
        List<OFFlow> fromLocalStaticFlows = new ArrayList<>();
        List<OFFlow> toLocalStaticFlows = new ArrayList<>();

        for (AccessControlListHolder accessControlListHolder : mudSpec.getAccessControlList().getAccessControlListHolder()) {
            if (fromDevicePolicyNames.contains(accessControlListHolder.getName())) {
                for (Ace ace : accessControlListHolder.getAces().getAceList()) {
                    Match match = ace.getMatches();

                    //filter local
                    if (match.getIetfMudMatch() != null && (match.getIetfMudMatch().getController() != null
                            || match.getIetfMudMatch().getLocalNetworks() != null)) {

                        //install local network related rules here
                        OFFlow ofFlow = new OFFlow();
                        ofFlow.setSrcMac(deviceMac);
                        String etherType = match.getEthMatch() == null ? PacketConstants.ETH_TYPE_IPV4 : match.getEthMatch()
                                .getEtherType();
                        ofFlow.setEthType(etherType);
                        ofFlow.setPriority(FIXED_LOCAL_COMMUNICATION);
                        if (match.getIpv4Match() != null &&
                                match.getIpv4Match().getProtocol() != 0) {

                            ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
                            ofFlow.setIpProto("" + match.getIpv4Match().getProtocol());
                        }

                        if (match.getIpv6Match() != null) {
                            ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV6);
                            ofFlow.setIpProto("" + match.getIpv6Match().getProtocol());
                        }



                        if (match.getEthMatch() != null) {
                            if (match.getEthMatch().getEtherType() != null) {
                                ofFlow.setEthType(match.getEthMatch().getEtherType());
                            }
                            if (match.getEthMatch().getSrcMacAddress() != null) {
                                ofFlow.setSrcMac(match.getEthMatch().getSrcMacAddress());
                            }
                            if (match.getEthMatch().getDstMacAddress() != null) {
                                ofFlow.setDstMac(match.getEthMatch().getDstMacAddress());
                            }

                        }

                        if (ofFlow.getIpProto().equals(PacketConstants.ICMP_PROTO)) {
                            ofFlow.setName(String.format(TO_LOCAL_FEATURE_NAME, ICMP, "All"));
                        }

                        if (!ofFlow.getEthType().equals(PacketConstants.ETH_TYPE_IPV4) &&
                                !ofFlow.getEthType().equals(PacketConstants.ETH_TYPE_IPV6)) {
                            ofFlow.setName(String.format(TO_LOCAL_FEATURE_NAME, ofFlow.getEthType(), "All"));

                        } else {
                            ofFlow.setName(String.format(TO_LOCAL_FEATURE_NAME, getProto(ofFlow.getIpProto()), "All"));
                        }

                        //tcp
                        if (match.getTcpMatch() != null &&
                                match.getTcpMatch().getDestinationPortMatch() != null
                                && match.getTcpMatch().getDestinationPortMatch().getPort() != 0) {
                            ofFlow.setDstPort("" + match.getTcpMatch().getDestinationPortMatch().getPort());
                            ofFlow.setName(String.format(TO_LOCAL_FEATURE_NAME, TCP, match.getTcpMatch()
                                    .getDestinationPortMatch().getPort()));
                        }

                        if (match.getTcpMatch() != null && match.getTcpMatch().getSourcePortMatch() != null
                                && match.getTcpMatch().getSourcePortMatch().getPort() != 0) {
                            ofFlow.setSrcPort("" + match.getTcpMatch().getSourcePortMatch().getPort());
                            ofFlow.setName(String.format(TO_LOCAL_FEATURE_NAME, TCP, match.getTcpMatch()
                                    .getSourcePortMatch().getPort()));
                        }
                        //udp
                        if (match.getUdpMatch() != null && match.getUdpMatch().getDestinationPortMatch() != null
                                && match.getUdpMatch().getDestinationPortMatch().getPort() != 0) {
                            ofFlow.setDstPort("" + match.getUdpMatch().getDestinationPortMatch().getPort());
                            ofFlow.setName(String.format(TO_LOCAL_FEATURE_NAME, UDP, match.getUdpMatch()
                                    .getDestinationPortMatch().getPort()));
                        }

                        if (match.getUdpMatch() != null && match.getUdpMatch().getSourcePortMatch() != null
                                && match.getUdpMatch().getSourcePortMatch().getPort() != 0) {
                            ofFlow.setSrcPort("" + match.getUdpMatch().getSourcePortMatch().getPort());
                            ofFlow.setName(String.format(TO_LOCAL_FEATURE_NAME, UDP, match.getUdpMatch()
                                    .getSourcePortMatch().getPort()));
                        }

                        if ((match.getIpv4Match() != null && match.getIpv4Match().getDestinationIp() != null)) {
                            ofFlow.setDstIp(match.getIpv4Match().getDestinationIp());
                            ofFlow.setName(ofFlow.getName() + "IP" + match.getIpv4Match().getDestinationIp());
                        } else if (match.getIpv6Match() != null && match.getIpv6Match().getDestinationIp() != null) {
                            ofFlow.setName(ofFlow.getName() + "IP" + match.getIpv6Match().getDestinationIp());
                            ofFlow.setDstIp(match.getIpv6Match().getDestinationIp());
                        } else if (match.getIetfMudMatch().getController() != null &&
                                (match.getIetfMudMatch().getController().contains(MUD_URN))) {
                            String ip = MUDCollectorDataHolder.getMUDControllerValue(match.getIetfMudMatch().getController());
                            ofFlow.setDstIp(ip);
                            ofFlow.setPriority(FIXED_LOCAL_CONTROLLER_COMMUNICATION);
                            ofFlow.setName(ofFlow.getName() + "IP" + ip);
                        }

                        ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
                        toLocalStaticFlows.add(ofFlow);

                    } else {
                        OFFlow ofFlow = new OFFlow();
                        ofFlow.setSrcMac(deviceMac);
                        ofFlow.setDstMac(switchMac);

                        String etherType = match.getEthMatch() == null ? PacketConstants.ETH_TYPE_IPV4 : match.getEthMatch()
                                .getEtherType();
                        ofFlow.setEthType(etherType);
                        if (match.getIpv4Match() != null &&
                                match.getIpv4Match().getProtocol() != 0) {
                            ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
                            ofFlow.setIpProto("" + match.getIpv4Match().getProtocol());
                        }

                        if (match.getIpv6Match() != null) {
                            ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV6);
                            ofFlow.setIpProto("" + match.getIpv6Match().getProtocol());
                        }

                        if (ofFlow.getIpProto().equals(PacketConstants.ICMP_PROTO)) {
                            ofFlow.setName(String.format(TO_INTERNET_FEATURE_NAME, ICMP, "All"));
                        }

                        if (!ofFlow.getEthType().equals(PacketConstants.ETH_TYPE_IPV4) &&
                                !ofFlow.getEthType().equals(PacketConstants.ETH_TYPE_IPV6)) {
                            ofFlow.setName(String.format(TO_INTERNET_FEATURE_NAME, ofFlow.getEthType(), "All"));

                        } else {
                            ofFlow.setName(String.format(TO_INTERNET_FEATURE_NAME, getProto(getProto(ofFlow.getIpProto())), "All"));
                        }


                        //tcp
                        if (match.getTcpMatch() != null && match.getTcpMatch().getDestinationPortMatch() != null
                                && match.getTcpMatch().getDestinationPortMatch().getPort() != 0) {
                            ofFlow.setDstPort("" + match.getTcpMatch().getDestinationPortMatch().getPort());
                            ofFlow.setName(String.format(TO_INTERNET_FEATURE_NAME, TCP, match.getTcpMatch()
                                    .getDestinationPortMatch().getPort()));
                        }

                        if (match.getTcpMatch() != null && match.getTcpMatch().getSourcePortMatch() != null
                                && match.getTcpMatch().getSourcePortMatch().getPort() != 0) {
                            ofFlow.setSrcPort("" + match.getTcpMatch().getSourcePortMatch().getPort());
                            ofFlow.setName(String.format(TO_INTERNET_FEATURE_NAME, TCP, match.getTcpMatch()
                                    .getSourcePortMatch().getPort()));
                        }
                        //udp
                        if (match.getUdpMatch() != null && match.getUdpMatch().getDestinationPortMatch() != null
                                && match.getUdpMatch().getDestinationPortMatch().getPort() != 0) {
                            ofFlow.setDstPort("" + match.getUdpMatch().getDestinationPortMatch().getPort());
                            ofFlow.setName(String.format(TO_INTERNET_FEATURE_NAME, UDP, match.getUdpMatch()
                                    .getDestinationPortMatch().getPort()));
                        }

                        if (match.getUdpMatch() != null && match.getUdpMatch().getSourcePortMatch() != null
                                && match.getUdpMatch().getSourcePortMatch().getPort() != 0) {
                            ofFlow.setSrcPort("" + match.getUdpMatch().getSourcePortMatch().getPort());
                            ofFlow.setName(String.format(TO_INTERNET_FEATURE_NAME, UDP, match.getUdpMatch()
                                    .getSourcePortMatch().getPort()));
                        }

                        if (match.getIpv4Match() != null && match.getIpv4Match().getDestinationIp() != null) {
                            ofFlow.setDstIp(match.getIpv4Match().getDestinationIp());
                            ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
                            ofFlow.setName(ofFlow.getName() + "IP" + match.getIpv4Match().getDestinationIp());
                        } else if (match.getIpv4Match() != null && match.getIpv4Match().getDstDnsName() != null) {
                            ofFlow.setPriority(DYNAMIC_INTERNET_COMMUNICATION);
                        } else if (match.getIpv6Match() != null &&
                                match.getIpv6Match().getDestinationIp() != null) {
                            ofFlow.setDstIp(match.getIpv6Match().getDestinationIp());
                            ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
                            ofFlow.setName(ofFlow.getName() + "IP" + match.getIpv6Match().getDestinationIp());
                        } else if (match.getIpv6Match() != null &&
                                match.getIpv6Match().getDstDnsName() != null) {
                            ofFlow.setPriority(DYNAMIC_INTERNET_COMMUNICATION);
                        } else {
                            ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
                        }
                        ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
                        if (FIXED_INTERNET_COMMUNICATION == ofFlow.getPriority()) {
                            toInternetStaticFlows.add(ofFlow);
                        } else {
                            toInternetDynamicFlows.add(ofFlow);
                        }
                    }
                }
            } else if (toDevicePolicyNames.contains(accessControlListHolder.getName())) {

                for (Ace ace : accessControlListHolder.getAces().getAceList()) {
                    Match match = ace.getMatches();
                    //filter local
                    if (match.getIetfMudMatch() != null && (match.getIetfMudMatch().getController() != null
                            || match.getIetfMudMatch().getLocalNetworks() != null)) {
                        //install local network related rules here
                        OFFlow ofFlow = new OFFlow();
                        ofFlow.setDstMac(deviceMac);
                        ofFlow.setPriority(FIXED_LOCAL_COMMUNICATION);
                        ofFlow.setOfAction(OFFlow.OFAction.NORMAL);

                        if (match.getIpv4Match() != null &&
                                match.getIpv4Match().getProtocol() != 0) {
                            ofFlow.setIpProto("" + match.getIpv4Match().getProtocol());
                            ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
                        }

                        if (match.getIpv6Match() != null) {
                            ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV6);
                            ofFlow.setIpProto("" + match.getIpv6Match().getProtocol());
                        }

                        if (ofFlow.getIpProto().equals(PacketConstants.ICMP_PROTO)) {
                            ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, ICMP, "All"));
                        }

                        if (!ofFlow.getEthType().equals(PacketConstants.ETH_TYPE_IPV4) &&
                                !ofFlow.getEthType().equals(PacketConstants.ETH_TYPE_IPV6)) {
                            ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, ofFlow.getEthType(), "All"));

                        } else {
                            ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, getProto(ofFlow.getIpProto()), "All"));
                        }

                        //tcp
                        if (match.getTcpMatch() != null &&
                                match.getTcpMatch().getDestinationPortMatch() != null
                                && match.getTcpMatch().getDestinationPortMatch().getPort() != 0) {
                            ofFlow.setDstPort("" + match.getTcpMatch().getDestinationPortMatch().getPort());
                            ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, TCP, match.getTcpMatch()
                                    .getDestinationPortMatch().getPort()));
                        }

                        if (match.getTcpMatch() != null &&
                                match.getTcpMatch().getSourcePortMatch() != null
                                && match.getTcpMatch().getSourcePortMatch().getPort() != 0) {
                            ofFlow.setSrcPort("" + match.getTcpMatch().getSourcePortMatch().getPort());
                            ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, TCP, match.getTcpMatch()
                                    .getSourcePortMatch().getPort()));
                        }
                        //udp
                        if (match.getUdpMatch() != null &&
                                match.getUdpMatch().getDestinationPortMatch() != null
                                && match.getUdpMatch().getDestinationPortMatch().getPort() != 0) {
                            ofFlow.setDstPort("" + match.getUdpMatch().getDestinationPortMatch().getPort());
                            ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, UDP, match.getUdpMatch()
                                    .getDestinationPortMatch().getPort()));
                        }

                        if (match.getUdpMatch() != null &&
                                match.getUdpMatch().getSourcePortMatch() != null
                                && match.getUdpMatch().getSourcePortMatch().getPort() != 0) {
                            ofFlow.setSrcPort("" + match.getUdpMatch().getSourcePortMatch().getPort());
                            ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, UDP, match.getUdpMatch()
                                    .getSourcePortMatch().getPort()));
                            if (ofFlow.getSrcPort().equals(PacketConstants.DNS_PORT)) {
                                ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
                            }
                        }


                        if ((match.getIpv4Match() != null && match.getIpv4Match().getSourceIp() != null)) {
                            ofFlow.setSrcIp(match.getIpv4Match().getDestinationIp());
                            ofFlow.setName(ofFlow.getName() + "IP" + match.getIpv4Match().getSourceIp());
                        } else if (match.getIpv6Match() != null && match.getIpv6Match().getSourceIp() != null) {
                            ofFlow.setSrcIp(match.getIpv6Match().getSourceIp());
                            ofFlow.setName(ofFlow.getName() + "IP" + match.getIpv6Match().getSourceIp());
                        } else if (match.getIetfMudMatch().getController() != null &&
                                (match.getIetfMudMatch().getController().contains(MUD_URN))) {
                            String ip = MUDCollectorDataHolder.getMUDControllerValue(match.getIetfMudMatch().getController());
                            ofFlow.setSrcIp(ip);
                            ofFlow.setName(ofFlow.getName() + "IP" + ip);
                            ofFlow.setPriority(FIXED_LOCAL_CONTROLLER_COMMUNICATION);
                        }
                        fromLocalStaticFlows.add(ofFlow);
                    } else {
                        OFFlow ofFlow = new OFFlow();
                        ofFlow.setSrcMac(switchMac);
                        ofFlow.setDstMac(deviceMac);
                        String etherType = match.getEthMatch() == null ? PacketConstants.ETH_TYPE_IPV4 : match.getEthMatch()
                                .getEtherType();
                        ofFlow.setEthType(etherType);
                        ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
                        if (match.getIpv4Match() != null &&
                                match.getIpv4Match().getProtocol() != 0) {

                            ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
                            ofFlow.setIpProto("" + match.getIpv4Match().getProtocol());
                        }

                        if (match.getIpv6Match() != null) {
                            ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV6);
                            ofFlow.setIpProto("" + match.getIpv6Match().getProtocol());
                        }

                        if (ofFlow.getIpProto().equals(PacketConstants.ICMP_PROTO)) {
                            ofFlow.setName(String.format(FROM_INTERNET_FEATURE_NAME, ICMP, "All"));
                        }

                        if (!ofFlow.getEthType().equals(PacketConstants.ETH_TYPE_IPV4) &&
                                !ofFlow.getEthType().equals(PacketConstants.ETH_TYPE_IPV6)) {
                            ofFlow.setName(String.format(FROM_INTERNET_FEATURE_NAME, ofFlow.getEthType(), "All"));

                        } else {
                            ofFlow.setName(String.format(FROM_INTERNET_FEATURE_NAME, getProto(ofFlow.getIpProto()), "All"));

                        }

                        //tcp
                        if (match.getTcpMatch() != null &&
                                match.getTcpMatch().getDestinationPortMatch() != null
                                && match.getTcpMatch().getDestinationPortMatch().getPort() != 0) {
                            ofFlow.setDstPort("" + match.getTcpMatch().getDestinationPortMatch().getPort());
                            ofFlow.setName(String.format(FROM_INTERNET_FEATURE_NAME, TCP, match.getTcpMatch()
                                    .getDestinationPortMatch().getPort()));
                        }

                        if (match.getTcpMatch() != null &&
                                match.getTcpMatch().getSourcePortMatch() != null
                                && match.getTcpMatch().getSourcePortMatch().getPort() != 0) {
                            ofFlow.setSrcPort("" + match.getTcpMatch().getSourcePortMatch().getPort());
                            ofFlow.setName(String.format(FROM_INTERNET_FEATURE_NAME, TCP, match.getTcpMatch()
                                    .getSourcePortMatch().getPort()));
                        }
                        //udp
                        if (match.getUdpMatch() != null &&
                                match.getUdpMatch().getDestinationPortMatch() != null
                                && match.getUdpMatch().getDestinationPortMatch().getPort() != 0) {
                            ofFlow.setDstPort("" + match.getUdpMatch().getDestinationPortMatch().getPort());
                            ofFlow.setName(String.format(FROM_INTERNET_FEATURE_NAME, UDP, match.getUdpMatch()
                                    .getDestinationPortMatch().getPort()));
                        }

                        if (match.getUdpMatch() != null &&
                                match.getUdpMatch().getSourcePortMatch() != null
                                && match.getUdpMatch().getSourcePortMatch().getPort() != 0) {
                            ofFlow.setSrcPort("" + match.getUdpMatch().getSourcePortMatch().getPort());
                            ofFlow.setName(String.format(FROM_INTERNET_FEATURE_NAME, UDP, match.getUdpMatch()
                                    .getSourcePortMatch().getPort()));
                            if (ofFlow.getSrcPort().equals(PacketConstants.DNS_PORT)) {
                                ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
                            }
                        }

                        if (match.getIpv4Match() != null && match.getIpv4Match().getSourceIp() != null) {
                            ofFlow.setSrcIp(match.getIpv4Match().getSourceIp());
                            ofFlow.setName(ofFlow.getName() + "IP" + match.getIpv4Match().getSourceIp());
                            ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
                        } else if (match.getIpv4Match() != null && match.getIpv4Match().getSrcDnsName() != null) {
                            ofFlow.setPriority(DYNAMIC_INTERNET_COMMUNICATION);
                        } else if (match.getIpv6Match() != null && match.getIpv6Match().getSourceIp() != null) {
                            ofFlow.setSrcIp(match.getIpv6Match().getSourceIp());
                            ofFlow.setName(ofFlow.getName() + "IP" + match.getIpv6Match().getSourceIp());
                            ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
                        } else if (match.getIpv6Match() != null && match.getIpv6Match().getSrcDnsName() != null) {
                            ofFlow.setPriority(DYNAMIC_INTERNET_COMMUNICATION);
                        } else {
                            ofFlow.setPriority(FIXED_INTERNET_COMMUNICATION);
                        }

                        if (DYNAMIC_INTERNET_COMMUNICATION == ofFlow.getPriority()) {
                            fromInternetDynamicFlows.add(ofFlow);
                        } else {
                            fromInternetStaticFlows.add(ofFlow);
                        }
                    }
                }
            }
        }

        DeviceMUDFlowMap deviceFlowMap = new DeviceMUDFlowMap();
        deviceFlowMap.setFromInternetDynamicFlows(fromInternetDynamicFlows);
        deviceFlowMap.setFromInternetStaticFlows(fromInternetStaticFlows);
        deviceFlowMap.setToInternetDynamicFlows(toInternetDynamicFlows);
        deviceFlowMap.setToInternetStaticFlows(toInternetStaticFlows);
        deviceFlowMap.setToLocalStaticFlows(toLocalStaticFlows);
        deviceFlowMap.setFromLocalStaticFlows(fromLocalStaticFlows);
        return deviceFlowMap;

    }

    private String getProto(String num) {
        if (PacketConstants.TCP_PROTO.equals(num)) {
            return TCP;
        } else if (PacketConstants.UDP_PROTO.equals(num)) {
            return UDP;
        } else if (PacketConstants.ICMP_PROTO.equals(num)) {
            return ICMP;
        }
        return num;
    }

    private void installLocalNetworkRules(String deviceMac, DeviceMUDFlowMap deviceMUDFlowMap) {
        OFFlow ofFlow = new OFFlow();
        ofFlow.setSrcMac(deviceMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_ARP);
        ofFlow.setName(String.format(TO_LOCAL_FEATURE_NAME, ARP,"All"));
        ofFlow.setPriority(FIXED_LOCAL_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
        deviceMUDFlowMap.getToLocalStaticFlows().add(ofFlow);

        ofFlow = new OFFlow();
        ofFlow.setDstMac(deviceMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_ARP);
        ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, ARP,"All"));
        ofFlow.setPriority(FIXED_LOCAL_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
        deviceMUDFlowMap.getFromLocalStaticFlows().add(ofFlow);

        ofFlow = new OFFlow();
        ofFlow.setDstMac(deviceMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
        ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, ICMP,"All"));
        ofFlow.setIpProto(PacketConstants.ICMP_PROTO);
        ofFlow.setPriority(DEFAULT_LOCAL_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
        deviceMUDFlowMap.getFromLocalStaticFlows().add(ofFlow);

        ofFlow = new OFFlow();
        ofFlow.setDstMac(deviceMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
        ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, TCP,"All"));
        ofFlow.setIpProto(PacketConstants.TCP_PROTO);
        ofFlow.setPriority(DEFAULT_LOCAL_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
        deviceMUDFlowMap.getFromLocalStaticFlows().add(ofFlow);

        ofFlow = new OFFlow();
        ofFlow.setDstMac(deviceMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
        ofFlow.setName(String.format(FROM_LOCAL_FEATURE_NAME, UDP,"All"));
        ofFlow.setIpProto(PacketConstants.UDP_PROTO);
        ofFlow.setPriority(DEFAULT_LOCAL_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
        deviceMUDFlowMap.getFromLocalStaticFlows().add(ofFlow);
    }

    private void installInternetNetworkRules(String deviceMac, String switchMac, DeviceMUDFlowMap deviceMUDFlowMap) {

        OFFlow ofFlow = new OFFlow();
        ofFlow.setSrcMac(switchMac);
        ofFlow.setDstMac(deviceMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
        ofFlow.setName(String.format(FROM_INTERNET_FEATURE_NAME, TCP,"All"));
        ofFlow.setIpProto(PacketConstants.TCP_PROTO);
        ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
        deviceMUDFlowMap.getFromInternetStaticFlows().add(ofFlow);

        ofFlow = new OFFlow();
        ofFlow.setSrcMac(switchMac);
        ofFlow.setDstMac(deviceMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
        ofFlow.setIpProto(PacketConstants.UDP_PROTO);
        ofFlow.setName(String.format(FROM_INTERNET_FEATURE_NAME, UDP,"All"));
        ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
        deviceMUDFlowMap.getFromInternetStaticFlows().add(ofFlow);

        ofFlow = new OFFlow();
        ofFlow.setSrcMac(deviceMac);
        ofFlow.setDstMac(switchMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
        ofFlow.setIpProto(PacketConstants.ICMP_PROTO);
        ofFlow.setName(String.format(FROM_INTERNET_FEATURE_NAME, ICMP,"All"));
        ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
        deviceMUDFlowMap.getToInternetStaticFlows().add(ofFlow);

        ofFlow = new OFFlow();
        ofFlow.setSrcMac(deviceMac);
        ofFlow.setDstMac(switchMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
        ofFlow.setIpProto(PacketConstants.UDP_PROTO);
        ofFlow.setName(String.format(TO_INTERNET_FEATURE_NAME, UDP,"All"));
        ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
        deviceMUDFlowMap.getToInternetStaticFlows().add(ofFlow);

        ofFlow = new OFFlow();
        ofFlow.setSrcMac(deviceMac);
        ofFlow.setDstMac(switchMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
        ofFlow.setIpProto(PacketConstants.TCP_PROTO);
        ofFlow.setName(String.format(TO_INTERNET_FEATURE_NAME, TCP,"All"));
        ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.MIRROR_TO_VXLAN);
        deviceMUDFlowMap.getToInternetStaticFlows().add(ofFlow);

        ofFlow = new OFFlow();
        ofFlow.setSrcMac(switchMac);
        ofFlow.setDstMac(deviceMac);
        ofFlow.setEthType(PacketConstants.ETH_TYPE_IPV4);
        ofFlow.setIpProto(PacketConstants.ICMP_PROTO);
        ofFlow.setName(String.format(TO_INTERNET_FEATURE_NAME, ICMP,"All"));
        ofFlow.setPriority(DEFAULT_INTERNET_COMMUNICATION);
        ofFlow.setOfAction(OFFlow.OFAction.NORMAL);
        deviceMUDFlowMap.getFromInternetStaticFlows().add(ofFlow);

    }

    private OFFlow getMatchingFlow(OFFlow currentFlow, Map<Integer, OFFlow> ofFlows) {
        for (int key : ofFlows.keySet()) {
            OFFlow flow = ofFlows.get(key);
            String srcMac = currentFlow.getSrcMac();
            String dstMac = currentFlow.getDstMac();
            String ethType = currentFlow.getEthType();
            String vlanId = "*";
            String srcIp = currentFlow.getSrcIp() == null ? "*" : currentFlow.getSrcIp();
            String dstIp = currentFlow.getDstIp() == null ? "*" : currentFlow.getDstIp();
            String ipProto = currentFlow.getIpProto() == null ? "*" : currentFlow.getIpProto();
            String srcPort = currentFlow.getSrcPort() == null ? "*" : currentFlow.getSrcPort();
            String dstPort = currentFlow.getDstPort() == null ? "*" : currentFlow.getDstPort();

            //TODO temporary for testing purposes
            boolean ipMatching ;
            if (flow.getSrcIp().contains("/")) {
                String ip = flow.getSrcIp().split("/")[0];
                if (flow.getSrcIp().equals(PacketConstants.LINK_LOCAL_MULTICAST_IP_RANGE)) {
                    ip = "ff";
                }
                ipMatching = srcIp.startsWith(ip) || flow.getSrcIp().equals("*");
            } else {
                ipMatching = (srcIp.equals(flow.getSrcIp())  || flow.getSrcIp().equals("*"));
            }

            if (flow.getDstIp().contains("/")) {
                String ip = flow.getDstIp().split("/")[0];
                if (flow.getDstIp().equals(PacketConstants.LINK_LOCAL_MULTICAST_IP_RANGE)) {
                    ip = "ff";
                }
                ipMatching = ipMatching && dstIp.startsWith(flow.getDstIp().split("/")[0]) || flow.getDstIp().equals("*");
            } else {
                ipMatching = ipMatching && (dstIp.equals(flow.getDstIp())  || flow.getDstIp().equals("*"));
            }

            boolean condition = (srcMac.equals(flow.getSrcMac()) || flow.getSrcMac().equals("*"))&&
                    (dstMac.equals(flow.getDstMac())  || flow.getDstMac().equals("*"))&&
                    (ethType.equals(flow.getEthType()) || flow.getEthType().equals("*")) &&
                    (vlanId.equals(flow.getVlanId())  || flow.getVlanId().equals("*"))&&
                    ipMatching &&
                    (ipProto.equals(flow.getIpProto())  || flow.getIpProto().equals("*"))&&
                    (srcPort.equals(flow.getSrcPort())  || flow.getSrcPort().equals("*"))&&
                    (dstPort.equals(flow.getDstPort()) || flow.getDstPort().equals("*"));

            if (condition) {
                return flow;
            }
        }
        return null;
    }

    private void logData(String path, String content, boolean firstEntry) {
        File file = new File(path);
        // need to check how often does this method is called per device.
        if (!file.exists()) {
            logData(path, content);
        }
    }

    private void logData(String path, String content) {
        File file = new File(path);
        // need to check how often does this method is called per device.
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, true);
            writer.write(content + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.error("Failed to log content [" + content  + "] in "+ path, e);
        }
    }
}
