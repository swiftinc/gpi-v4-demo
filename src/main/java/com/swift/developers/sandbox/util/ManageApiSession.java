package com.swift.developers.sandbox.util;

public class ManageApiSession {
	/*
	 * private JsonObject configJson = null; private ConnectionInfo connInfo = null;
	 * private SessionImpl sessionImpl = null; private OAuthTokenHolder
	 * oauthTokenHolder = null; private boolean isInitialized = false; private
	 * ProxyParameters[] proxyParameters = null; private final static Logger LOG =
	 * LoggerFactory.getLogger(ManageApiSession.class); private static
	 * ManageApiSession mngAccessToken = null;
	 * 
	 * public static synchronized ManageApiSession getInstance() throws
	 * ApiSessionException { if (mngAccessToken == null) { mngAccessToken = new
	 * ManageApiSession(); }
	 * 
	 * return mngAccessToken; }
	 * 
	 * public void init() throws ApiSessionException { if (isInitialized) {
	 * System.out.println("Initialization is already done.");
	 * LOG.info("Initialization is already done."); return; } initSession(null);
	 * isInitialized = true; }
	 * 
	 * public void init(ConnectionInfo info) throws ApiSessionException {
	 * initSession(info); }
	 * 
	 * private void initSession(ConnectionInfo info) throws ApiSessionException {
	 * try { if (info == null) { String path = System.getProperty("CFG_FILE"); if
	 * (path == null) { path = Constants.CONN_SESS_CONFIG_FILEPATH; }
	 * LOG.info("Initializing using the configration file {}.", path); configJson =
	 * Util.readConfigurationPropertiesJson(path); connInfo =
	 * Util.createConnectionInfo(configJson); } else { connInfo = info; }
	 * 
	 * sessionImpl = new SessionImpl(connInfo, proxyParameters);
	 * this.setOauthTokenHolder(sessionImpl.getAccessTokenHolder()); } catch
	 * (Exception ex) { LOG.error("Failed to initialize. Error : " + ex.getMessage()
	 * + "\nStack Trace : \n" + ManageApiSession.getStackTrace(ex));
	 * ApiSessionException locex = (ex instanceof ApiSessionException) ?
	 * (ApiSessionException) ex : new ApiSessionException(ex.getMessage(), ex);
	 * throw locex; } }
	 * 
	 * private ManageApiSession() { ; }
	 * 
	 * public String getAccessToken() { return oauthTokenHolder.getAccessToken(); }
	 * 
	 * public String getRefreshToken() { return oauthTokenHolder.getRefreshToken();
	 * }
	 * 
	 * public String getTokenExpiry( ) { return oauthTokenHolder.getTokenExpiry(); }
	 * 
	 * public String getRefreshExpiry( ) { return
	 * oauthTokenHolder.getRefreshExpiry(); }
	 * 
	 * public synchronized String refreshToken() throws ApiSessionException { try {
	 * this.setOauthTokenHolder(sessionImpl.refreshToken(this.getRefreshToken()));
	 * 
	 * return this.getAccessToken(); } catch (Exception ex) { ApiSessionException
	 * locex = (ex instanceof ApiSessionException) ? (ApiSessionException) ex : new
	 * ApiSessionException(ex.getMessage(), ex); throw locex; } }
	 * 
	 * public Object prepareApiClient(Object client) throws
	 * SignatureContextException, ApiSessionException {
	 * 
	 * try { Setting access token. Method tmpmethod =
	 * client.getClass().getMethod("setAccessToken", String.class);
	 * tmpmethod.invoke(client, this.getAccessToken());
	 * 
	 * Setting base path. tmpmethod = client.getClass().getMethod("setBasePath",
	 * String.class); tmpmethod.invoke(client, connInfo.getGatewayHost() +
	 * connInfo.getProviderService());
	 * 
	 * Setting SSL. KeyStore keystore =
	 * KeyStoreUtils.loadKeyStore(connInfo.getCertPath(),
	 * connInfo.getCertPassword()); Certificate trustCertificate =
	 * Util.extractCertificate(connInfo.getTrustAliasGateway(), keystore); String
	 * trustPEMCertificate = Util.extractPemCertificate(trustCertificate); if
	 * (trustPEMCertificate == null) { throw new SignatureContextException(
	 * "Trust storage doesn't contain certificate with alias " +
	 * connInfo.getTrustAliasGateway()); } tmpmethod =
	 * client.getClass().getMethod("setSslCaCert", InputStream.class);
	 * tmpmethod.invoke(client, new
	 * ByteArrayInputStream(trustPEMCertificate.getBytes(StandardCharsets.UTF_8)));
	 * 
	 * Setting HTTP Client. tmpmethod =
	 * client.getClass().getMethod("getHttpClient"); OkHttpClient httpClient =
	 * (OkHttpClient) tmpmethod.invoke(client);
	 * 
	 * if ((proxyParameters != null) && (proxyParameters.length != 0)) {
	 * httpClient.setProxySelector(new HttpProxySelector()); ProxyAuthenticatorUtil
	 * proxyAuthenticatorUtil = new ProxyAuthenticatorUtil(); Authenticator auth =
	 * proxyAuthenticatorUtil.prepareProxyAuthenticator(proxyParameters);
	 * httpClient.setAuthenticator(auth); }
	 * 
	 * Setting GSON no-escape. tmpmethod = client.getClass().getMethod("getJSON");
	 * Object jsonObj = tmpmethod.invoke(client); Gson gsonObj =
	 * getJsonNotHtmlEscaped(jsonObj);
	 * 
	 * tmpmethod = jsonObj.getClass().getMethod("setGson", Gson.class);
	 * tmpmethod.invoke(jsonObj, gsonObj);
	 * 
	 * return client; } catch (Exception ex) {
	 * LOG.error("Failed to prepare the client. Error : " + ex.getMessage() +
	 * "\nStack Trace : \n" + ManageApiSession.getStackTrace(ex));
	 * ex.printStackTrace(); throw new ApiSessionException(ex.getMessage(), ex); } }
	 * 
	 * public static Gson getJsonNotHtmlEscaped(Object gsonHtmlEscaped) throws
	 * ApiSessionException {
	 * 
	 * Method tmpmethod; try { tmpmethod =
	 * gsonHtmlEscaped.getClass().getMethod("createGson"); GsonBuilder bldrObj =
	 * (GsonBuilder) tmpmethod.invoke(gsonHtmlEscaped);
	 * 
	 * tmpmethod = gsonHtmlEscaped.getClass().getMethod("getGson"); Gson gsonObj =
	 * (Gson) tmpmethod.invoke(gsonHtmlEscaped);
	 * 
	 * Gson gson = bldrObj.disableHtmlEscaping() .registerTypeAdapter(Date.class,
	 * gsonObj.getAdapter(Date.class)) .registerTypeAdapter(java.sql.Date.class,
	 * gsonObj.getAdapter(java.sql.Date.class))
	 * .registerTypeAdapter(OffsetDateTime.class,
	 * gsonObj.getAdapter(OffsetDateTime.class))
	 * .registerTypeAdapter(LocalDate.class,
	 * gsonObj.getAdapter(LocalDate.class)).create();
	 * 
	 * return gson; } catch (Exception ex) { // TODO Auto-generated catch block
	 * ex.printStackTrace(); throw new ApiSessionException(ex.getMessage(), ex); } }
	 * 
	 * class HttpProxySelector extends ProxySelector { int proxyConnectionTries = 0;
	 * 
	 * @Override public List<Proxy> select(URI uri) { proxyConnectionTries = 0;
	 * List<Proxy> proxies = new ArrayList<>(); for (ProxyParameters proxyParameter
	 * : proxyParameters) { String proxyHost = proxyParameter.getHost(); String
	 * proxyPort = proxyParameter.getPort();
	 * 
	 * if (!Util.isNullOrEmpty(proxyHost)) { InetSocketAddress proxyInet = new
	 * InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)); Proxy proxy = new
	 * Proxy(Proxy.Type.HTTP, proxyInet); proxies.add(proxy); //
	 * LOG.trace("Proxy {}:{} added.", proxyHost, proxyPort); } } return proxies; }
	 * 
	 * @Override public void connectFailed(URI uri, SocketAddress socketAddress,
	 * IOException e) { proxyConnectionTries++; if (proxyConnectionTries ==
	 * proxyParameters.length) { throw new OAuthConnectionException(e); } } }
	 * 
	 * public OAuthTokenHolder getOauthTokenHolder() { return oauthTokenHolder; }
	 * 
	 * public void setOauthTokenHolder(OAuthTokenHolder oauthTokenHolder) {
	 * this.oauthTokenHolder = oauthTokenHolder; }
	 * 
	 * public static String base64ZippedString(final String str) throws
	 * ApiSessionException { String unzippedStr = null;
	 * 
	 * if ((str == null) || (str.length() == 0)) { throw new
	 * ApiSessionException("Cannot zip null or empty string."); }
	 * ByteArrayOutputStream baos = new ByteArrayOutputStream(); try
	 * (Base64OutputStream byteArrayOutputStream = new Base64OutputStream(baos)) {
	 * try (GZIPOutputStream gzipOutputStream = new
	 * GZIPOutputStream(byteArrayOutputStream)) {
	 * gzipOutputStream.write(str.getBytes(StandardCharsets.UTF_8)); }
	 * 
	 * } catch(IOException ex) { throw new
	 * ApiSessionException("Failed to zip content", ex); } try { unzippedStr =
	 * baos.toString("UTF-8").replace("\r\n", ""); } catch
	 * (UnsupportedEncodingException ex) { throw new
	 * ApiSessionException("Unsuppported encoding", ex); }
	 * 
	 * return unzippedStr; }
	 * 
	 * public static String zipBase64Encode(String value) throws ApiSessionException
	 * { Base64.Encoder encoder = Base64.getUrlEncoder(); try { if ((value == null)
	 * || value.isEmpty()) { throw new
	 * ApiSessionException("Cannot zip null or empty string."); }
	 * ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(); Deflater
	 * deflater = new Deflater(Deflater.DEFLATED, true); DeflaterOutputStream
	 * deflaterStream = new DeflaterOutputStream(bytesOut, deflater);
	 * deflaterStream.write(value.getBytes("UTF-8")); deflaterStream.finish();
	 * 
	 * return encoder.encodeToString(bytesOut.toByteArray()); } catch (IOException
	 * ex) { throw new ApiSessionException("Unable to Deflate and Base64 encode",
	 * ex); } }
	 * 
	 * public static String baseDecodeAndUnzip(String value) throws
	 * ApiSessionException { Base64.Decoder decoder = Base64.getUrlDecoder(); try {
	 * if ((value == null) || value.isEmpty()) { throw new
	 * ApiSessionException("Cannot unzip null or empty string."); }
	 * ByteArrayOutputStream bos = new ByteArrayOutputStream(); Inflater
	 * decompresser = new Inflater(true); InflaterOutputStream inflaterOutputStream
	 * = new InflaterOutputStream(bos, decompresser);
	 * inflaterOutputStream.write(decoder.decode(value));
	 * inflaterOutputStream.close();
	 * 
	 * String rbacRoles = new String(bos.toByteArray(), StandardCharsets.UTF_8);
	 * 
	 * return rbacRoles; } catch (IOException ex) { throw new
	 * ApiSessionException("Unable to unzip and Base64 decode", ex); } }
	 * 
	 * public static String getStackTrace(Throwable ex) { return
	 * ExceptionUtils.getStackTrace(ex); }
	 */
}