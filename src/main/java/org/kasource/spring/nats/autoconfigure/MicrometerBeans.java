package org.kasource.spring.nats.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.kasource.spring.nats.metrics.NatsMetricsRegistry;

import io.micrometer.core.instrument.MeterRegistry;
import io.nats.client.Connection;

@ConditionalOnClass(MeterRegistry.class)
@Configuration
public class MicrometerBeans {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private Connection natsConnection;

    @ConditionalOnMissingBean
    @Bean
    public NatsMetricsRegistry natsMetricsFactory() {
        return new NatsMetricsRegistry(meterRegistry, natsConnection);
    }

}
