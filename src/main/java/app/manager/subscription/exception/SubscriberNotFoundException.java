package app.manager.subscription.exception;

public class SubscriberNotFoundException extends RuntimeException {

    public SubscriberNotFoundException(String message) {
        super(message);
    }

    public SubscriberNotFoundException(int subscriberId) {
        super("Subscriber not found with ID: " + subscriberId);
    }
}