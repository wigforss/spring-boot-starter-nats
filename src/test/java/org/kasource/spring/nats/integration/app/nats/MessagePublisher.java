package org.kasource.spring.nats.integration.app.nats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.kasource.spring.nats.NatsTemplate;

@Component
public class MessagePublisher {

    @Autowired
    private NatsTemplate natsTemplate;

    public void publishUser(User user) {
        natsTemplate.publish(user, "users");
    }

    public void publishOwner(Owner owner) {
        natsTemplate.publish(owner, "owners");
    }
}
