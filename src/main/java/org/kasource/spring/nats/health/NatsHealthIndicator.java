package org.kasource.spring.nats.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import io.nats.client.Connection;


public class NatsHealthIndicator extends AbstractHealthIndicator {

    @Autowired
    private Connection natsConnection;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        builder
                .withDetail("connection", natsConnection.getConnectedUrl())
                .withDetail("servers", natsConnection.getOptions().getServers());
        String connectionName = natsConnection.getOptions().getConnectionName();
        if (connectionName != null) {
            builder.withDetail("name", connectionName);
        }

        builder.withDetail("status", natsConnection.getStatus());
        switch (natsConnection.getStatus()) {
            case CONNECTED:
                builder.up();
                break;
            case CONNECTING:
            case RECONNECTING:
                builder.outOfService();
                break;
            default: // CLOSED, DISCONNECTED
                builder.down();
                break;
        }
    }
}

