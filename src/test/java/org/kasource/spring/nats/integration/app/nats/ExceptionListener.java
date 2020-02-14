package org.kasource.spring.nats.integration.app.nats;

import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import org.kasource.spring.nats.event.NatsExceptionEvent;

@Component
public class ExceptionListener {

    private Optional<Exception> exception = Optional.empty();

    @Async
    @EventListener
    public void onException(NatsExceptionEvent event) {
        exception = Optional.ofNullable(event.getException());
    }

    public Optional<Exception> getException() {
        return exception;
    }

    public void clear() {
        exception = Optional.empty();
    }
}


