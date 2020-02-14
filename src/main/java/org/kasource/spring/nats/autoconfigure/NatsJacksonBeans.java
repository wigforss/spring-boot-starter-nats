package org.kasource.spring.nats.autoconfigure;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.kasource.spring.nats.message.serde.NatsMessageSerDeFactory;
import org.kasource.spring.nats.message.serde.json.NatsJacksonMessageSerDeFactory;
import org.kasource.spring.nats.message.validation.JsonSchemaValidator;
import org.kasource.spring.nats.message.validation.MessageObjectValidator;

import com.fasterxml.jackson.databind.ObjectMapper;

@ConditionalOnProperty(prefix = "spring.nats", name = "ser-de-type", havingValue = "JACKSON", matchIfMissing = true)
@ConditionalOnClass(ObjectMapper.class)
@Configuration
public class NatsJacksonBeans {

    @Autowired
    private Optional<JsonSchemaValidator> jsonSchemaValidator;

    @Autowired
    private Optional<MessageObjectValidator> validator;

    @ConditionalOnMissingBean
    @Bean
    public NatsMessageSerDeFactory jacksonMessageSerDeFactory(ObjectMapper objectMapper) {
        NatsJacksonMessageSerDeFactory serDeFactory = new NatsJacksonMessageSerDeFactory();
        serDeFactory.setObjectMapper(objectMapper);
        jsonSchemaValidator.ifPresent(v -> serDeFactory.setSchemaValidator(v));
        validator.ifPresent(v -> serDeFactory.setValidator(v));
        return serDeFactory;
    }




}
