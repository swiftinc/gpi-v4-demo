# SWIFT gpi API demo application in Java

Make calls to SWIFT APIs is easy using SWIFT SDK. All you need to do is add the SWIFT SDKs as dependency when building your Java application through Maven or Gradle.

We built this demo Java application with Maven to show you how we are using it to make calls to SWIFT gpi APIs in the [API Sandbox](https://developer.swift.com/reference#sandbox-overview).


## Getting Started ##

### Prerequisites ###
* Java 1.7 and above
* maven 3.5.* and above

### Install SWIFT SDK ###

Download [SWIFT SDK](https://developer.swift.com/swift-sdk) from SWIFT Developer Portal, login is required for download.

Unpackage the zip file and install the dependency in your local .m2 repository.

```
$ install.sh
```

### Configure runtime SDK propertise ###

Update ```config/config-swift-sdk.yaml``` with your application credentials, consumer-key & consumer-secret. Obtain from SWIFT Developer Portal by [creating an app](https://developer.swift.com/reference#sandbox-getting-started).

### Build ###

```
$ mvn clean install
```

### Run ###

```
$ java -Dlog4j.configuration=file:config/log4j.properties -jar target/gpi-v4-demo-1.0.0-SNAPSHOT-jar-with-dependencies.jar config/config-swift-sdk.yaml

```
## Authors
alex.salinas@swift.com
