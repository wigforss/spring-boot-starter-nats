package org.kasource.spring.nats.autoconfigure;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import org.kasource.spring.nats.NatsTemplate;
import org.kasource.spring.nats.NatsTemplateImpl;
import org.kasource.spring.nats.config.NatsConfigProperties;
import org.kasource.spring.nats.connection.ConnectionFactoryBean;
import org.kasource.spring.nats.connection.TlsConfiguration;
import org.kasource.spring.nats.consumer.NatsConsumerManager;
import org.kasource.spring.nats.consumer.NatsConsumerManagerImpl;
import org.kasource.spring.nats.consumer.NatsPostBeanProcessor;
import org.kasource.spring.nats.event.NatsConnectionListener;
import org.kasource.spring.nats.event.NatsErrorListener;
import org.kasource.spring.nats.health.NatsHealthIndicator;
import org.kasource.spring.nats.logging.ConnectionStatusLogger;
import org.kasource.spring.nats.logging.ErrorLogger;
import org.kasource.spring.nats.message.serde.NatsMessageSerDeFactory;
import org.kasource.spring.nats.metrics.NatsMetricsRegistry;

import io.nats.client.Connection;

@SuppressWarnings({"checkstyle:classdataabstractioncoupling", "checkstyle:classfanoutcomplexity"})
@EnableConfigurationProperties(NatsConfigProperties.class)
@Import({
                NatsJacksonBeans.class,
                NatsGsonBeans.class,
                NatsAvroBeans.class,
                NatsJavaSerDeBeans.class,
                NatsJaxbBeans.class,
                NatsKryoBeans.class,
                NatsProtobufBeans.class,
                NatsThriftBeans.class,
                JsonSchemaBeans.class,
                MicrometerBeans.class,
                ValidationBeans.class
})
@Configuration
public class NatsAutoConfiguration {

    @Autowired
    private NatsConfigProperties configProperties;


    @ConditionalOnMissingBean(Connection.class)
    @Bean
    public ConnectionFactoryBean connectionFactoryBean(NatsConnectionListener natsConnectionListener, NatsErrorListener natsErrorListener) {
        ConnectionFactoryBean factoryBean = new ConnectionFactoryBean();
        factoryBean.setUrls(configProperties.getConnection().getUrls());
        factoryBean.setConnectionListener(natsConnectionListener);
        factoryBean.setErrorListener(natsErrorListener);
        factoryBean.setConnectionName(configProperties.getConnection().getName());
        factoryBean.setMaxReconnects(configProperties.getConnection().getMaxReconnect());
        factoryBean.setPassword(configProperties.getConnection().getPassword());
        factoryBean.setUsername(configProperties.getConnection().getUsername());
        factoryBean.setConnectionTimeout(Duration.ofSeconds(configProperties.getConnection().getTimeoutSeconds()));
        factoryBean.setDrainTimeout(Duration.ofSeconds(configProperties.getConnection().getDrainTimeoutSeconds()));

        NatsConfigProperties.Tls tls = configProperties.getConnection().getTls();
        if (tls.isEnabled()) {
             TlsConfiguration tlsConf = new TlsConfiguration();
             tlsConf.setEnabled(tls.isEnabled());
             tlsConf.setTrustStore(tls.getTrustStore());
             tlsConf.setTrustStorePassword(tls.getTrustStorePassword());
             tlsConf.setIdentityStore(tls.getIdentityStore());
             tlsConf.setIdentityStorePassword(tls.getIdentityStorePassword());
             factoryBean.setTlsConfiguration(tlsConf);
        }

        NatsConfigProperties.Jwt jwt = configProperties.getConnection().getJwt();
        if (jwt != null) {
            factoryBean.setJwtNKey(configProperties.getConnection().getJwt().getnKey());
            factoryBean.setJwtToken(configProperties.getConnection().getJwt().getToken());
        }
        return factoryBean;
    }

    @ConditionalOnMissingBean
    @Bean
    protected NatsConnectionListener natsConnectionListener() {

        return new NatsConnectionListener();
    }

    @ConditionalOnMissingBean
    @Bean
    protected NatsErrorListener natsErrorListener() {
        return new NatsErrorListener();
    }

    @ConditionalOnProperty(name = "management.health.nats.enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public NatsHealthIndicator natsHealthIndicator() {
        return new NatsHealthIndicator();
    }


    @Primary
    @Bean
    public NatsTemplate natsTemplate(Connection natsConnection, NatsMessageSerDeFactory serDeFactory) {
        return new NatsTemplateImpl(natsConnection, serDeFactory);
    }


    @Bean
    public NatsConsumerManager natsConsumerManager(Connection natsConnection,
                                                     NatsMessageSerDeFactory serDeFactory,
                                                     Optional<NatsMetricsRegistry> natsMetricsRegistry) {
        return new NatsConsumerManagerImpl(
                natsConnection,
                serDeFactory,
                natsMetricsRegistry,
                configProperties.getConnection().getDrainTimeoutSeconds(),
                configProperties.isAutoStartConsumers());
    }

    @Bean
    public NatsPostBeanProcessor natsPostBeanProcessor(NatsConsumerManager natsConsumerManager) {
        return new NatsPostBeanProcessor(natsConsumerManager);
    }

    @ConditionalOnMissingBean
    @Bean
    public ErrorLogger errorLogger() {
        return new ErrorLogger();
    }


    @ConditionalOnMissingBean
    @Bean
    public ConnectionStatusLogger connectionStatusLogger() {
        return new ConnectionStatusLogger();
    }
}
