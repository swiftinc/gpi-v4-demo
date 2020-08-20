package com.swift.developers.sandbox.util;

import com.swift.developers.sandbox.util.LAUInfo;
import com.swift.developers.sandbox.util.SNLConnectionInfo;

import java.util.List;

public class ConnectionInfo {

    private String certAlias;
    private String trustAliasGateway;
    private String trustAliasConnector;
    private String certPassword;
    private String clientID;
    private String clientSecret;
    private String certPath;
    private String gatewayHost;
    private String connectorHost;
    private String oauthService;
    private String scope;
    private String audience;
    private String caCert;
    private List<SNLConnectionInfo> snlConnectionInfo;
    private List<LAUInfo> lauInfo;
    private String username;
    private String password;
    private String providerService;
    private String certSelector;
    private int hsmSlot;

    public String getCertAlias() {
        return certAlias;
    }

    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    public String getTrustAliasGateway() {
        return trustAliasGateway;
    }

    public void setTrustAliasGateway(String trustAliasGateway) {
        this.trustAliasGateway = trustAliasGateway;
    }

    public String getTrustAliasConnector() {
        return trustAliasConnector;
    }

    public void setTrustAliasConnector(String trustAliasConnector) {
        this.trustAliasConnector = trustAliasConnector;
    }

    public String getCertPassword() {
        return certPassword;
    }

    public void setCertPassword(String certPassword) {
        this.certPassword = certPassword;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getGatewayHost() {
        return gatewayHost;
    }

    public void setGatewayHost(String gatewayHost) {
        this.gatewayHost = gatewayHost;
    }

    public String getConnectorHost() {
        return connectorHost;
    }

    public void setConnectorHost(String connectorHost) {
        this.connectorHost = connectorHost;
    }

    public String getOauthService() {
        return oauthService;
    }

    public void setOauthService(String service) {
        this.oauthService = service;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getCaCert() {
        return caCert;
    }

    public void setCaCert(String caCert) {
        this.caCert = caCert;
    }

    public List<SNLConnectionInfo> getSnlConnectionInfo() {
        return snlConnectionInfo;
    }

    public void setSnlConnectionInfo(List<SNLConnectionInfo> snlConnectionInfo) {
        this.snlConnectionInfo = snlConnectionInfo;
    }

    public List<LAUInfo> getLauInfo() {
        return lauInfo;
    }

    public void setLauInfo(List<LAUInfo> lauInfo) {
        this.lauInfo = lauInfo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProviderService() {
        return providerService;
    }

    public void setProviderService(String providerService) {
        this.providerService = providerService;
    }

    public String getCertSelector() {
        return certSelector;
    }

    public void setCertSelector(String certSelector) {
        this.certSelector = certSelector;
    }

    public int getHsmSlot() {
        return hsmSlot;
    }

    public void setHsmSlot(int hsmSlot) {
        this.hsmSlot = hsmSlot;
    }
}
