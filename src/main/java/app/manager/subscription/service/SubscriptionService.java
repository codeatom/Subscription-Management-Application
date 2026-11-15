
package app.manager.subscription.service;

import app.manager.subscription.exception.InvalidSubscriptionException;
import app.manager.subscription.exception.SubscriberNotFoundException;
import app.manager.subscription.model.*;
import app.manager.subscription.repository.SubscriberRepository;
import app.manager.subscription.repository.SubscriptionEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SubscriptionService {

    private final SubscriberRepository subscriberRepository;
    private final SubscriptionEventRepository eventRepository;
    private final AtomicInteger nextId;

    public SubscriptionService(
            SubscriberRepository subscriberRepository,
            SubscriptionEventRepository eventRepository) {

        this.subscriberRepository =
                Objects.requireNonNull(subscriberRepository);
        this.eventRepository =
                Objects.requireNonNull(eventRepository);

        int maxId = subscriberRepository.findAll()
                .stream()
                .mapToInt(Subscriber::getId)
                .max()
                .orElse(0);

        this.nextId = new AtomicInteger(maxId + 1);
    }

    // REGISTER SUBSCRIBER
    public Subscriber registerSubscriber(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidSubscriptionException("Email cannot be empty");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (subscriberRepository.existsByEmail(normalizedEmail)) {
            throw new InvalidSubscriptionException("Email is already registered: " + normalizedEmail);
        }

        Subscriber subscriber = new Subscriber(nextId.getAndIncrement(), normalizedEmail, Plan.FREE);
        subscriberRepository.save(subscriber);
        recordEvent(subscriber.getId(), SubscriptionEventType.REGISTERED, "Subscriber registered with FREE plan");

        return subscriber;
    }

    // ACTIVATE SUBSCRIPTION
    public void activateSubscription(int subscriberId) {
        Subscriber subscriber = findSubscriber(subscriberId);

        if (subscriber.getStatus() != SubscriptionStatus.PENDING) {
            throw new InvalidSubscriptionException("Only pending subscriptions can be activated");
        }

        subscriber.setStatus(SubscriptionStatus.ACTIVE);
        subscriber.setExpirationDate(LocalDateTime.now().plusMonths(1));
        subscriberRepository.save(subscriber);

        recordEvent(subscriberId, SubscriptionEventType.ACTIVATED, "Subscription activated successfully");
    }

    // CANCEL SUBSCRIPTION
    public void cancelSubscription(int subscriberId) {
        Subscriber subscriber = findSubscriber(subscriberId);

        if (subscriber.getStatus() != SubscriptionStatus.ACTIVE && subscriber.getStatus() != SubscriptionStatus.PENDING) {
            throw new InvalidSubscriptionException("Subscription cannot be cancelled from status: " + subscriber.getStatus());
        }

        subscriber.setStatus(SubscriptionStatus.CANCELLED);
        subscriberRepository.save(subscriber);
        recordEvent(subscriberId, SubscriptionEventType.CANCELLED, "Subscription cancelled");
    }

    // RENEW SUBSCRIPTION
    public void renewSubscription(int subscriberId, int months) {
        if (months <= 0) {
            throw new InvalidSubscriptionException("Renewal period must be greater than zero");
        }

        Subscriber subscriber = findSubscriber(subscriberId);

        if (subscriber.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new InvalidSubscriptionException("Only active subscriptions can be renewed");
        }

        LocalDateTime currentExpiration = subscriber.getExpirationDate();

        LocalDateTime newExpiration;

        if (currentExpiration == null || currentExpiration.isBefore(LocalDateTime.now())) {
            newExpiration = LocalDateTime.now().plusMonths(months);
        } else {
            newExpiration = currentExpiration.plusMonths(months);
        }

        subscriber.setExpirationDate(newExpiration);
        subscriberRepository.save(subscriber);

        recordEvent(subscriberId, SubscriptionEventType.RENEWED, "Subscription renewed for "
                + months + " month(s). New expiration: " + newExpiration);
    }

    // UPGRADE PLAN
    public void upgradePlan(int subscriberId, Plan newPlan) {
        if (newPlan == null) {
            throw new InvalidSubscriptionException("New plan cannot be null");
        }

        Subscriber subscriber = findSubscriber(subscriberId);

        if (subscriber.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new InvalidSubscriptionException("Only active subscriptions can be upgraded");
        }

        Plan currentPlan = subscriber.getPlan();

        if (newPlan.getPriority() <= currentPlan.getPriority()) {
            throw new InvalidSubscriptionException("New plan must have a higher priority than " + currentPlan);
        }

        subscriber.setPlan(newPlan);
        subscriberRepository.save(subscriber);
        recordEvent(subscriberId, SubscriptionEventType.PLAN_UPGRADED, "Plan upgraded from " + currentPlan + " to " + newPlan);
    }

    // EXPIRE SUBSCRIPTIONS
    public void expireSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscriber> subscribers = subscriberRepository.findAll();

        for (Subscriber subscriber : subscribers) {
            if (subscriber.getStatus() == SubscriptionStatus.ACTIVE
                    && subscriber.getExpirationDate() != null && !subscriber.getExpirationDate().isAfter(now)) {
                subscriber.setStatus(SubscriptionStatus.EXPIRED);
                subscriberRepository.save(subscriber);
                recordEvent(subscriber.getId(), SubscriptionEventType.EXPIRED, "Subscription expired on " + subscriber.getExpirationDate());
            }
        }
    }

    // FIND SUBSCRIBER
    private Subscriber findSubscriber(int subscriberId) {
        return subscriberRepository.findById(subscriberId).orElseThrow(() -> new SubscriberNotFoundException(subscriberId));
    }

    // RECORD EVENT
    private void recordEvent(int subscriberId, SubscriptionEventType eventType, String description) {
        SubscriptionEvent event =
                new SubscriptionEvent(subscriberId, eventType, description);

        eventRepository.save(event);
    }

    // GET ALL EVENTS
    public List<SubscriptionEvent> getEvents() {
        return eventRepository.findAll();
    }

    // GET EVENTS FOR ONE SUBSCRIBER
    public List<SubscriptionEvent> getEventsBySubscriber(int subscriberId) {
        return eventRepository.findBySubscriberId(subscriberId);
    }
}
