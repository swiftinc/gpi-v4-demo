package com.swift.developers.sandbox.session.impl;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.OkHttpClient;
import com.swift.developers.sandbox.exception.ApiSessionException;
import com.swift.developers.sandbox.util.ConnectionInfo;
import com.swift.developers.sandbox.util.ProxyAuthenticatorUtil;
import com.swift.developers.sandbox.util.ProxyParameters;
import com.swift.developers.sandbox.util.Util;
import com.swift.commons.api.APISecurityHSM;
import com.swift.commons.context.HSMContext;
import com.swift.commons.exceptions.NRSignatureException;
import com.swift.commons.exceptions.SignatureContextException;
import com.swift.commons.exceptions.SignatureGenerationException;
import com.swift.commons.oauth.api.OAuthCredentials;
import com.swift.commons.oauth.api.OAuthSessionImpl;
import com.swift.commons.oauth.connection.PhysicalCertificateHolder;
import com.swift.commons.oauth.connection.SNLInfoHolder;
import com.swift.commons.oauth.connection.SignHolder;
import com.swift.commons.oauth.exceptions.OAuthConnectionException;
import com.swift.commons.oauth.exceptions.OAuthFailResponseException;
import com.swift.commons.oauth.exceptions.OAuthSessionException;
import com.swift.commons.oauth.exceptions.OAuthValidationException;
import com.swift.commons.oauth.token.OAuthTokenHolder;
import com.swift.commons.oauth.token.OAuthTokenJWTBearer;
import com.swift.commons.oauth.token.OAuthTokenUnsecuredJWTBearer;
import com.swift.commons.utils.KeyStoreUtils;

