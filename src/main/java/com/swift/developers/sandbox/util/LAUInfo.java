package com.swift.developers.sandbox.util;

import java.time.Instant;
import java.util.UUID;

public class LAUInfo {

    private String lauKey;
    private String lauApplicationID;
    private String lauVersion;
    private String lauCallTime;
    private String lauRequestNonce;
    private String lauSigned;

    public String getLauKey() {
        return lauKey;
    }

    public void setLauKey(String lauKey) {
        this.lauKey = lauKey;
    }

    public String getLauApplicationID() {
        return lauApplicationID;
    }

    public void setLauApplicationID(String lauApplicationID) {
        this.lauApplicationID = lauApplicationID;
    }

    public String getLauVersion() {
        return lauVersion;
    }

    public void setLauVersion(String lauVersion) {
        this.lauVersion = lauVersion;
    }

    public String getLauCallTime() {
        return lauCallTime;
    }

    public void setLauCallTime(String lauCallTime) {
        this.lauCallTime = lauCallTime;
    }

    public String getLauRequestNonce() {
        return lauRequestNonce;
    }

    public void setLauRequestNonce(String lauRequestNonce) {
        this.lauRequestNonce = lauRequestNonce;
    }

    public String getLauSigned() {
        return lauSigned;
    }

    public void setLauSigned(String lauSigned) {
        this.lauSigned = lauSigned;
    }

    public LAUInfo() {
        this.lauCallTime = Instant.now().toString();
        this.lauRequestNonce = UUID.randomUUID().toString();
    }

    public LAUInfo(String lauKey, String lauApplicationID, String lauSigned) {
        this(lauKey, lauApplicationID, "1.0", lauSigned);
    }

    public LAUInfo(String lauKey, String lauApplicationID, String lauVersion, String lauSigned) {
        this();
        this.lauKey = lauKey;
        this.lauApplicationID = lauApplicationID;
        this.lauVersion = lauVersion;
        this.lauSigned = lauSigned;
    }
}
