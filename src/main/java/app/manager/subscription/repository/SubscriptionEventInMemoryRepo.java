package app.manager.subscription.repository;

import app.manager.subscription.model.SubscriptionEvent;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionEventInMemoryRepo
        implements SubscriptionEventRepository {

    private final List<SubscriptionEvent> events = new ArrayList<>();

    @Override
    public SubscriptionEvent save(SubscriptionEvent event) {
        events.add(event);
        return event;
    }

    @Override
    public List<SubscriptionEvent> findAll() {
        return List.copyOf(events);
    }

    @Override
    public List<SubscriptionEvent> findBySubscriberId(int subscriberId) {
        return events.stream()
                .filter(event -> event.getSubscriberId() == subscriberId)
                .toList();
    }
}