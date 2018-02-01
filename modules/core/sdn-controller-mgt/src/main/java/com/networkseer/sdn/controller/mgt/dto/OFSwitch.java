package com.networkseer.sdn.controller.mgt.dto;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OFSwitch {

    private String macAddress;
    private String ip;
    private String dpid;
    private long connectedTimestamp;
    private LinkedList<OFFlow> ofFlows = new LinkedList<OFFlow>();
    private List<String> hosts = new ArrayList<>();

    public long getConnectedTimestamp() {
        return connectedTimestamp;
    }

    public void setConnectedTimestamp(long connectedTimestamp) {
        this.connectedTimestamp = connectedTimestamp;
    }

    public LinkedList<OFFlow> getOfFlows() {
        return ofFlows;
    }

    public void setOfFlows(LinkedList<OFFlow> ofFlows) {
        this.ofFlows = ofFlows;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDpid() {
        return dpid;
    }

    public void setDpid(String dpid) {
        this.dpid = dpid;
    }

    public List<OFFlow> getAllFlows() {
        return ofFlows;
    }

}
