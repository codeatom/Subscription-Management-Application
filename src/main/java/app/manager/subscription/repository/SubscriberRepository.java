package app.manager.subscription.repository;

import app.manager.subscription.model.Subscriber;

import java.util.List;
import java.util.Optional;

public interface SubscriberRepository {

    Subscriber save(Subscriber subscriber);

    List<Subscriber> findAll();

    Optional<Subscriber> findById(int id);

    Optional<Subscriber> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean deleteById(int id);

    long count();
}