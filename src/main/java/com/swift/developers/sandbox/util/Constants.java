package com.swift.developers.sandbox.util;

public final class Constants {	
    public static final String GATEWAY_HOST = "host";
    public static final String CONNECTOR_HOST = "host";
    public static final String OAUTH_SERVICE = "url";   
    public static final String AUDIENCE = "audience";
    public static final String SCOPE = "scope";
    public static final String CONSUMER_KEY = "consumer_key";
    public static final String CONSUMER_SECRET = "consumer_secret";
    public static final String CERT_PATH = "cert_path";
    public static final String CERT_ALIAS = "cert_alias";
    public static final String GATEWAY_TRUST_ALIAS = "trust_alias";
    public static final String GPI_CONNECTOR_TRUST_ALIAS = "trust_alias";
    public static final String CERT_PASSWORD = "cert_password";
    
    public static final String SNL_HOSTNAME = "hostname";
    public static final String SNL_PORT = "port";
    public static final String SNL_SSL_DN = "ssl_dn";
    public static final String SNL_MP = "mp";
    public static final String SNL_USER_DN = "user_dn";
    public static final String SNL_LAU_KEY = "lau_key";
    public static final String SNL_SSL_PATH = "ssl_path";
    public static final String SNL_SSL_PASSWORD = "ssl_password";
    public static final String SNL_SSL_ALIAS = "ssl_alias";

    public static final String LAU_VERSION_DEFAULT = "1.0";
    public static final String LAU_APPLICATION_ID = "lau_application_id";
    public static final String LAU_VERSION = "lau_version";
    public static final String LAU_KEY = "lau_key";
    public static final String LAU_APPL_API_KEY = "lau_appl_api_key";
    public static final String LAU_RBAC_ROLE = "lau_rbac_role";
         
    public static final String ALGORITHM = "AES";
    public static final String CIPHER = "AES/GCM/NoPadding";
    public static final String CIPHER_ALGORITHIM = "SunJCE"; 
    
    public static final String BASIC_USERNAME = "username";
    public static final String BASIC_PASSWORD = "password";

    public static final String YAML_CONFIGURATION = "configuration";
    public static final String YAML_CONFIGURATION_HOSTS = "hosts";
    public static final String YAML_CONFIGURATION_API_GATEWAY = "api_gateway";
    public static final String YAML_CONFIGURATION_AUTHORIZATION_SERVICE = "authorization_service";
    public static final String YAML_CONFIGURATION_GPI_CONNECTOR = "gpi_connector";
    public static final String YAML_CONFIGURATION_SERVICES = "services";
    public static final String YAML_CONFIGURATION_SECURITY_FOOTPRINT = "security_footprints";
    public static final String YAML_CONFIGURATION_COMMON = "common";
    public static final String YAML_CONFIGURATION_BASIC = "basic";
    public static final String YAML_CONFIGURATION_SOFTWARE_CERTIFICATE = "software_certificate";
    
    public static final String YAML_CUSTOM_FRAMEWORK = "custom_framework";
    
    public static final int SAG_SLOT = 0;
	public static final String BEARER = "Bearer ";
	public static final String TOKEN_CACHE_NAME = "OAuthTokenHolder";
    
    public static final String GATEWAY_PROVIDER_SERVICE = "gateway_provider_service";
    public static final String CERT_SELECTOR = "cert_selector";
    public static final String HSM_SLOT = "hsm_slot";
    public static final String CHANNEL = "channel";
    public static final String HSM = "hsm";
    
    public static final String PROPERTIES_NOTIFSERVICE_SERVICE_NAME = "SERVICE_NAME";
    public static final String PROPERTIES_NOTIFSERVICE_CATEGORY = "CATEGORY";
    public static final String PROPERTIES_NOTIFSERVICE_DESCRIPTION = "DESCRIPTION";
    public static final String PROPERTIES_NOTIFSERVICE_SUBJECTDN = "SUBJECT_DN";
    public static final String PROPERTIES_NOTIFSERVICE_INSTITUITION = "INSTITUITION";
    public static final String PROPERTIES_NOTIFSERVICE_RBAC_ROLE = "RBAC_ROLE";
    public static final String PROPERTIES_NOTIFSERVICE_NOTIF_ROLE = "NOTIF_ROLE";
    public static final String PROPERTIES_NOTIFSERVICE_XREQUEST_ID = "XREQUEST_ID";    
    
    public static final String CREATE_SERVICE_CATEGORY_V1 = "/services";
    public static final String GATEWAY_PROVIDER_SERVICE_V1 = "GATEWAY_PROVIDER_SERVICE_V1";    
    
    public static final String CONN_SESS_CONFIG_FILEPATH = "C:\\SWIFT\\APIVAS_CFG\\config.json";
	public static final String CONN_SESS_SECRETS_FILEPATH = "C:\\SWIFT\\APIVAS_CFG\\secrets";
}
