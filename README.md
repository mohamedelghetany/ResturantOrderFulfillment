# ResturantOrderFulfillment

Simple Java Client/ Server (Netty) Order fulfillment 

## Prerequisites

1. java8 [setup](https://www.oracle.com/java/technologies/javase-jre8-downloads.html)
2. maven [setup](https://maven.apache.org/install.html)

## Packages

**Server**: Netty Server to process and dispatch orders 

**Client**: Simple client that send orders to the server. That's where the orders JSON file being parsed and sent to the server

## Server Design 

Please refer to Server [README.md](./Server/README.md)

## Build 

From Server folder run the following maven command: `mvn install`

## Running 

From Server folder run the following maven command: `mvn exec:java`

## Changing Log Level

Default log level is `INFO` 

To change the level  set `log4j.rootLogger=<Level>` in [log4j2.xml](./Server/src/main/resources/log4j.properties)

You will need to rebuild the server : `mvn install`

Note: `DEBUG` level is very verbose so setting log level to `DEBUG` will have negative effet on performance.

## Changing Server Configuration

Configuration is based on Java Properties file [config.properties](./Server/src/main/resources/config.properties)

All configration have default values, these values are specified at [ServerProperties.java](./Server/src/main/java/common/ServerProperties.java) 

Example: 

`public static PropertyKey<Integer> PORT = new IntegerPropertyKey("port", 8080)`

`("port", 8080)`: 1st value is the key, 2nd is the default value so to override the PORT, just add in config.properties `port=1234`

## Changing Client Configuration

Similar to Server Configuration, it is based on Java Properties file [config.properties](./Client/src/main/resources/config.properties)

The only availabe configuration is the `rate` 

In [ClientProperties.java](./Client/src/main/java/ClientProperties.java) 

`public static PropertyKey<Integer> RATE = new IntegerPropertyKey("rate", 2)`

`("rate", 2)`: 1st value is the key, 2nd is the default value so to override the RATE, just add in config.properties `rate=5`