import com.swift.developers.sandbox.util.SNLConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SessionImpl {

    private final static Logger LOG = LoggerFactory.getLogger(SessionImpl.class);
    private ProxyParameters[] proxyParameters = null;
    private OAuthCredentials oAuthCredentials = null;
    private OAuthSessionImpl oauthToken = null;
    private ConnectionInfo connInfo = null;

    public SessionImpl(ConnectionInfo info, ProxyParameters[] proxy) throws OAuthValidationException,
            ApiSessionException, SignatureContextException
    {
        connInfo = info;
        proxyParameters = proxy;
        oAuthCredentials = new OAuthCredentials(connInfo.getCertPath(), connInfo.getCertPassword(),
                connInfo.getCertPassword(), connInfo.getCertAlias(), connInfo.getTrustAliasGateway(), connInfo.getGatewayHost(),
                connInfo.getClientID(), connInfo.getClientSecret());
        oauthToken = new OAuthSessionImpl();

        /* Validate the proxy syntax. */
        Util.validateProxy(proxyParameters);
    }

    public OAuthTokenHolder getAccessTokenHolder()
            throws OAuthValidationException, OAuthConnectionException, OAuthFailResponseException, OAuthSessionException,
            ApiSessionException, SignatureContextException, SignatureGenerationException {

        if (connInfo.getCertSelector().equalsIgnoreCase("hsm")) {
            return getAccessTokenHolderHSM();
        }
        else {
            return getAccessTokenHolderChannelCert();
        }
    }

    private OAuthTokenHolder getAccessTokenHolderHSM()
            throws OAuthValidationException, OAuthConnectionException, OAuthFailResponseException, OAuthSessionException,
            ApiSessionException, SignatureContextException, SignatureGenerationException {

        /* Update fail over logic for SAG slots if needed. */
        LOG.info("Trying to connect to the SAG at {} slot.", connInfo.getHsmSlot());
        SNLConnectionInfo snlConInfo = connInfo.getSnlConnectionInfo().get(connInfo.getHsmSlot());
        PhysicalCertificateHolder sslHolder = new PhysicalCertificateHolder(snlConInfo.getTrustStoragePath(),
                snlConInfo.getTrustStoragePass(), snlConInfo.getTrustStorageAlias());

        SignHolder signHolder = new SNLInfoHolder(snlConInfo.getHostname(), new Integer(snlConInfo.getPort()),
                snlConInfo.getSslDN(), sslHolder, snlConInfo.getUserDN(), snlConInfo.getMessagePartner(),
                snlConInfo.getLauKey());
        try {
            oauthToken.init(oAuthCredentials, signHolder, OAuthTokenUnsecuredJWTBearer.class);
        } catch (SignatureContextException ex) {
            LOG.error("Failed to initialize oauth context  using {} SAG. "
                    + "Try to change SAG connection slot and rerun.", connInfo.getHsmSlot());
            throw ex;
        }

        /* Api Client can be prepared only after the OAUTH Session is initialized. */
        prepareApiClientOAuth(oauthToken.getTokenApiClient());

        OAuthTokenHolder oauthTokenInfo = null;
        try {
            oauthTokenInfo = oauthToken.getToken(connInfo.getScope(), connInfo.getAudience());
        } catch (SignatureGenerationException | SignatureContextException ex) {
            LOG.error("Failed to get oauth token using SAG at {} slot. Try to change SAG connection slot and re-run.", connInfo.getHsmSlot());
            throw ex;
        }
        LOG.debug("Received OAuth token: {}", oauthTokenInfo);

        // oauthToken.close();

        return oauthTokenInfo;
    }

    private OAuthTokenHolder getAccessTokenHolderChannelCert()
            throws SignatureContextException, OAuthValidationException, SignatureGenerationException,
            OAuthConnectionException, OAuthFailResponseException, OAuthSessionException, ApiSessionException {

        LOG.info("Start getting OAuth token signed by channel certificate.");
        SignHolder signHolder = new PhysicalCertificateHolder(connInfo.getCertPath(),
                connInfo.getCertPassword(), connInfo.getCertAlias());
        oauthToken.init(oAuthCredentials, signHolder, OAuthTokenJWTBearer.class);

        /* Api Client can be prepared only after the OAUTH Session is initialized. */
        prepareApiClientOAuth(oauthToken.getTokenApiClient());

        OAuthTokenHolder oauthTokenInfo = oauthToken.getToken(connInfo.getScope(), connInfo.getAudience());
        LOG.debug("Received OAuth token: {}", oauthTokenInfo);

        return oauthTokenInfo;
    }

    public OAuthTokenHolder refreshToken(String refreshToken)
            throws SignatureContextException, OAuthValidationException, OAuthFailResponseException,
            OAuthConnectionException, OAuthSessionException, ApiSessionException {

        LOG.info("Start refreshing OAuth token.");
        SignHolder signHolder = new PhysicalCertificateHolder(connInfo.getCertPath(),
                connInfo.getCertPassword(),	connInfo.getCertAlias());
        oauthToken.init(oAuthCredentials, signHolder, OAuthTokenJWTBearer.class);

        /* Api Client can be prepared only after the OAUTH Session is initialized. */
        // prepareApiClientOAuth(oauthToken.getTokenApiClient());

        OAuthTokenHolder oauthTokenInfo = oauthToken.refreshToken(connInfo.getScope(), connInfo.getAudience(),
                refreshToken);
        LOG.debug("Received OAuth refreshed token: {}", oauthTokenInfo);

        return oauthTokenInfo;
    }

    public String getSignatureNR(Map<String, Object> claimsMap) throws SignatureContextException,
            NRSignatureException {

        /* Update fail over logic for NR SAG slots here if needed. */
        LOG.info("Try to connect to the SAG at {} slot.", connInfo.getHsmSlot());
        SNLConnectionInfo snlHolder = connInfo.getSnlConnectionInfo().get(connInfo.getHsmSlot());
        HSMContext context = new HSMContext(snlHolder.getUserDN(), snlHolder.getTrustStoragePath(),
                snlHolder.getTrustStoragePass(), snlHolder.getTrustStorageAlias(), snlHolder.getSslDN(),
                snlHolder.getHostname(), new Integer(snlHolder.getPort()), snlHolder.getMessagePartner(),
                snlHolder.getLauKey());
        APISecurityHSM apiSecurityHSM = new APISecurityHSM(context);
        try {
            LOG.trace("Establish SNL session for NR signature.");
            apiSecurityHSM.initSign();
            LOG.trace("Sign NR.");
            String signatureNR = apiSecurityHSM.signNR(claimsMap);
            LOG.trace("Close SNL session for NR signature.");
            apiSecurityHSM.closeSign();
            return signatureNR;
        } catch (SignatureContextException | NRSignatureException ex) {
            LOG.error("Failed to sign NR request using SAG at {} slot. "
                    + "Try to change SAG connection slot and re-run.", connInfo.getHsmSlot());
            throw ex;
        }
    }

    public boolean revokeAccessToken(ConnectionInfo connInfo, OAuthTokenHolder oauthTokenInfo, ProxyParameters[] proxy)
            throws SignatureContextException, OAuthValidationException, OAuthFailResponseException,
            OAuthConnectionException, ApiSessionException {

        LOG.info("Start revoking OAuth token.");
        boolean flag = false;
        SignHolder signHolder = new PhysicalCertificateHolder(connInfo.getCertPath(), connInfo.getCertPassword(), connInfo.getCertAlias());
        oauthToken.init(oAuthCredentials, signHolder, OAuthTokenJWTBearer.class);

        com.swift.commons.oauth.oas.ApiClient client = oauthToken.revokeTokenApiClient();
        prepareApiClientOAuth(client);
        String revokeResp = oauthToken.revokeToken(oauthTokenInfo);
        if ((revokeResp != null) && !revokeResp.isEmpty()) {
            flag = true;
            LOG.debug("Received OAuth revoke response: {}", revokeResp);
        }

        return flag;
    }

    public boolean revokeAccessToken(ConnectionInfo connInfo, String accessToken, ProxyParameters[] proxy)
            throws SignatureContextException, OAuthValidationException, OAuthFailResponseException,
            OAuthConnectionException, ApiSessionException {

        LOG.info("Start revoking OAuth token.");

        boolean flag = false;

        SignHolder signHolder = new PhysicalCertificateHolder(connInfo.getCertPath(), connInfo.getCertPassword(), connInfo.getCertAlias());
        oauthToken.init(oAuthCredentials, signHolder, OAuthTokenJWTBearer.class);

        com.swift.commons.oauth.oas.ApiClient client = oauthToken.revokeTokenApiClient();
        prepareApiClientOAuth(client);

        String revokeResp = oauthToken.revokeToken(accessToken, OAuthTokenHolder.ACCESS_TOKEN_TYPE);
        if ((revokeResp != null) && !revokeResp.isEmpty()) {
            flag = true;
            LOG.debug("Received OAuth revoke response: {}", revokeResp);
        }

        return flag;
    }

    private void prepareApiClientOAuth(com.swift.commons.oauth.oas.ApiClient client)
            throws SignatureContextException, ApiSessionException {

        LOG.info("Preparing API Client for OAuth");
        KeyStore keystore = KeyStoreUtils.loadKeyStore(connInfo.getCertPath(), connInfo.getCertPassword());
        Certificate trustCertificate = Util.extractCertificate(connInfo.getTrustAliasGateway(), keystore);
        String trustPEMCertificate = Util.extractPemCertificate(trustCertificate);
        client.setSslCaCert(new ByteArrayInputStream(trustPEMCertificate.getBytes(StandardCharsets.UTF_8)));

        client.setBasePath(connInfo.getGatewayHost());
        client.setUsername(connInfo.getClientID());
        client.setPassword(connInfo.getClientSecret());

        OkHttpClient httpClient = client.getHttpClient();

        if ((proxyParameters != null) && (proxyParameters.length != 0)) {
            httpClient.setProxySelector(new HttpProxySelector());
            ProxyAuthenticatorUtil proxyAuthenticatorUtil = new ProxyAuthenticatorUtil();
            Authenticator auth = proxyAuthenticatorUtil.prepareProxyAuthenticator(proxyParameters);
            httpClient.setAuthenticator(auth);
        }
    }

    public void close() {
        oauthToken.close();
    }

    class HttpProxySelector extends ProxySelector {
        int proxyConnectionTries = 0;

        @Override
        public List<Proxy> select(URI uri) {
            proxyConnectionTries = 0;
            List<Proxy> proxies = new ArrayList<>();
            for (ProxyParameters proxyParameter : proxyParameters) {
                String proxyHost = proxyParameter.getHost();
                String proxyPort = proxyParameter.getPort();

                if (!Util.isNullOrEmpty(proxyHost)) {
                    InetSocketAddress proxyInet = new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort));
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
                    proxies.add(proxy);
                }
            }
            return proxies;
        }

        @Override
        public void connectFailed(URI uri, SocketAddress socketAddress, IOException e) {
            proxyConnectionTries++;
            LOG.error("Proxy connection to {}:{} failed: {}", uri.getHost(), uri.getPort(), e.getMessage());
            if (proxyConnectionTries == proxyParameters.length) {
                throw new OAuthConnectionException(e);
            }
        }
    }


}
