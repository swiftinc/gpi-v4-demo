package com.swift.developers.sandbox.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.*;
import com.swift.developers.sandbox.exception.ApiSessionException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import static com.swift.developers.sandbox.util.Constants.*;

public class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);
    private static final String PEM_CERTIFICATE_PREFIX = "-----BEGIN CERTIFICATE-----";
    private static final String PEM_CERTIFICATE_POSTFIX = "-----END CERTIFICATE-----";

    public static boolean isNullOrEmpty(String string) {
        return ((string == null) || (string.length() == 0));
    }
    
    public static void validateProxy(ProxyParameters[] proxyParameters) throws ApiSessionException {
        if (proxyParameters != null) {
            ProxyParameters[] uniqueProxyParameters = Arrays.stream(proxyParameters).distinct()
                    .toArray(length -> (ProxyParameters[]) Array.newInstance(ProxyParameters.class, length));
            if (uniqueProxyParameters.length != proxyParameters.length) {
                throw new ApiSessionException(
                        "Proxy should be unique by combination of host and port. The list of proxies contains duplicates:"
                                + System.lineSeparator() + Arrays.toString(proxyParameters));
            }
        }
    }
    
    public static Certificate extractCertificate(String alias, KeyStore keyStore) throws ApiSessionException {
        if ((alias == null) || alias.isEmpty()) {
            return null;
        }
        if (keyStore == null) {
            return null;
        }
        Certificate cert = null;
        try {
            cert = keyStore.getCertificate(alias);
        } catch (KeyStoreException ex) {
            LOG.error("Failed to extract certificate by alias = {}.{}{}", alias, System.lineSeparator(), ex.getMessage());
            throw new ApiSessionException(ex.getMessage(), ex);
        }

        return cert;
    }
        
    public static String extractPemCertificate(Certificate certificate) throws ApiSessionException {
        if (certificate == null) {
            return null;
        }
        
        String certificateBase64 = null;
        try {
            certificateBase64 = Base64.getEncoder().encodeToString(certificate.getEncoded());
        } catch (CertificateEncodingException ex) {
            LOG.error("Failed to encode certificate.{}{}", System.lineSeparator(), ex.getMessage());
            throw new ApiSessionException(ex.getMessage(), ex);
        }
        String wrappedCert = PEM_CERTIFICATE_PREFIX + System.lineSeparator() + certificateBase64
                + System.lineSeparator() + PEM_CERTIFICATE_POSTFIX;
        
        return wrappedCert;
    }
    
    public static ConnectionInfo createConnectionInfo(JsonObject configJsonObject) {
        ConnectionInfo connInfo = new ConnectionInfo();
        
        JsonObject configurationObject = returnConfigObject(configJsonObject, Constants.YAML_CONFIGURATION);
        if (configurationObject != null) {
        	JsonObject hostsObject = returnConfigObject(configurationObject, Constants.YAML_CONFIGURATION_HOSTS);
        	if (hostsObject != null) {
        		JsonObject apiGatewayObject = returnConfigObject(hostsObject, Constants.YAML_CONFIGURATION_API_GATEWAY);
        		if (apiGatewayObject != null) {
        			connInfo.setGatewayHost(returnConfigValue(apiGatewayObject, Constants.GATEWAY_HOST));
                    connInfo.setTrustAliasGateway(returnConfigValue(apiGatewayObject, Constants.GATEWAY_TRUST_ALIAS));
                    connInfo.setClientID(returnConfigValue(apiGatewayObject, Constants.CONSUMER_KEY));
                    connInfo.setClientSecret(returnConfigValue(apiGatewayObject, Constants.CONSUMER_SECRET));
                    
                    JsonObject authorizationServiceObject = returnConfigObject(apiGatewayObject, Constants.YAML_CONFIGURATION_AUTHORIZATION_SERVICE);
                    if (authorizationServiceObject != null) {
                    	connInfo.setOauthService(returnConfigValue(authorizationServiceObject, Constants.OAUTH_SERVICE));
                        connInfo.setAudience(returnConfigValue(authorizationServiceObject, Constants.AUDIENCE));
                        connInfo.setScope(returnConfigValue(authorizationServiceObject, Constants.SCOPE));
                    }
        		}
        		
        		JsonObject gpiConnectorObject = returnConfigObject(hostsObject, Constants.YAML_CONFIGURATION_GPI_CONNECTOR);
        		if (gpiConnectorObject != null) {
        			connInfo.setTrustAliasConnector(returnConfigValue(gpiConnectorObject, Constants.GPI_CONNECTOR_TRUST_ALIAS));
                    connInfo.setConnectorHost(returnConfigValue(gpiConnectorObject, Constants.CONNECTOR_HOST));
        		}
        	}

			JsonObject securityFootprintsObject = returnConfigObject(configurationObject,
					Constants.YAML_CONFIGURATION_SECURITY_FOOTPRINT);
			if (securityFootprintsObject != null) {
				JsonObject commonObject = returnConfigObject(securityFootprintsObject,
						Constants.YAML_CONFIGURATION_COMMON);
				if (commonObject != null) {
					connInfo.setCertPath(returnConfigValue(commonObject, Constants.CERT_PATH));
					connInfo.setCertPassword(returnConfigValue(commonObject, Constants.CERT_PASSWORD));
				}

				JsonObject softwareCertificateObject = returnConfigObject(securityFootprintsObject,
						Constants.YAML_CONFIGURATION_SOFTWARE_CERTIFICATE);
				if (softwareCertificateObject != null) {
					connInfo.setCertAlias(returnConfigValue(softwareCertificateObject, Constants.CERT_ALIAS));
				}

				List<SNLConnectionInfo> sagConnectionInfo = new ArrayList<>();
				JsonArray sagArray = returnConfigArray(securityFootprintsObject, "sags");
				if (sagArray != null) {
					for (JsonElement sagElement : sagArray) {
						JsonObject sagObj = sagElement.getAsJsonObject();
						if ((sagObj != null) && (commonObject != null)) {
							SNLConnectionInfo snlConnectionInfo = new SNLConnectionInfo();
							snlConnectionInfo.setHostname(returnConfigValue(sagObj, Constants.SNL_HOSTNAME));
							snlConnectionInfo.setPort(returnConfigValue(sagObj, Constants.SNL_PORT));
							snlConnectionInfo.setSslDN(returnConfigValue(sagObj, Constants.SNL_SSL_DN));
							snlConnectionInfo.setUserDN(returnConfigValue(sagObj, Constants.SNL_USER_DN));
							snlConnectionInfo.setMessagePartner(returnConfigValue(sagObj, Constants.SNL_MP));
							snlConnectionInfo.setLauKey(returnConfigValue(sagObj, Constants.SNL_LAU_KEY));
							snlConnectionInfo.setTrustStoragePath(returnConfigValue(commonObject, Constants.CERT_PATH));
							snlConnectionInfo
									.setTrustStoragePass(returnConfigValue(commonObject, Constants.CERT_PASSWORD));
							snlConnectionInfo.setTrustStorageAlias(returnConfigValue(sagObj, Constants.SNL_SSL_ALIAS));
							sagConnectionInfo.add(snlConnectionInfo);
						}
					}
					connInfo.setSnlConnectionInfo(sagConnectionInfo);
				}

				JsonArray lausArray = returnConfigArray(securityFootprintsObject, "laus");
				if (lausArray != null) {
					List<LAUInfo> lauInfoList = new ArrayList<>();
					for (JsonElement lauElement : lausArray) {
						JsonObject lauObj = lauElement.getAsJsonObject();
						if (lauObj != null) {
							LAUInfo lauInfo = new LAUInfo();

							lauInfo.setLauApplicationID(returnConfigValue(lauObj, Constants.LAU_APPLICATION_ID));
							lauInfo.setLauVersion(returnConfigValue(lauObj, Constants.LAU_VERSION));
							lauInfo.setLauKey(returnConfigValue(lauObj, Constants.LAU_KEY));
							String lauApplApiKey = returnConfigValue(lauObj, Constants.LAU_APPL_API_KEY);
							String lauRbacRole = returnConfigValue(lauObj, Constants.LAU_RBAC_ROLE);
							lauInfo.setLauSigned("(ApplAPIKey=" + lauApplApiKey + "),(RBACRole=[" + lauRbacRole + "])");

							lauInfoList.add(lauInfo);
						}
					}
					connInfo.setLauInfo(lauInfoList);
				}
				JsonObject basicObject = returnConfigObject(securityFootprintsObject,
						Constants.YAML_CONFIGURATION_BASIC);
				if (basicObject != null) {
					connInfo.setUsername(returnConfigValue(basicObject, Constants.BASIC_USERNAME));
					connInfo.setPassword(returnConfigValue(basicObject, Constants.BASIC_PASSWORD));
				}
			}

			/* Read parameters custom to the test framework. */
			JsonObject customFrameworkObject = returnConfigObject(configurationObject, Constants.YAML_CUSTOM_FRAMEWORK);
			connInfo.setProviderService(returnConfigValue(customFrameworkObject, GATEWAY_PROVIDER_SERVICE));
			try {
				connInfo.setCertSelector(returnConfigValue(customFrameworkObject, CERT_SELECTOR));
			} catch (InvalidParameterException ex) {
				/* Defaults to channel if the parameter is not defined. */
				connInfo.setCertSelector(Constants.CHANNEL);
			}
			try {
				String tmpStr = returnConfigValue(customFrameworkObject, HSM_SLOT);
				connInfo.setHsmSlot(Integer.parseInt(tmpStr));
			} catch (InvalidParameterException | NumberFormatException ex) {
				/* Defaults to slot 0. */
				connInfo.setHsmSlot(0);
			}
		}

		return connInfo;
    }
    
    public static ConnectionInfo createConnectionInfoOld(JsonObject configJsonObject) {
        ConnectionInfo connInfo = new ConnectionInfo();
        
        JsonObject connectionObject = returnConfigObject(configJsonObject, "connection");
        if (connectionObject != null) {
            connInfo.setCertAlias(returnConfigValue(connectionObject, CERT_ALIAS));
            connInfo.setCertPath(returnConfigValue(connectionObject, CERT_PATH));
            connInfo.setCertPassword(returnConfigValue(connectionObject, CERT_PASSWORD));
            connInfo.setTrustAliasGateway(returnConfigValue(connectionObject, GATEWAY_TRUST_ALIAS));
            connInfo.setTrustAliasConnector(returnConfigValue(connectionObject, GPI_CONNECTOR_TRUST_ALIAS));
            connInfo.setClientID(returnConfigValue(connectionObject, CONSUMER_KEY));
            connInfo.setClientSecret(returnConfigValue(connectionObject, CONSUMER_SECRET));
            connInfo.setGatewayHost(returnConfigValue(connectionObject, GATEWAY_HOST));
            connInfo.setConnectorHost(returnConfigValue(connectionObject, CONNECTOR_HOST));
            connInfo.setOauthService(returnConfigValue(connectionObject, OAUTH_SERVICE));
            connInfo.setAudience(returnConfigValue(connectionObject, AUDIENCE));
            connInfo.setScope(returnConfigValue(connectionObject, SCOPE));

            List<SNLConnectionInfo> sagConnectionInfo = new ArrayList<>();
            JsonArray sagArray = returnConfigArray(connectionObject, "hsm");
            if (sagArray != null) {
                for (JsonElement sagElement : sagArray) {
                    JsonObject sagObj = sagElement.getAsJsonObject();
                    SNLConnectionInfo snlConnectionInfo = new SNLConnectionInfo();
                    snlConnectionInfo.setHostname(returnConfigValue(sagObj, SNL_HOSTNAME));
                    snlConnectionInfo.setPort(returnConfigValue(sagObj, SNL_PORT));
                    snlConnectionInfo.setSslDN(returnConfigValue(sagObj, SNL_SSL_DN));
                    snlConnectionInfo.setUserDN(returnConfigValue(sagObj, SNL_USER_DN));
                    snlConnectionInfo.setMessagePartner(returnConfigValue(sagObj, SNL_MP));
                    snlConnectionInfo.setLauKey(returnConfigValue(sagObj, SNL_LAU_KEY));
                    snlConnectionInfo.setTrustStoragePath(returnConfigValue(sagObj, SNL_SSL_PATH));
                    snlConnectionInfo.setTrustStoragePass(returnConfigValue(sagObj, SNL_SSL_PASSWORD));
                    snlConnectionInfo.setTrustStorageAlias(returnConfigValue(sagObj, SNL_SSL_ALIAS));
                    sagConnectionInfo.add(snlConnectionInfo);
                }
                connInfo.setSnlConnectionInfo(sagConnectionInfo);
            }

            List<String> parentPropertiesLAU = Arrays.asList("connection", "lau");
            LAUInfo lauInfo = new LAUInfo();
            lauInfo.setLauApplicationID(returnConfigValue(configJsonObject, LAU_APPLICATION_ID, parentPropertiesLAU));
            lauInfo.setLauVersion(returnConfigValue(configJsonObject, LAU_VERSION, parentPropertiesLAU));
            lauInfo.setLauKey(returnConfigValue(configJsonObject, LAU_KEY, parentPropertiesLAU));
            String lauApplApiKey = returnConfigValue(configJsonObject, LAU_APPL_API_KEY, parentPropertiesLAU);
            String lauRbacRole = returnConfigValue(configJsonObject, LAU_RBAC_ROLE, parentPropertiesLAU);
            lauInfo.setLauSigned("(ApplAPIKey=" + lauApplApiKey + "),(RBACRole=[" + lauRbacRole + "])");

            // connInfo.setLauInfo(lauInfo);
            
            connInfo.setProviderService(returnConfigValue(connectionObject, GATEWAY_PROVIDER_SERVICE));
            connInfo.setCertSelector(returnConfigValue(connectionObject, CERT_SELECTOR));
            
            int slotnum;
            try {
            	slotnum = Integer.parseInt(returnConfigValue(connectionObject, HSM_SLOT));
            }
            catch (NumberFormatException ex) {
            	/* Defaults to slot 0. */
            	slotnum = 0;
            }
            connInfo.setHsmSlot(slotnum);
        }

        return connInfo;
    }   
    
    public static JsonObject returnConfigObject(JsonObject parentJsonObject, String parentProperty) {
        JsonObject propertyParentObject = null;
        if (parentJsonObject.has(parentProperty)) {
            propertyParentObject = parentJsonObject.getAsJsonObject(parentProperty);
        }
        return propertyParentObject;
    }
   
    public static String returnConfigValue(JsonObject parentJsonObject, String property) {
        String value;

        JsonElement valueJson = parentJsonObject.get(property);
        if (valueJson != null) {
            value = valueJson.getAsString().isEmpty() ? null : valueJson.getAsString();
        } else {
            throw new InvalidParameterException(property + " property expected and is not provided in config file.");
        }

        return value;
    }
    
    public static String returnConfigValue(JsonObject jsonObject, String property, List<String> parentProperties) {
        JsonObject propertyParentObject = jsonObject;
        if (parentProperties != null) {
            for (String parentProperty : parentProperties) {
                if (propertyParentObject.has(parentProperty)) {
                    propertyParentObject = propertyParentObject.getAsJsonObject(parentProperty);
                }
            }
        }
        return returnConfigValue(propertyParentObject, property);
    }
    
    public static JsonArray returnConfigArray(JsonObject jsonObject, String property) {
        JsonObject propertyParentObject = jsonObject;
        JsonArray propertyParentArray = null;
        if (propertyParentObject.has(property)) {
            propertyParentArray = propertyParentObject.getAsJsonArray(property);
        }
        return propertyParentArray;
    }
    
    public static String returnConnectionConfigValue(JsonObject jsonObject, String property) {
        return returnConfigValue(jsonObject, property, Arrays.asList("connection"));
    }
    
    public static String returnServicePropertyValue(JsonObject jsonObject, String property) {
        return returnConfigValue(jsonObject, property, Arrays.asList("properties", "service"));
    }
    
    public static String returnNotificationPropertyValue(JsonObject jsonObject, String property) {
        return returnConfigValue(jsonObject, property, Arrays.asList("properties", "notification"));
    }
    
    public static String returnSubscriptionPropertyValue(JsonObject jsonObject, String property) {
        return returnConfigValue(jsonObject, property, Arrays.asList("properties", "subscription"));
    }

	public static JsonObject readConfigurationPropertiesJson(String configPath) throws ApiSessionException {
        JsonObject jsonObject = null;
        JsonParser jsonParser = new JsonParser();
        
        try {
        	Object obj = jsonParser.parse(new FileReader(configPath));
        	jsonObject = (JsonObject) obj;
        }
        catch (FileNotFoundException | JsonIOException | JsonSyntaxException ex) {
        	throw new ApiSessionException(ex.getMessage(), ex);
        }
              
        // jsonObject = new JsonParser().parse(configPath).getAsJsonObject();        
        return jsonObject;
    }
	
	public static JsonObject readConfigurationPropertiesYaml(String configPath) throws ApiSessionException {
		byte[] fileContent = null;
		JsonObject jsonObject = null;
		
		try {
			fileContent = Files.readAllBytes((new File(configPath)).toPath());
			String tmpStr = new String(fileContent, StandardCharsets.UTF_8);
			ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
			Object obj = yamlReader.readValue(tmpStr, Object.class);
			ObjectMapper jsonWriter = new ObjectMapper();
			
			JsonParser jsonParser = new JsonParser();
			// System.out.println(jsonWriter.writeValueAsString(obj));
			jsonObject = (JsonObject) jsonParser.parse(new StringReader(jsonWriter.writeValueAsString(obj)));
		}
		catch (IOException | JsonIOException | JsonSyntaxException ex) {
			throw new ApiSessionException(ex.getMessage(), ex);
		}
		
		return jsonObject;
    }
    
    public static String getBasePath(JsonObject configJson, String host, String service) {
        return returnConnectionConfigValue(configJson, host) + returnConnectionConfigValue(configJson, service);
    }
    
    public static String getStackTrace(Throwable ex) {
		return ExceptionUtils.getStackTrace(ex);
	}
    
    /**
     * Base64 encode and zip the string value.
     * @param value
     * @return
     * @throws ApiSessionException
     */
    public static String zipBase64Encode(String value) throws ApiSessionException {
		Base64.Encoder encoder = Base64.getUrlEncoder();        
		try {
			if ((value == null) || value.isEmpty()) {
				throw new ApiSessionException("Cannot zip null or empty string.");
			}
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			Deflater deflater = new Deflater(Deflater.DEFLATED, true);
			DeflaterOutputStream deflaterStream = new DeflaterOutputStream(bytesOut, deflater);
			deflaterStream.write(value.getBytes("UTF-8"));
			deflaterStream.finish();

			return encoder.encodeToString(bytesOut.toByteArray());
		} catch (IOException ex) {
			throw new ApiSessionException("Unable to Deflate and Base64 encode", ex);
		}
	}
	
    /**
     * Unzip and Base64 decode the string value.
     * @param value
     * @return
     * @throws ApiSessionException
     */
	public static String baseDecodeAndUnzip(String value) throws ApiSessionException {
		Base64.Decoder decoder = Base64.getUrlDecoder();
		try {
			if ((value == null) || value.isEmpty()) {
				throw new ApiSessionException("Cannot unzip null or empty string.");
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Inflater decompresser = new Inflater(true);
			InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(bos, decompresser);
			inflaterOutputStream.write(decoder.decode(value));
			inflaterOutputStream.close();

			String rbacRoles = new String(bos.toByteArray(), StandardCharsets.UTF_8);
			
			return rbacRoles;
		} catch (IOException ex) {
			throw new ApiSessionException("Unable to unzip and Base64 decode", ex);                    
		}
	}
}
