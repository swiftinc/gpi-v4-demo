# SWIFT gpi API demo application in Java

Make calls to SWIFT APIs is easy using SWIFT SDK. All you need to do is add the SWIFT SDKs as dependency when building your Java application through Maven or Gradle.

We built this demo Java application with Maven to show you how we are using it to make calls to SWIFT gpi APIs in the [API Sandbox](https://developer.swift.com/reference#gsg).


## Getting Started ##

### Prerequisites ###
* Java 11 and below
* maven 3.5.* and above

### Install SWIFT SDK ###

Download [SWIFT SDK](https://developer.swift.com/swift-sdk) from SWIFT Developer Portal, login is required for download.

Unpackage the zip file and install the dependency in your local .m2 repository.

```
$ install.sh
```

### Configure runtime SDK propertise ###

Update ```config/config-swift-connect.yaml``` with your application credentials, consumer-key & consumer-secret. Obtain from SWIFT Developer Portal by [creating an app](https://developer.swift.com/reference#sandbox-getting-started).

To use forward proxies update ```config/config-swift-connect-fp.yaml``` with your application credentials, consumer-key & consumer-secret and forward proxy information. Obtain from SWIFT Developer Portal by [creating an app](https://developer.swift.com/reference#sandbox-getting-started).

### Build ###

```
$ mvn clean install
```

### Run ###

```
$ java -Dlog4j.configuration=file:config/log4j.properties -jar target/gpi-v4-demo-jar-with-dependencies.jar config/config-swift-connect.yaml

```
To use forward proxies:
```
$ java -Dlog4j.configuration=file:config/log4j.properties -jar target/gpi-v4-demo-jar-with-dependencies.jar config/config-swift-connect-fp.yaml

```

## Keystore
The keystore (conf/keystore.jks) has these several entried that may need renewing when the Sandbox certificate is renewed:
- globalsign_root_ca_r3, 18 Oct 2022, trustedCertEntry, Sandbox server root ca
- globalsign_rsa_ov_ssl_ca_2018, 18 Oct 2022, trustedCertEntry, Sandbox server sub ca
- sandbox_pub, 18 Oct 2022, trustedCertEntry, Sandbox server cert. Potentially not needed given its roots are present (see above)
- selfsigned, 18 Jun 2020, PrivateKeyEntry, Demo client key

## Authors
alex.salinas@swift.com
