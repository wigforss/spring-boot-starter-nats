package org.kasource.spring.nats.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.kasource.spring.nats.config.java.NatsAvroConfiguration;

@ConditionalOnProperty(prefix = "spring.nats", name = "ser-de-type", havingValue = "AVRO")
@Configuration
@Import(NatsAvroConfiguration.class)
public class NatsAvroBeans {
}
