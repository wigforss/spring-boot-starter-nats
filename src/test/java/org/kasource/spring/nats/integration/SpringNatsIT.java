package org.kasource.spring.nats.integration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.search.Search;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.nats.client.Connection;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.kasource.json.schema.validation.InvalidJsonException;

import org.kasource.spring.nats.integration.app.nats.ExceptionListener;
import org.kasource.spring.nats.integration.app.nats.MessageConsumer;
import org.kasource.spring.nats.integration.app.nats.User;
import org.kasource.spring.nats.integration.app.Application;
import org.kasource.spring.nats.integration.app.nats.MessagePublisher;
import org.kasource.spring.nats.integration.app.nats.Owner;
import org.kasource.spring.nats.metrics.NatsMetricsRegistry;

@SpringBootTest(classes = Application.class,
                webEnvironment = SpringBootTest.WebEnvironment.NONE,
                properties = {"spring.nats.connection.urls=nats://localhost:${NATS_PORT}",
                              "spring.nats.enable-validation=true",
                              "spring.nats.jackson.json-schema.scan-packages=org.kasource.spring.nats.integration.app"})

@RunWith(SpringRunner.class)
public class SpringNatsIT {
    private static Integer NATS_PORT = SocketUtils.findAvailableTcpPort(10000);
    private static Integer NATS_MONITOR_PORT = SocketUtils.findAvailableTcpPort(10000);

    @ClassRule
    public static DockerComposeContainer dockerComposeContainer =
            new DockerComposeContainer(new File("src/test/resources/integration/docker/docker-compose.yml"))
                    .withEnv("NATS_PORT", NATS_PORT.toString())
                    .withEnv("NATS_MONITOR_PORT", NATS_MONITOR_PORT.toString())
                    .waitingFor("nats", Wait.forListeningPort())
                    .waitingFor("nats", Wait.forLogMessage(".*Server is ready.*\\n", 1));


    @Autowired
    private MessagePublisher messagePublisher;

    @Autowired
    private MessageConsumer messageConsumer;

    @Autowired
    private Connection natsConnection;

    @Autowired
    private ExceptionListener exceptionListener;

    @Autowired
    private SimpleMeterRegistry meterRegistry;

    @Autowired
    private NatsMetricsRegistry natsMetricsRegistry;

    private ObjectMapper objectMapper = new ObjectMapper();

    private CountDownLatch latch;

    private double deliveredUserMessages;
    private double deliveredOwnerMessages;
    private double invalidDataExceptionCount;
    private double invalidObjectExceptionCount;

    @BeforeClass
    public static void setupPorts() {
        System.setProperty("NATS_PORT", NATS_PORT.toString());
        System.setProperty("NATS_MONITOR_PORT", NATS_MONITOR_PORT.toString());
        System.out.println("\n\n##################################");
        System.out.println("Starting NATS with port " + NATS_PORT + " and monitoring port " + NATS_MONITOR_PORT);
        System.out.println("##################################\n\n");
    }

    @Before
    public void clear() {
        messageConsumer.clear();
        exceptionListener.clear();
        latch = new CountDownLatch(1);

        // Read metrics
        deliveredUserMessages = getGaugeValue("nats.subscription.delivered.count", "subject", "users");
        deliveredOwnerMessages = getGaugeValue("nats.subscription.delivered.count", "subject", "owners");
        invalidDataExceptionCount = getCounterValue("nats.connection.exception.count", "exception", InvalidJsonException.class.getName());
        invalidObjectExceptionCount = getCounterValue("nats.connection.exception.count", "exception", ConstraintViolationException.class.getName());
    }

    @Test
    public void publishSubscribeUser() throws InterruptedException {

        messageConsumer.setLatch(latch);

        String name = "Rikard The Long Name";
        User user = new User();
        user.setName(name);

        messagePublisher.publishUser(user);

        boolean received = latch.await(5, TimeUnit.SECONDS);

        assertThat("Timed out", received, is(true));

        User receivedUser = messageConsumer.getUsers().get(0);

        assertThat(receivedUser, is(notNullValue()));
        assertThat(receivedUser.getName(), is(equalTo(name)));
        assertThat(getGaugeValue("nats.subscription.delivered.count", "subject", "users"), is(deliveredUserMessages + 1D));
    }


