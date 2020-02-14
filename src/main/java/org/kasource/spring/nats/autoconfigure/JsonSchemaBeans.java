package org.kasource.spring.nats.autoconfigure;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.kasource.json.schema.JsonSchemaScanner;
import org.kasource.json.schema.registry.JsonSchemaRegistry;
import org.kasource.spring.nats.config.NatsConfigProperties;
import org.kasource.spring.nats.message.validation.JsonSchemaValidator;

import com.fasterxml.jackson.databind.ObjectMapper;


@ConditionalOnProperty(prefix = "spring.nats", name = "jackson.json-schema.enable-validation", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({JsonSchemaRegistry.class, ObjectMapper.class})
@Configuration
public class JsonSchemaBeans {
    @Autowired
    private NatsConfigProperties configProperties;

    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.nats", name = "jackson.json-schema.scan-packages")
    @Bean
    public JsonSchemaValidator jsonSchemaRepository(ObjectMapper objectMapper) {
        List<String> packages = configProperties.getJackson().getJsonSchema().getScanPackages();
        // this is an @NotEmpty property, so it should have at least one element
        String firstPackage = packages.remove(0);
        String[] additionalPackages = packages.stream().toArray(String[]::new);
        JsonSchemaRegistry repository = new JsonSchemaScanner(objectMapper, true)
                .scan(firstPackage, additionalPackages);
        return new JsonSchemaValidator(repository);
    }
}
