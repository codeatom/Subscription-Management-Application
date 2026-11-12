package app.manager.subscription;

import app.manager.subscription.exception.InvalidSubscriptionException;
import app.manager.subscription.exception.SubscriberNotFoundException;
import app.manager.subscription.model.Plan;
import app.manager.subscription.model.Subscriber;
import app.manager.subscription.model.SubscriptionEvent;
import app.manager.subscription.model.SubscriptionStatus;
import app.manager.subscription.repository.SubscriberInMemoryRepo;
import app.manager.subscription.repository.SubscriberRepository;

import app.manager.subscription.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionServiceTest {

    private SubscriberRepository repository;
    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        repository = new SubscriberInMemoryRepo();
        service = new SubscriptionService(repository);
    }

    // ==========================================
    // REGISTRATION TESTS
    // ==========================================

    @Test
    void shouldRegisterSubscriberSuccessfully() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        assertNotNull(subscriber);
        assertEquals(1, subscriber.getId());
        assertEquals("alice@example.com", subscriber.getEmail());
        assertEquals(Plan.FREE, subscriber.getPlan());
        assertEquals(SubscriptionStatus.PENDING, subscriber.getStatus());

        assertTrue(repository.findById(1).isPresent());
    }

    @Test
    void shouldNormalizeEmailWhenRegistering() {
        Subscriber subscriber = service.registerSubscriber(" ALICE@EXAMPLE.COM ");

        assertEquals(
                "alice@example.com",
                subscriber.getEmail()
        );
    }

    @Test
    void shouldRejectDuplicateEmail() {
        service.registerSubscriber("alice@example.com");

        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.registerSubscriber("ALICE@example.com")
        );

        assertEquals(1, repository.count());
    }

    @Test
    void shouldRejectNullEmail() {
        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.registerSubscriber(null)
        );
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.registerSubscriber(" ")
        );
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerSubscriber("invalid-email")
        );
    }

    // ==========================================
    // ACTIVATION TESTS
    // ==========================================

    @Test
    void shouldActivateSubscription() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        Subscriber updated = repository.findById(subscriber.getId()).orElseThrow();

        assertEquals(
                SubscriptionStatus.ACTIVE,
                updated.getStatus()
        );

        assertNotNull(updated.getExpirationDate());

        assertTrue(updated.getExpirationDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void shouldRejectActivationOfAlreadyActiveSubscription() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.activateSubscription(subscriber.getId())
        );
    }

    @Test
    void shouldThrowWhenActivatingNonexistentSubscriber() {
        assertThrows(
                SubscriberNotFoundException.class,
                () -> service.activateSubscription(999)
        );
    }

    // ==========================================
    // CANCELLATION TESTS
    // ==========================================

    @Test
    void shouldCancelActiveSubscription() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());
        service.cancelSubscription(subscriber.getId());

        Subscriber updated = repository.findById(subscriber.getId()).orElseThrow();

        assertEquals(
                SubscriptionStatus.CANCELLED,
                updated.getStatus()
        );
    }

    @Test
    void shouldCancelPendingSubscription() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.cancelSubscription(subscriber.getId());

        assertEquals(
                SubscriptionStatus.CANCELLED,
                repository.findById(subscriber.getId())
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void shouldRejectCancellationOfCancelledSubscription() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.cancelSubscription(subscriber.getId());

        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.cancelSubscription(subscriber.getId())
        );
    }

    // ==========================================
    // RENEWAL TESTS
    // ==========================================

    @Test
    void shouldRenewActiveSubscription() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        LocalDateTime oldExpiration = repository.findById(subscriber.getId())
                        .orElseThrow()
                        .getExpirationDate();

        service.renewSubscription(subscriber.getId(), 3);

        LocalDateTime newExpiration = repository.findById(subscriber.getId())
                        .orElseThrow()
                        .getExpirationDate();

        assertEquals(
                oldExpiration.plusMonths(3),
                newExpiration
        );
    }

    @Test
    void shouldRejectZeroRenewalPeriod() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.renewSubscription(subscriber.getId(), 0)
        );
    }

    @Test
    void shouldRejectNegativeRenewalPeriod() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.renewSubscription(subscriber.getId(), -2)
        );
    }

    @Test
    void shouldRejectRenewalOfPendingSubscription() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.renewSubscription(subscriber.getId(), 2)
        );
    }

    // ==========================================
    // PLAN UPGRADE TESTS
    // ==========================================

    @Test
    void shouldUpgradePlanSuccessfully() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        service.upgradePlan(subscriber.getId(), Plan.BASIC);

        Subscriber updated = repository.findById(subscriber.getId()).orElseThrow();

        assertEquals(
                Plan.BASIC,
                updated.getPlan()
        );
    }

    @Test
    void shouldUpgradeFromBasicToPro() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        service.upgradePlan(subscriber.getId(), Plan.BASIC);
        service.upgradePlan(subscriber.getId(), Plan.PRO);

        assertEquals(
                Plan.PRO,
                repository.findById(subscriber.getId())
                        .orElseThrow()
                        .getPlan()
        );
    }

    @Test
    void shouldRejectDowngrade() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        service.upgradePlan(subscriber.getId(), Plan.PRO);

        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.upgradePlan(subscriber.getId(), Plan.BASIC)
        );
    }

    @Test
    void shouldRejectUpgradeToSamePlan() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.upgradePlan(subscriber.getId(), Plan.FREE)
        );
    }

    @Test
    void shouldRejectNullPlan() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        assertThrows(
                InvalidSubscriptionException.class,
                () -> service.upgradePlan(subscriber.getId(), null)
        );
    }

    // ==========================================
    // EXPIRATION TESTS
    // ==========================================

    @Test
    void shouldExpireSubscriptionPastExpirationDate() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        subscriber = repository.findById(subscriber.getId()).orElseThrow();

        subscriber.setExpirationDate(LocalDateTime.now().minusDays(1));

        repository.save(subscriber);

        service.expireSubscriptions();

        Subscriber expired = repository.findById(subscriber.getId()).orElseThrow();

        assertEquals(
                SubscriptionStatus.EXPIRED,
                expired.getStatus()
        );
    }

    @Test
    void shouldNotExpireSubscriptionWithFutureExpirationDate() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        service.expireSubscriptions();

        assertEquals(
                SubscriptionStatus.ACTIVE,
                repository.findById(subscriber.getId())
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void shouldNotExpirePendingSubscription() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.expireSubscriptions();

        assertEquals(
                SubscriptionStatus.PENDING,
                repository.findById(subscriber.getId())
                        .orElseThrow()
                        .getStatus()
        );
    }

    // ==========================================
    // EVENT TESTS
    // ==========================================

    @Test
    void shouldRecordRegistrationEvent() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        List<SubscriptionEvent> events = service.getEvents();

        assertEquals(1, events.size());

        assertEquals(
                subscriber.getId(),
                events.get(0).getSubscriberId()
        );

        assertEquals(
                "REGISTERED",
                events.get(0).getEventType()
        );
    }

    @Test
    void shouldRecordAllSubscriptionEvents() {
        Subscriber subscriber = service.registerSubscriber("alice@example.com");

        service.activateSubscription(subscriber.getId());

        service.upgradePlan(subscriber.getId(), Plan.BASIC);

        service.renewSubscription(subscriber.getId(), 2);

        service.cancelSubscription(subscriber.getId());

        List<SubscriptionEvent> events = service.getEvents();

        assertEquals(5, events.size());

        assertEquals("REGISTERED", events.get(0).getEventType());
        assertEquals("ACTIVATED", events.get(1).getEventType());
        assertEquals("PLAN_UPGRADED", events.get(2).getEventType());
        assertEquals("RENEWED", events.get(3).getEventType());
        assertEquals("CANCELLED", events.get(4).getEventType());
    }

    @Test
    void shouldReturnEventsForSpecificSubscriber() {
        Subscriber first = service.registerSubscriber("alice@example.com");

        Subscriber second =
                service.registerSubscriber("bob@example.com");

        service.activateSubscription(first.getId());
        service.activateSubscription(second.getId());

        List<SubscriptionEvent> events =
                service.getEventsBySubscriber(first.getId());

        assertEquals(2, events.size());

        assertTrue(
                events.stream().allMatch(
                        event -> event.getSubscriberId() == first.getId()
                )
        );
    }

    @Test
    void shouldReturnImmutableEventList() {
        service.registerSubscriber("alice@example.com");

        List<SubscriptionEvent> events = service.getEvents();

        assertThrows(
                UnsupportedOperationException.class,
                events::clear
        );
    }
}

