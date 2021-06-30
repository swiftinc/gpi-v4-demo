package com.swift.developers.sandbox.util;

import java.util.Objects;

public class ProxyParameters {

    private String host;
    private String port;
    private String user;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;

    public ProxyParameters(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public ProxyParameters(String host, String port, String user, String password) {
        this(host, port);
        this.user = user;
        this.password = password;
    }
    
    public ProxyParameters() {
    	;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        ProxyParameters that = (ProxyParameters) o;
        return host.equals(that.host) &&
                port.equals(that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "ProxyParameters{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}
