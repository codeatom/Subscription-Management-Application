package app.manager.subscription.functional;

import app.manager.subscription.model.Subscriber;

@FunctionalInterface
public interface SubscriberFilter {

    boolean matches(Subscriber subscriber);
}