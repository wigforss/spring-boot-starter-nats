package org.kasource.spring.nats.integration.app.nats;

import org.kasource.json.schema.JsonSchema;

@JsonSchema(name = "owner",
            version = "1.0",
            location = "/integration/json-schema/owner.schema.json")
public class Owner {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
