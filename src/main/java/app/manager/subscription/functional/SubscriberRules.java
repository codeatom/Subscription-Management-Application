package app.manager.subscription.functional;

import app.manager.subscription.model.Plan;
import app.manager.subscription.model.SubscriptionStatus;

public final class SubscriberRules {

    private SubscriberRules() {
        // Prevent instantiation
    }

    // FILTER RULES

    public static SubscriberFilter subscribersByEmailProvider(String provider) {
        return subscriber ->
                subscriber.getEmail().endsWith("@" + provider.toLowerCase());
    }

    public static SubscriberFilter exampleDomainSubscribers() {
        return subscriber -> subscriber.getEmail().endsWith("@example.com");
    }

    public static SubscriberFilter activeSubscribers() {
        return subscriber -> subscriber.getStatus() == SubscriptionStatus.ACTIVE;
    }

    public static SubscriberFilter pendingSubscribers() {
        return subscriber -> subscriber.getStatus() == SubscriptionStatus.PENDING;
    }

    public static SubscriberFilter subscribersOnPlan(Plan plan) {
        return subscriber -> subscriber.getPlan() == plan;
    }

    public static SubscriberFilter premiumSubscribers() {
        return subscriber -> subscriber.getPlan().getPriority() > Plan.FREE.getPriority();
    }


    // ACTION RULES

    public static SubscriberAction printSubscriber() {
        return subscriber -> System.out.println(subscriber);
    }

    public static SubscriberAction printEmail() {
        return subscriber -> System.out.println(subscriber.getEmail());
    }

    public static SubscriberAction printStatus() {
        return subscriber -> System.out.println(subscriber.getEmail() + " - " + subscriber.getStatus());
    }
}
