package org.kasource.spring.nats.integration.app.nats;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.springframework.stereotype.Component;

import org.kasource.spring.nats.annotation.Consumer;

@Component
public class MessageConsumer {

    private List<User> users = new LinkedList<User>();
    private List<Owner> owners = new LinkedList<>();
    private Optional<CountDownLatch> optionalLatch = Optional.empty();

    public void clear() {
        users.clear();
        owners.clear();
    }

    public void setLatch(CountDownLatch latch) {
        optionalLatch = Optional.ofNullable(latch);
    }

    @Consumer(subject = "users")
    public void onUpdatedUser(User user) {
        users.add(user);
        optionalLatch.ifPresent(l -> l.countDown());
    }


    @Consumer(subject = "owners")
    public void onUpdatedOwner(Owner owner) {
        owners.add(owner);
        optionalLatch.ifPresent(l -> l.countDown());
    }

    public List<User> getUsers() {
        return users;
    }


    public List<Owner> getOwners() {
        return owners;
    }
}
