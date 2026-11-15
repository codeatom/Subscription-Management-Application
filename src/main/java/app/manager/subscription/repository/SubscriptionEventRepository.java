package app.manager.subscription.repository;

import app.manager.subscription.model.SubscriptionEvent;
import java.util.List;

public interface SubscriptionEventRepository {

    SubscriptionEvent save(SubscriptionEvent event);

    List<SubscriptionEvent> findAll();

    List<SubscriptionEvent> findBySubscriberId(int subscriberId);
}