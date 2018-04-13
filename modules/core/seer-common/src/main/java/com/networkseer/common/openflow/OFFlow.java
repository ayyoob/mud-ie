package com.networkseer.common.openflow;

import java.io.Serializable;

public class OFFlow implements Serializable{
    private String name;
    private String srcMac="*";
    private String dstMac="*";
    private String ethType="*";
    private String vlanId="*";
    private String srcIp="*";
    private String dstIp="*";
    private String ipProto="*";
    private String srcPort="*";
    private String dstPort="*";
    private String icmpType="*";
    private String icmpCode="*";
    private OFAction ofAction;
    private int hardTimeout=0;
    private long idleTimeOutInSeconds = 0;
    private int priority;
    private long byteCount = 0;
    private long packetCount = 0;

    public OFFlow copy() {
        OFFlow ofFlow = new OFFlow();
        ofFlow.setSrcMac(this.srcMac);
        ofFlow.setDstMac(this.dstMac);
        ofFlow.setEthType(this.ethType);
        ofFlow.setVlanId(this.vlanId);
        ofFlow.setSrcIp(this.srcIp);
        ofFlow.setDstIp(this.dstIp);
        ofFlow.setIpProto(this.ipProto);
        ofFlow.setSrcPort(this.srcPort);
        ofFlow.setDstPort(this.dstPort);
        ofFlow.setPriority(this.priority);
        ofFlow.setIcmpType(this.icmpType);
        ofFlow.setIcmpCode(this.icmpCode);
        return ofFlow;
    }

    public String getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(String icmpType) {
        this.icmpType = icmpType;
    }

    public String getIcmpCode() {
        return icmpCode;
    }

    public void setIcmpCode(String icmpCode) {
        this.icmpCode = icmpCode;
    }

    public long getIdleTimeOutInSeconds() {
        return idleTimeOutInSeconds;
    }

    public void setIdleTimeOutInSeconds(long idleTimeOutInSeconds) {
        this.idleTimeOutInSeconds = idleTimeOutInSeconds;
    }

    public long getPacketCount() {
        return packetCount;
    }

    public void setPacketCount(long packetCount) {
        this.packetCount = packetCount;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public enum OFAction {
        MIRROR_TO_VXLAN,
        NORMAL,
        DROP
    }

    public OFAction getOfAction() {
        return ofAction;
    }

    public void setOfAction(OFAction ofAction) {
        this.ofAction = ofAction;
    }

    public String getSrcMac() {
        return srcMac;
    }

    public void setSrcMac(String srcMac) {
        this.srcMac = srcMac;
    }

    public String getDstMac() {
        return dstMac;
    }

    public void setDstMac(String dstMac) {
        this.dstMac = dstMac;
    }

    public String getEthType() {
        return ethType;
    }

    public void setEthType(String ethType) {
        this.ethType = ethType;
    }

    public String getVlanId() {
        return vlanId;
    }

    public void setVlanId(String vlanId) {
        this.vlanId = vlanId;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public String getIpProto() {
        return ipProto;
    }

    public void setIpProto(String ipProto) {
        this.ipProto = ipProto;
    }

    public String getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(String srcPort) {
        this.srcPort = srcPort;
    }

    public String getDstPort() {
        return dstPort;
    }

    public void setDstPort(String dstPort) {
        this.dstPort = dstPort;
    }

    @Override
    public int hashCode() {
        int result = this.srcMac.hashCode();
        result = 31 * result + ("@" + this.dstMac + "@" + this.ethType + "@" + this.vlanId
                + "@" + this.srcIp + "@" + this.dstIp + "@" + this.ipProto + "@" + this.srcPort + "@" + this.dstPort
                + "@" + this.icmpType+ "@" + this.icmpCode).hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        OFFlow flow = (OFFlow) obj;
        return  this.srcMac.equals(flow.getSrcMac()) &&
                this.dstMac.equals(flow.getDstMac()) &&
                this.ethType.equals(flow.getEthType()) &&
                this.vlanId.equals(flow.getVlanId()) &&
                this.srcIp.equals(flow.getSrcIp()) &&
                this.dstIp.equals(flow.getDstIp()) &&
                this.ipProto.equals(flow.getIpProto()) &&
                this.srcPort.equals(flow.getSrcPort()) &&
                this.icmpType.equals(flow.getIcmpType()) &&
                this.icmpCode.equals(flow.getIcmpCode()) &&
                this.dstPort.equals(flow.getDstPort());

    }

    public long getByteCount() {
        return byteCount;
    }

    public void setByteCount(long byteCount) {
        this.byteCount = byteCount;
    }

    public String getFlowHeaderString() {
        return "srcMac,dstMac,ethType,vlanId,srcIp,dstIp,ipProto,srcPort,dstPort,priority,byteCount,packetCount, " +
                "icmpType, icmpCode";
    }

    public String getFlowString() {
        return srcMac + "," + dstMac + "," + ethType + "," + vlanId + "," + srcIp + "," + dstIp + ","
                + ipProto + "," + srcPort + "," + dstPort + "," + priority + "," + byteCount + "," + packetCount + "," + icmpType + "," + icmpCode
                ;
    }

    public String getFlowHeaderWithoutFlowStat() {
        return "srcMac,dstMac,ethType,vlanId,srcIp,dstIp,ipProto,srcPort,dstPort,priority,icmpType,icmpCode";
    }

    public String getFlowStringWithoutFlowStat() {
        return srcMac + "," + dstMac + "," + ethType + "," + vlanId + "," + srcIp + "," + dstIp + ","
                + ipProto + "," + srcPort + "," + dstPort + "," + priority + "," + icmpType + "," + icmpCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHardTimeout() {
        return hardTimeout;
    }

    public void setHardTimeout(int hardTimeout) {
        this.hardTimeout = hardTimeout;
    }
}
