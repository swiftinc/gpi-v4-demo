package com.swift.developers.sandbox.util;

public class SNLConnectionInfo {

    private String hostname;
    private String port;
    private String sslDN;
    private String messagePartner;
    private String userDN;
    private String lauKey;
    private String trustStoragePath;
    private String trustStoragePass;
    private String trustStorageAlias;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSslDN() {
        return sslDN;
    }

    public void setSslDN(String sslDN) {
        this.sslDN = sslDN;
    }

    public String getMessagePartner() {
        return messagePartner;
    }

    public void setMessagePartner(String messagePartner) {
        this.messagePartner = messagePartner;
    }

    public String getUserDN() {
        return userDN;
    }

    public void setUserDN(String userDN) {
        this.userDN = userDN;
    }

    public String getLauKey() {
        return lauKey;
    }

    public void setLauKey(String lauKey) {
        this.lauKey = lauKey;
    }

    public String getTrustStoragePath() {
        return trustStoragePath;
    }

    public void setTrustStoragePath(String trustStoragePath) {
        this.trustStoragePath = trustStoragePath;
    }

    public String getTrustStoragePass() {
        return trustStoragePass;
    }

    public void setTrustStoragePass(String trustStoragePass) {
        this.trustStoragePass = trustStoragePass;
    }

    public String getTrustStorageAlias() {
        return trustStorageAlias;
    }

    public void setTrustStorageAlias(String trustStorageAlias) {
        this.trustStorageAlias = trustStorageAlias;
    }

}
