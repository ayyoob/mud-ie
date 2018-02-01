package com.networkseer.sdn.controller.mgt.impl.floodlight;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatFlowDTO {
	private String version;
	private String cookie;
	@JsonProperty("tableId")
	private String tableId;
	@JsonProperty("packet_count")
	private String packetCount;
	@JsonProperty("byte_count")
	private String byteCount;
	@JsonProperty("duration_sec")
	private String durationSec;
	private String priority;
	@JsonProperty("idle_timeout_s")
	private String idleTimeoutS;
	@JsonProperty("hard_timeout_s")
	private String hardTimeoutS;
	private Match match;
	private Actions actions;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getPacketCount() {
		return packetCount;
	}

	public void setPacketCount(String packetCount) {
		this.packetCount = packetCount;
	}

	public String getByteCount() {
		return byteCount;
	}

	public void setByteCount(String byteCount) {
		this.byteCount = byteCount;
	}

	public String getDurationSec() {
		return durationSec;
	}

	public void setDurationSec(String durationSec) {
		this.durationSec = durationSec;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getIdleTimeoutS() {
		return idleTimeoutS;
	}

	public void setIdleTimeoutS(String idleTimeoutS) {
		this.idleTimeoutS = idleTimeoutS;
	}

	public String getHardTimeoutS() {
		return hardTimeoutS;
	}

	public void setHardTimeoutS(String hardTimeoutS) {
		this.hardTimeoutS = hardTimeoutS;
	}

	public Match getMatch() {
		return match;
	}

	public void setMatch(Match match) {
		this.match = match;
	}

	public Actions getActions() {
		return actions;
	}

	public void setActions(Actions actions) {
		this.actions = actions;
	}
}
