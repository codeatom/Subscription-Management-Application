package app.manager.subscription.repository;

import app.manager.subscription.model.Subscriber;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriberInMemoryRepo implements SubscriberRepository {

    private final Map<Integer, Subscriber> subscribers = new ConcurrentHashMap<>();

    @Override
    public Subscriber save(Subscriber subscriber) {
        subscribers.put(subscriber.getId(), subscriber);
        return subscriber;
    }

    @Override
    public List<Subscriber> findAll() {
        return List.copyOf(subscribers.values());
    }

    @Override
    public Optional<Subscriber> findById(int id) {
        return Optional.ofNullable(subscribers.get(id));
    }

    @Override
    public Optional<Subscriber> findByEmail(String email) {
        return subscribers.values()
                .stream()
                .filter(s -> s.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean deleteById(int id) {
        return subscribers.remove(id) != null;
    }

    @Override
    public long count() {
        return subscribers.size();
    }
}
