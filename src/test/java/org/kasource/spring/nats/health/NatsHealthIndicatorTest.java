package org.kasource.spring.nats.health;

import java.net.URI;
import java.util.List;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import io.nats.client.Connection;
import io.nats.client.Options;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NatsHealthIndicatorTest {

    @Mock
    private Connection natsConnection;

    @Mock
    private Options options;

    @Mock
    private List<URI> serverList;

    @Mock
    private RuntimeException runtimeException;

    @InjectMocks
    private NatsHealthIndicator healthIndicator;

    @Test
    public void healthy() {
        String url = "url";
        String connectionName = "connectionName";
        Connection.Status status = Connection.Status.CONNECTED;

        when(natsConnection.getConnectedUrl()).thenReturn(url);
        when(natsConnection.getOptions()).thenReturn(options);
        when(options.getServers()).thenReturn(serverList);
        when(options.getConnectionName()).thenReturn(connectionName);
        when(natsConnection.getStatus()).thenReturn(status);

        Health health = healthIndicator.health();

        assertThat(health.getStatus(), is(Status.UP));
        assertThat(health.getDetails(), hasEntry("status", status));
        assertThat(health.getDetails(), hasEntry("connection", url));
        assertThat(health.getDetails(), hasEntry("servers", serverList));
        assertThat(health.getDetails(), hasEntry("name", connectionName));
    }

    @Test
    public void unhealthyDisconnected() {
        String url = "url";
        String connectionName = "connectionName";
        Connection.Status status = Connection.Status.DISCONNECTED;

        when(natsConnection.getConnectedUrl()).thenReturn(url);
        when(natsConnection.getOptions()).thenReturn(options);
        when(options.getServers()).thenReturn(serverList);
        when(options.getConnectionName()).thenReturn(connectionName);
        when(natsConnection.getStatus()).thenReturn(status);

        Health health = healthIndicator.health();

        assertThat(health.getStatus(), is(Status.DOWN));
        assertThat(health.getDetails(), hasEntry("status", status));
        assertThat(health.getDetails(), hasEntry("connection", url));
        assertThat(health.getDetails(), hasEntry("servers", serverList));
        assertThat(health.getDetails(), hasEntry("name", connectionName));
    }

    @Test
    public void unhealthyClosed() {
        String url = "url";
        String connectionName = "connectionName";
        Connection.Status status = Connection.Status.CLOSED;

        when(natsConnection.getConnectedUrl()).thenReturn(url);
        when(natsConnection.getOptions()).thenReturn(options);
        when(options.getServers()).thenReturn(serverList);
        when(options.getConnectionName()).thenReturn(connectionName);
        when(natsConnection.getStatus()).thenReturn(status);

        Health health = healthIndicator.health();

        assertThat(health.getStatus(), is(Status.DOWN));
        assertThat(health.getDetails(), hasEntry("status", status));
        assertThat(health.getDetails(), hasEntry("connection", url));
        assertThat(health.getDetails(), hasEntry("servers", serverList));
        assertThat(health.getDetails(), hasEntry("name", connectionName));
    }

    @Test
    public void outOfServiceConnecting() {
        String url = "url";
        Connection.Status status = Connection.Status.CONNECTING;

        when(natsConnection.getConnectedUrl()).thenReturn(url);
        when(natsConnection.getOptions()).thenReturn(options);
        when(options.getServers()).thenReturn(serverList);

        when(natsConnection.getStatus()).thenReturn(status);

        Health health = healthIndicator.health();

        assertThat(health.getStatus(), is(Status.OUT_OF_SERVICE));
        assertThat(health.getDetails(), hasEntry("status", status));
        assertThat(health.getDetails(), hasEntry("connection", url));
        assertThat(health.getDetails(), hasEntry("servers", serverList));
        assertThat(health.getDetails(), not(hasKey("name")));
    }

    @Test
    public void outOfServiceReconnecting() {
        String url = "url";
        Connection.Status status = Connection.Status.RECONNECTING;

        when(natsConnection.getConnectedUrl()).thenReturn(url);
        when(natsConnection.getOptions()).thenReturn(options);
        when(options.getServers()).thenReturn(serverList);

        when(natsConnection.getStatus()).thenReturn(status);

        Health health = healthIndicator.health();

        assertThat(health.getStatus(), is(Status.OUT_OF_SERVICE));
        assertThat(health.getDetails(), hasEntry("status", status));
        assertThat(health.getDetails(), hasEntry("connection", url));
        assertThat(health.getDetails(), hasEntry("servers", serverList));
        assertThat(health.getDetails(), not(hasKey("name")));
    }


}
