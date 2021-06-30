package com.swift.developers.sandbox.util;

public class MGWConnectionInfo {

	private String mgwApplicationName;

	private String mgwProfileId;

	private String mgwSharedSecret;

	public String getMgwProfileId() {
		return mgwProfileId;
	}

	public void setMgwProfileId(String mgwProfileId) {
		this.mgwProfileId = mgwProfileId;
	}

	public String getMgwSharedSecret() {
		return mgwSharedSecret;
	}

	public void setMgwSharedSecret(String mgwSharedSecret) {
		this.mgwSharedSecret = mgwSharedSecret;
	}

	public String getMgwApplicationName() {
		return mgwApplicationName;
	}

	public void setMgwApplicationName(String mgwApplicationName) {
		this.mgwApplicationName = mgwApplicationName;
	}
}