    @Test(expected = ConstraintViolationException.class)
    public void publishInvalidObject() throws InterruptedException {
        String name = "Rikard";
        User user = new User();
        user.setName(name);

        try {
            messagePublisher.publishUser(user);
        } catch (ConstraintViolationException e) {
            assertThat(getGaugeValue("nats.subscription.delivered.count", "subject", "users"), is(deliveredUserMessages));
            throw e;
        }
    }

    @Test
    public void invalidObjectOnConsumption() throws InterruptedException, JsonProcessingException {
        messageConsumer.setLatch(latch);

        String name = "Rikard";
        User user = new User();
        user.setName(name);

        natsConnection.publish("users", objectMapper.writer().writeValueAsString(user).getBytes(StandardCharsets.UTF_8));

        boolean received = latch.await(1, TimeUnit.SECONDS);

        // Should timeout
        assertThat(received, is(false));
        Optional<Exception> optionalException = exceptionListener.getException();

        assertThat(optionalException.isPresent(), is(true));
        assertThat(optionalException.get(), is(instanceOf(ConstraintViolationException.class)));
        assertThat(getGaugeValue("nats.subscription.delivered.count", "subject", "users"), is(deliveredUserMessages + 1D));
        assertThat(getCounterValue("nats.connection.exception.count", "exception", ConstraintViolationException.class.getName()), is(invalidObjectExceptionCount + 1D));

    }


    @Test
    public void publishSubscribeOwner() throws InterruptedException {
        messageConsumer.setLatch(latch);

        String name = "Rikard The Long Name";
        Owner owner = new Owner();
        owner.setName(name);

        messagePublisher.publishOwner(owner);

        boolean received = latch.await(5, TimeUnit.SECONDS);

        assertThat("Timed out", received, is(true));

        Owner receivedOwner = messageConsumer.getOwners().get(0);

        assertThat(receivedOwner, is(notNullValue()));
        assertThat(receivedOwner.getName(), is(equalTo(name)));

        assertThat(getGaugeValue("nats.subscription.delivered.count", "subject", "owners"), is(deliveredOwnerMessages + 1D));
    }

    @Test(expected = InvalidJsonException.class)
    public void publishInvalidData()  {
        String name = "Rikard";
        Owner owner = new Owner();
        owner.setName(name);

        try {
            messagePublisher.publishOwner(owner);
        } catch (ConstraintViolationException e) {
            assertThat(getGaugeValue("nats.subscription.delivered.count", "subject", "owners"), is(deliveredOwnerMessages));
            throw e;
        }
    }

    @Test
    public void invalidDataOnConsumption() throws InterruptedException, JsonProcessingException {
        messageConsumer.setLatch(latch);

        String name = "Rikard";
        Owner owner = new Owner();
        owner.setName(name);

        natsConnection.publish("owners", objectMapper.writer().writeValueAsString(owner).getBytes(StandardCharsets.UTF_8));

        boolean received = latch.await(1, TimeUnit.SECONDS);

        // Should timeout
        assertThat(received, is(false));
        Optional<Exception> optionalException = exceptionListener.getException();

        assertThat(optionalException.isPresent(), is(true));
        assertThat(optionalException.get(), is(instanceOf(InvalidJsonException.class)));
        assertThat(getGaugeValue("nats.subscription.delivered.count", "subject", "owners"), is(deliveredOwnerMessages + 1D));
        assertThat(getCounterValue("nats.connection.exception.count", "exception", InvalidJsonException.class.getName()), is(invalidDataExceptionCount + 1D));

    }


    private double getGaugeValue(String name, String tagKey, String tagValue) {
        Optional<Search> metric = getMetricsValueByTag(name, tagKey, tagValue);
        if (metric.isPresent()) {
            return metric.get().gauge().value();
        }
        return 0D;
    }

    private double getCounterValue(String name, String tagKey, String tagValue) {
        Optional<Search> metric = getMetricsValueByTag(name, tagKey, tagValue);
        if (metric.isPresent()) {
            Counter counter = metric.get().counter();
            if (counter != null) {
                return counter.count();
            }
        }
        return 0D;
    }

    private Optional<Search> getMetricsValueByTag(String name, String tagKey, String tagValue) {
        Search metric = meterRegistry.find(name);
        return Optional.ofNullable(metric.tag(tagKey, tagValue));
    }
}
