package com.swift.developers.sandbox.util;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.net.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyAuthenticatorUtil {

	private final static Logger LOG = LoggerFactory.getLogger(ProxyAuthenticatorUtil.class);
	
    public Authenticator prepareProxyAuthenticator(final ProxyParameters[] proxyParameters) {
        if ((proxyParameters != null) && (proxyParameters.length != 0)) {
            Authenticator auth = new Authenticator() {

                @Override
                public Request authenticateProxy(Proxy proxy, Response response) {
                    if (response.request().header("Proxy-Authorization") != null) {
                        return null;
                    }
                    LOG.info("Proxy authentication");
                    Request.Builder builder = response.request().newBuilder();
                    for (ProxyParameters parameter : proxyParameters) {
                        if (!Util.isNullOrEmpty(parameter.getUser()) && !Util.isNullOrEmpty(parameter.getPassword())) {
                            String[] proxyAddress = proxy.address().toString().split(":");
                            if ((proxyAddress != null) && proxyAddress[0].substring(1).equals(parameter.getHost())
                                    && proxyAddress[1].equals(parameter.getPort())) {
                            	LOG.info("Proxy Authentication");
                                String credential = Credentials.basic(parameter.getUser(), parameter.getPassword());
                                builder.addHeader("Proxy-Authorization", credential);                                
                            }
                        }
                    }
                    return builder.build();
                }

                @Override
                public Request authenticate(Proxy proxy, Response response) {
                    return authenticateProxy(proxy, response);
                }
            };

            return auth;
        }
        return null;
    }
}
