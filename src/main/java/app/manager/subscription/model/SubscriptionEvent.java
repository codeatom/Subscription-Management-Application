package app.manager.subscription.model;

import java.time.LocalDateTime;

public class SubscriptionEvent {

    private final int subscriberId;
    private final SubscriptionEventType eventType;
    private final String description;
    private final LocalDateTime timestamp;

    public SubscriptionEvent(
            int subscriberId,
            SubscriptionEventType eventType,
            String description) {

        this.subscriberId = subscriberId;
        this.eventType = eventType;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public SubscriptionEventType getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "SubscriptionEvent{" +
                "subscriberId=" + subscriberId +
                ", eventType='" + eventType + '\'' +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
