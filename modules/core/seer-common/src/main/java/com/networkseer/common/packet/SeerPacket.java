package com.networkseer.common.packet;

public class SeerPacket {

    private String vxlanId;
    private String srcMac;
    private String dstMac;
    private String ethType;
    private String srcIp;
    private String dstIp;
    private String ipProto;
    private String srcPort;
    private String dstPort;
    private byte[] payload;

    public void setTcpFlag(Flag tcpFlag) {
        this.tcpFlag = tcpFlag;
    }

    private Flag tcpFlag;

    public enum Flag {
        SYN,
        SYN_ACK,
        OTHER
    }

    public Flag getTcpFlag() {
        return tcpFlag;
    }

    public void setTcpFlag(boolean syn, boolean ack) {
        tcpFlag = Flag.OTHER;
        if (syn) {
            tcpFlag = Flag.SYN;
            if (ack) {
                tcpFlag = Flag.SYN_ACK;
            }
        }
    }

    public String getVxlanId() {
        return vxlanId;
    }

    public void setVxlanId(String vxlanId) {
        this.vxlanId = vxlanId;
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

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
