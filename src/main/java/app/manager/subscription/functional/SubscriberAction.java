package app.manager.subscription.functional;

import app.manager.subscription.model.Subscriber;

@FunctionalInterface
public interface SubscriberAction {

    void run(Subscriber subscriber);
}