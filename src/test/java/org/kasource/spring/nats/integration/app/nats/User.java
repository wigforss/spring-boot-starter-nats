package org.kasource.spring.nats.integration.app.nats;

import org.springframework.validation.annotation.Validated;

import org.hibernate.validator.constraints.Length;

@Validated
public class User {

    @Length(min = 10)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
