package org.kasource.spring.nats.autoconfigure;


import javax.validation.Validator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.kasource.spring.nats.message.validation.BeanValidationValidator;

@ConditionalOnClass(Validator.class)
@ConditionalOnProperty(prefix = "spring.nats", name = "enable-validation")
@Configuration
public class ValidationBeans {

    @ConditionalOnMissingBean
    @Bean
    public BeanValidationValidator beanValidationValidator(Validator validator) {
        return new BeanValidationValidator(validator);
    }
}
