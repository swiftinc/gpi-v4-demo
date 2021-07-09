package com.swift.developers.sandbox.session.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.ConnectionSpec;
import com.swift.developers.sandbox.exception.ApiSessionException;
import com.swift.developers.sandbox.util.ConnectionInfo;
import com.swift.developers.sandbox.util.Constants;
import com.swift.developers.sandbox.util.ProxyAuthenticatorUtil;
import com.swift.developers.sandbox.util.ProxyParameters;
import com.swift.developers.sandbox.util.Util;
import com.swift.commons.exceptions.SignatureContextException;
import com.swift.commons.oauth.exceptions.OAuthConnectionException;
import com.swift.commons.oauth.token.OAuthTokenHolder;
import com.swift.commons.utils.KeyStoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SandboxApiSession {

	private String configFile = null;
	private JsonObject configJson = null;
	private ConnectionInfo connInfo = null;
	private SessionImpl sessionImpl = null;
	private OAuthTokenHolder oauthTokenHolder = null;
	private ProxyParameters[] proxyParameters = null;
	private final static Logger LOG = LoggerFactory.getLogger(SandboxApiSession.class);
	
	public SandboxApiSession(String configFile, Util.CertType type) throws ApiSessionException {
		
		this.setUp(configFile, type, null, null);		
	}
	
	/**
	 * 
	 * @param configFile
	 * @param service		- Override Provider Service in the configuration file.
	 * @param scope			- Override Scope in the configuration file.
	 * @throws ApiSessionException
	 */
	public SandboxApiSession(String configFile, Util.CertType type, String service, String scope) throws ApiSessionException {
        
        this.setUp(configFile, type, service, scope);			
	}
	
    public Object prepareApiClient(Object client, String basepath) throws SignatureContextException, ApiSessionException {
		
		try {
			LOG.info("Preparing API Client with Access Token and SSL Cert");
			/* Setting access token. */
			Method tmpmethod = client.getClass().getMethod("setAccessToken", String.class);			
			tmpmethod.invoke(client, this.getAccessToken());
			
			/* Setting base path. */
			tmpmethod = client.getClass().getMethod("setBasePath", String.class);			
			// tmpmethod.invoke(client, connInfo.getGatewayHost() + connInfo.getProviderService());
			tmpmethod.invoke(client, basepath);
					
			/* Setting SSL. */
			KeyStore keystore = KeyStoreUtils.loadKeyStore(connInfo.getCertPath(), connInfo.getCertPassword());
			Certificate trustCertificate = Util.extractCertificate(connInfo.getTrustAliasGateway(), keystore);
			String trustPEMCertificate = Util.extractPemCertificate(trustCertificate);
			if (trustPEMCertificate == null) {
				throw new SignatureContextException(
						"Trust storage doesn't contain certificate with alias " + connInfo.getTrustAliasGateway());
			}
			tmpmethod = client.getClass().getMethod("setSslCaCert", InputStream.class);
			tmpmethod.invoke(client, new ByteArrayInputStream(trustPEMCertificate.getBytes(StandardCharsets.UTF_8)));
			
			/* Setting HTTP Client. */
			tmpmethod = client.getClass().getMethod("getHttpClient");			
			OkHttpClient httpClient = (OkHttpClient) tmpmethod.invoke(client);
			httpClient.setRetryOnConnectionFailure(true);
			httpClient.setConnectionSpecs(
					Collections.singletonList(
							new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).allEnabledCipherSuites().build()));
			
			if ((proxyParameters != null) && (proxyParameters.length != 0)) {
				httpClient.setProxySelector(new HttpProxySelector());
				ProxyAuthenticatorUtil proxyAuthenticatorUtil = new ProxyAuthenticatorUtil();
				Authenticator auth = proxyAuthenticatorUtil.prepareProxyAuthenticator(proxyParameters);
				httpClient.setAuthenticator(auth);
			}
			
			/* Setting GSON no-escape. */
			tmpmethod = client.getClass().getMethod("getJSON");			
			Object jsonObj = tmpmethod.invoke(client);
			Gson gsonObj = getJsonNotHtmlEscaped(jsonObj);
			
			tmpmethod = jsonObj.getClass().getMethod("setGson", Gson.class);
			tmpmethod.invoke(jsonObj, gsonObj);
			
			return client;
		} catch (Exception ex) {
			LOG.error("Failed to prepare the client. Error : " + ex.getMessage() +
        			"\nStack Trace : \n" + Util.getStackTrace(ex));
			throw new ApiSessionException(ex.getMessage(), ex);
		}
	}
    
    public synchronized String refreshToken() throws ApiSessionException {
		try {
			this.setOauthTokenHolder(sessionImpl.refreshToken(this.getRefreshToken()));
			
			return this.getAccessToken();
		}
		catch (Exception ex) {
			ApiSessionException locex = (ex instanceof ApiSessionException) ? (ApiSessionException) ex :
        		new ApiSessionException(ex.getMessage(), ex);
        	throw locex;
		}
	}
    
    public String getSignatureNR(Map<String, Object> claimsMap) throws ApiSessionException {
		try {
			return sessionImpl.getSignatureNR(claimsMap);			
		}
		catch (Exception ex) {
			ApiSessionException locex = (ex instanceof ApiSessionException) ? (ApiSessionException) ex :
        		new ApiSessionException(ex.getMessage(), ex);
        	throw locex;
		}
	}
    
    /*
     * Timeout in milliseconds.
     */
    public void setHttpTimeout(Object client, int timeOut) throws ApiSessionException {
    	
		try {
			Method tmpmethod = client.getClass().getMethod("setConnectTimeout", int.class);
			tmpmethod.invoke(client, timeOut);
		} catch (Exception ex) {
			throw new ApiSessionException(ex.getMessage(), ex);
		}		
    }
    
    private Gson getJsonNotHtmlEscaped(Object gsonHtmlEscaped) throws ApiSessionException {
		
		Method tmpmethod;
		try {
			tmpmethod = gsonHtmlEscaped.getClass().getMethod("createGson");
			// GsonBuilder bldrObj = (GsonBuilder) tmpmethod.invoke(gsonHtmlEscaped);
			/* Static function call. */
			GsonBuilder bldrObj = (GsonBuilder) tmpmethod.invoke(null);
			
			tmpmethod = gsonHtmlEscaped.getClass().getMethod("getGson");
			Gson gsonObj = (Gson) tmpmethod.invoke(gsonHtmlEscaped);
			
			Gson gson = bldrObj.disableHtmlEscaping()
					.registerTypeAdapter(Date.class, gsonObj.getAdapter(Date.class))
					.registerTypeAdapter(java.sql.Date.class, gsonObj.getAdapter(java.sql.Date.class))
					.registerTypeAdapter(OffsetDateTime.class, gsonObj.getAdapter(OffsetDateTime.class))
					.registerTypeAdapter(LocalDate.class, gsonObj.getAdapter(LocalDate.class)).create();
			
			return gson;
		} catch (Exception ex) {
			throw new ApiSessionException(ex.getMessage(), ex);
		}					
	}
	
	private void setUp(String configFile, Util.CertType type, String service, String scope) throws ApiSessionException {
		try {
			this.setConfigFile(configFile);
			
			LOG.info("\n#################Initializing Basic Session Object######################\n");
			LOG.info("Initializing using the configration file {}.", configFile);
			/* Supports both YAML & JSON configurations. */
			if (configFile.endsWith(".yaml")) {
				configJson = Util.readConfigurationPropertiesYaml(configFile);
			} else if (configFile.endsWith(".json")) {
				configJson = Util.readConfigurationPropertiesJson(configFile);
			}
			connInfo = Util.createConnectionInfo(configJson);
	        
	        /* Override Provider Service & Scope. */
			/*
	        if (service != null) {
	        	connInfo.setProviderService(service);
	        }
	        */
	        if (scope != null) {
	        	connInfo.setScope(scope);
	        }
	        
	        if (connInfo.getProxyList() != null) {
	        	/* Forward proxies are defined. */
	        	proxyParameters = new ProxyParameters[connInfo.getProxyList().size()];
		        proxyParameters = connInfo.getProxyList().toArray(proxyParameters);
	        }
	        
	        sessionImpl = new SessionImpl(connInfo, proxyParameters, type);
	        this.setOauthTokenHolder(sessionImpl.getAccessTokenHolder());
		}
		catch (Exception ex)
        {
        	LOG.error("Failed to initialize. Error : " + ex.getMessage() +
        			"\nStack Trace : \n" + Util.getStackTrace(ex));
        	ApiSessionException locex = (ex instanceof ApiSessionException) ? (ApiSessionException) ex :
        		new ApiSessionException(ex.getMessage(), ex);
        	throw locex;
        }
	}
	
	public String getBasePath(String host, String service) {
		return Util.getBasePath(configJson, host, service);
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
					LOG.info("Proxy {}:{} added.", proxyHost, proxyPort);
				}
			}
			return proxies;
		}

		@Override
		public void connectFailed(URI uri, SocketAddress socketAddress, IOException e) {
			proxyConnectionTries++;
			if (proxyConnectionTries == proxyParameters.length) {
				throw new OAuthConnectionException(e);
			}
		}
	}
	
	/**
	 * Returns the access token.
	 * @return
	 */
	public String getAccessToken() {
		return oauthTokenHolder.getAccessToken();
	}
		
	/**
	 * Returns the refresh token.
	 * @return
	 */
	public String getRefreshToken() {
		return oauthTokenHolder.getRefreshToken();
	}
	
	public String getTokenExpiry( ) {
		return oauthTokenHolder.getTokenExpiry();
	}
	
	public String getRefreshExpiry( ) {
		return oauthTokenHolder.getRefreshExpiry();
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public JsonObject getConfigJson() {
		return configJson;
	}

	public void setConfigJson(JsonObject configJson) {
		this.configJson = configJson;
	}

	public ConnectionInfo getConnInfo() {
		return connInfo;
	}

	public void setConnInfo(ConnectionInfo connInfo) {
		this.connInfo = connInfo;
	}

	public OAuthTokenHolder getOauthTokenHolder() {
		return oauthTokenHolder;
	}

	public void setOauthTokenHolder(OAuthTokenHolder oauthTokenHolder) {
		this.oauthTokenHolder = oauthTokenHolder;
	}
}
