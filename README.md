# NATS for Spring Boot
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 
![](https://img.shields.io/badge/Package-JAR-2396ad)
![](https://img.shields.io/badge/Repository-Maven%20Central-2396ad)  
![](https://img.shields.io/badge/Java-11%2B-d6a827)
![](https://github.com/wigforss/spring-boot-starter-nats/workflows/Test%20and%20Deploy/badge.svg)
[![codecov](https://codecov.io/gh/wigforss/spring-boot-starter-nats/branch/master/graph/badge.svg)](https://codecov.io/gh/wigforss/spring-boot-starter-nats)


###### NATS Client 2.5, Spring Boot 2.x
This project enables Spring Boot auto configuration for [NATS](https://nats.io/). 

Simply add the dependency to your Spring Boot project and you are good to go.
```
 <dependency>
    <groupId>org.kasource</groupId>
    <artifactId>spring-boot-starter-nats</artifactId>
    <version>${spring-boot.nats.starter.version}</version>
</dependency>
```

The auto configuration will try to connect to the default NATS port (4222) on localhost.

To change the connection add the *spring.nats.connection.urls* property to your application.yml
```
spring:
   nats:
      connection:
         urls:
         - nats://${hostname}:${port}
```

## Sending Messages

A bean of type **org.kasource.spring.nats.publisher.NatsTemplate** will be available for injection (autowiring).

```
import org.kasource.spring.nats.NatsTemplate;
    ...
    @Autowired
    private NatsTemplate natsTemplate;
    
    ...
    natsTemplate.publish(anyObject, "a-subject")
```

## Receiving Messages
Messages can be read by setting a Consumer either by annotating a bean method with ```@org.kasource.spring.nats.annotation.Consumer``` or by registering a **java.util.function.Consumer**. 

### Annotated Methods 
By annotating a bean method with ```@Consumer``` one can listen to NATS messages.
 
This method must be public with one or two arguments and void return type. If the method has two arguments one of the must be of type io.nats.client.Message.
```
import org.kasource.spring.nats.annotation.Consumer;
...
    @Consumer(subject = "a-subject")
    public void onMessage(MyObject myObject) {
        System.out.println("onMessage: " + myObject);
    }
 ...
```
The io.nats.client.Message object can also be bound to an argument. The queueName can also be specified in the Consumer annotation. 
```
import org.kasource.spring.nats.annotation.Consumer;
import io.nats.client.Message;
...
    @Consumer(subject = "a-subject", queueName="consumerGroup1")
    public void onMessage(MyObject myObject, Message message) {
        System.out.println("onMessage: " + myObject);
    }
 ...
```

### Register Consumer Function
By registering a **java.util.function.Consumer** object or Lambda function with the NatsConsumerManager

```
import org.kasource.spring.nats.consumer.NatsConsumerManager;
...
    @Autowired
    private NatsConsumerManager manager;
        
    @PostConstruct
    void setup() {
        manager.register(System.out::println, MyObject.class, "a-subject");
    }
 ...
```
One could also consume the Message object itself, which would bypass and deserialization of the message data.
```
import org.kasource.spring.nats.consumer.NatsConsumerManager;
import io.nats.client.Message;
...
    @Autowired
    private NatsConsumerManager manager;
        
    @PostConstruct
    void setup() {
        manager.register(System.out::println, Message.class, "a-subject");
    }
 ...
```
By registering a **java.util.function.BiConsumer<?, Message>** both the payload and the message can be received.
```
import org.kasource.spring.nats.consumer.NatsConsumerManager;
import io.nats.client.Message;
...
    @Autowired
    private NatsConsumerManager manager;
        
    @PostConstruct
    void setup() {
        manager.register((p, m) -> System.out.println(String.format("Message '%s' payload '%s'", m, p)), MyObject.class, "a-subject");
    }
 ...
```
## SerDe (Serialization / Deserialization)
NatsTemplate will serialize objects and the Consumers will deserialize objects automatically. Jackson will automatically be used for SerDe if its on the classpath.

The following SerDe frameworks are supported out of the box and can be used instead of Jackson.

| SerDe Type  | Value    |
| :-----------| :--------| 
|  -          | NONE     |
|  Jackson    | JACKSON  |
|  Gson       | GSON     |
|  Java SerDe | JAVA     |
|  JAXB       | JAXB     |
|  Avro       | AVRO     |
|  Protobuf   | PROTOBUF |
|  Kryo       | KRYO     |
|  Thrift     | THRIFT   |
 
To change SerDe, configure the ser-de-type property like:
```
spring:
   nats:
      ser-de-type: KRYO
```

As an alternative to configure the ser-de-type you can provide a custom SerDe by implementing **org.kasource.spring.nats.message.serde.NatsMessageSerDeFactory**.
When such bean is found it will be used instead of Jackson.


## Message Validation
The SerDe process supports validation for the Message object before serialization and after deserialization.

JSR 303 validation can be enabled by configuration, default its disabled.
```
spring:
   nats:
      enable-validation: true
```
When enabling validation a javax.validation.Validator bean must be present in the ApplicationContext.
The SerDe validator will validate all objects which classes are annotated with **org.springframework.validation.annotation.Validated**.

You can provide a custom validator instead of the JSR 303 validator provided by implementing **org.kasource.spring.nats.message.validation.MessageObjectValidator** and exposing the custom implementation as bean in the ApplicationContext.
##### Optional Dependencies
javax.validation needs to be on the classpath and a bean of type **javax.validation.Validator** found in the *Application Context*.
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
or
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## JSON Schema Validator (Jackson)
If Jackson SerDe is used the actual JSON data (bytes) sent and received can be validated against a JSON schema.

To discover and load and validate with JSON Schemas the [The JSON Schema Registry](https://github.com/wigforss/json-schema-registry) library is used, which is based on the [FGE JSON Schema Validator](https://github.com/java-json-tools/json-schema-validator) and in turn based on Jackson.

If the property **spring.nats.jackson.json-schema.scan-packages** is set and the optional dependencies are on the classpath a JSON Schema data validator implementation will be added to the *Application Context*. 

This feature can be disabled by setting the property **spring.nats.jackson.json-schema.enable-validation** to false.
##### Optional Dependencies
The Jackson and the json-schema-discovery libs needs to be added as dependencies to enable this feature. As well as a ObjectMapper bean in the *Application Context*.
```
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-json</artifactId>
</dependency>
```
or
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
and
```
<dependency>
    <groupId>org.kasource</groupId>
    <artifactId>json-schema-registry</artifactId>
    <version>0.1</version>
</dependency>
```

## XML Schema Validator (JAXB)
If JAXB SerDe is used the actual XML data (bytes) sent and received can be validated against a XML schema.

This requires the XmlSchema annotation in **package-info.java** to have the location attribute set to either a URI or a file path.
```
@javax.xml.bind.annotation.XmlSchema(namespace = "http://kasource.org/schema/nats/test/person", location = "src/test/resources/xml/person.xsd")
package org.kasource.spring.nats.integration.xml;
```
##### Optional Dependencies
The JAXB framework is no longer part of the JDK/JRE and needs to be added.
```
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.1</version>
</dependency>
<dependency>
    <groupId>javax.activation</groupId>
    <artifactId>activation</artifactId>
    <version>1.1</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>2.3.1</version>
</dependency>
```
## Metrics
If MicroMeter is available on the classpath a NATS Metric Registry bean will be added, that will record metrics for the Connection, the Consumers (Dispatchers) and the Errors.

### Connection
| Tag                | Description  |
| :------------------| :--------------| 
| url                | The connected URL |
| connection-name    | The optional name configured for the connection | 


| Metric                             | Type  | Description                       |
| :----------------------------------|-------| :---------------------------------| 
| nats.connection.dropped.count      | Gauge | Number of dropped messages        |
| nats.connection.reconnect.count    | Gauge | Number of re-connect attempts     | 
| nats.connection.in.message.count   | Gauge | Number of incoming messages       |
| nats.connection.out.message.count  | Gauge | Number of outgoing messages       |
| nats.connection.in.bytes           | Gauge | Number of incoming bytes          |
| nats.connection.out.message.count  | Gauge | Number of outgoing bytes          |
| nats.connection.status             | Gauge | 0 = DISCONNECTED, 1 = CONNECTED, 2 = CLOSED, 3 = RECONNECTING, 4 = CONNECTING |          |

### Consumer (Dispatcher)
| Tag           | Description  |
| :-------------| :--------------| 
| subject       | The subject |
| queue-name    | The (optional) queue name | 


| Metric                                  | Type  | Description                                |
| :---------------------------------------|-------| :------------------------------------------| 
| nats.subscription.dropped.count         | Gauge | Number of dropped messages                 |
| nats.subscription.delivered.count       | Gauge | Number of messages consumed                | 
| nats.subscription.pending.byte.count    | Gauge | Number of bytes not read                   |
| nats.subscription.pending.message.count | Gauge | Number of messages not read                |
| nats.subscription.pending.byte.limit    | Gauge | Number of pending bytes before dropping    |
| nats.subscription.pending.message.limit | Gauge | Number of pending messages before dropping |
| nats.subscription.active                | Gauge | 0 = Not Active, 1 = Active                 |

### Errors
| Tag                   | Description  |
| :---------------------| :--------------| 
| url                   | The connected URL |
| connection-name       | The optional name configured for the connection | 
| exception             | The exception class name |
| root-cause (optional) | The route-cause class name | 

| Metric                      | Type    | Description                       |
| :---------------------------|---------| :---------------------------------| 
| nats.connection.error.count | Counter | Number of dropped messages        |

##### Optional Dependencies
 Micrometer needs to be on the classpath for this feature and a bean of type **io.micrometer.core.instrument.MeterRegistry** found in the *Application Context*.
```
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
</dependency>
```
or
```
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## TLS
TLS can be enabled by setting the **spring.nats.connection.tls.enabled** property to true.
Both trust and identity store is expected to be normal Java key stores.


## JWT
NATS JWT Authentication with NKey is also supported by configuring **spring.nats.connection.jwt.token** and **spring.nats.connection.jwt.n-key**

## Health
A NATS HealthIndicator (NatsHealthIndicator) is included an used automatically and can be disabled by setting the property **management.health.nats.enabled** to false.

## Application Events
The following ApplicationEvents are published:

* NatsConnectionEvent - When connection state changes
* NatsErrorEvent - On errors 
* NatsExceptionEvent - On Exceptions (in dispatcher when consuming messages).
* NatsSlowConsumerEvent - When a slow consumer is detected

## Configuration
An empty configuration will result in a connection to default URL without TLS and validation or any credentials.

### spring.nats 
[NatsConfigProperties](https://github.com/wigforss/spring-boot-starter-nats/src/master/src/main/java/org/kasource/spring/nats/config/NatsConfigProperties.java)

| Property             | Type    | Required | Default | Description  |
| :--------------------| :-------| ---------| :-------| :------------|
| auto-start-consumers | Boolean | No       | true    | Auto-start the message consumers |
| ser-de-type          | Enum    | No       | JACKSON | Sets the SerDe to use. Allowed values are:  NONE, JACKSON, GSON ,JAVA, JAXB, AVRO, PROTOBUF, KRYO, THRIFT |
| enable-validation    | Boolean | No       | false   | Enable JSR 303 validation of objects during SerDe  |


### spring.nats.connection
| Property              | Type           | Required | Default            | Description  |
| :---------------------| :--------------| ---------| :------------------| :------------|
| urls                  | List of String | No       | ['nats://localhost:4222'] | List of servers to connect to |
| max-reconnect         | Integer        | No       | 60                 | Maximum number of reconnect attempts  |
| name                  | String         | No       | -                  | Optional name for connection  |
| username              | String         | No       | -                  | Connection user name|
| password              | String         | No       | -                  | Connection password  |
| timeout-seconds       | Integer        | No       | 2                  | Connection timeout|
| drain-timeout-seconds | Integer        | No       | 2                  | Drain timeout, when JVM is stopped. 0 waits until all messageas are drained with no upper limit in time  |



### spring.nats.connection.tls
| Property                | Type    | Required | Default | Description  |
| :-----------------------| --------| ---------| --------| :-------------|
| enabled                 | Boolean | No       | false   | Enable TLS, with no trust-store configured all "known" CA's will be trusted |
| trust-store             | String  | No       | -       | Resource path to java keystore (jks) file |
| trust-store-password    | String  | No       | -       | Key store password  |
| identity-store          | String  | No       | -       | Resource path to java keystore (jks) file |
| identity-store-password | String  | No       | -       | Key store password  |

### spring.nats.connection.jwt
| Property  | Type            | Required | Default | Description         |
| :---------| :---------------| ---------| --------| :--------------------|
| token     | String (base64) | Yes      | -       | The JWT token       |
| n-key     | String (base64) | Yes      | -       | The NKey seed bytes |

### spring.nats.jackson.json-schema
| Property          | Type           | Required | Default | Description  |
| :-----------------| :--------------| ---------| --------| :--------------|
| enable-validation | Boolean        | No       | true    | Enable Json Schema validation for Jackson SerDe |
| scan-packages     | List of String | Yes      | -       | List of java packages to scan for ```@JsonSchema``` annotated classes, will also scan sub-packages. |


