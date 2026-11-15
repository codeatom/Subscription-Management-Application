package app.manager.subscription;

import app.manager.subscription.exception.SubscriberNotFoundException;
import app.manager.subscription.functional.SubscriberRules;
import app.manager.subscription.model.Plan;
import app.manager.subscription.model.Subscriber;
import app.manager.subscription.model.SubscriptionEvent;
import app.manager.subscription.processor.SubscriberProcessor;
import app.manager.subscription.repository.SubscriberInMemoryRepo;
import app.manager.subscription.repository.SubscriberRepository;
import app.manager.subscription.repository.SubscriptionEventInMemoryRepo;
import app.manager.subscription.repository.SubscriptionEventRepository;
import app.manager.subscription.service.SubscriptionService;

import java.util.List;

public class Application {
    private final SubscriberRepository subscriberRepository;
    private final SubscriberProcessor subscriberProcessor;
    private final SubscriptionService subscriptionService;

    // CONSTRUCTOR INJECTION
    public Application(SubscriberRepository subscriberRepository,
                       SubscriptionEventRepository eventRepository,
                       SubscriberProcessor subscriberProcessor) {
        this.subscriberRepository = subscriberRepository;
        this.subscriberProcessor = subscriberProcessor;

        this.subscriptionService =
                new SubscriptionService(subscriberRepository, eventRepository);
    }

    // APPLICATION ENTRY POINT
    public static void main(String[] args) {
        SubscriberRepository repository = new SubscriberInMemoryRepo();
        SubscriptionEventRepository eventRepository = new SubscriptionEventInMemoryRepo();

        SubscriberProcessor processor = new SubscriberProcessor();
        Application application = new Application(repository, eventRepository, processor);

        application.run();
    }

    // MAIN APPLICATION FLOW
    public void run() {
        System.out.println("================================");
        System.out.println("       SUBSCRIPTION MANAGER");
        System.out.println("================================");

        registerSubscribers();

        displayAllSubscribers();

        findSubscriber(2);

        filterSubscribers();

        processSubscribers();

        manageSubscriptions();

        displayEvents();

        displayStatistics();

        System.out.println("\nApplication finished.");
    }

    // ==========================================
    // REGISTRATION
    // ==========================================

    private void registerSubscribers() {
        System.out.println("\n--- REGISTERING SUBSCRIBERS ---");

        List<String> emails = List.of(
                "alice@example.com",
                "bob@example.com",
                "charlie@gmail.com",
                "diana@gmail.com",
                "eric@example.com"
        );

        emails.forEach(email -> {Subscriber subscriber = subscriptionService.registerSubscriber(email);

            System.out.println("Registered: " + subscriber.getEmail()
                            + " | ID: " + subscriber.getId()
                            + " | Plan: " + subscriber.getPlan()
                            + " | Status: " + subscriber.getStatus()
            );
        });
    }

    // ==========================================
    // DISPLAY ALL SUBSCRIBERS
    // ==========================================

    private void displayAllSubscribers() {
        System.out.println("\n--- ALL SUBSCRIBERS ---");
        subscriberRepository.findAll().forEach(System.out::println);
    }

    // ==========================================
    // FIND SUBSCRIBER
    // ==========================================

    private void findSubscriber(int id) {
        System.out.println("\n--- FIND SUBSCRIBER ---");

        try {Subscriber subscriber = subscriberRepository.findById(id)
                .orElseThrow(() -> new SubscriberNotFoundException(id));

            System.out.println(subscriber);

        } catch (SubscriberNotFoundException exception) {
            System.err.println(exception.getMessage());
        }
    }

    // ==========================================
    // FILTER SUBSCRIBERS
    // ==========================================

    private void filterSubscribers() {
        System.out.println("\n--- FILTER SUBSCRIBERS ---");

        // Gmail subscribers
        System.out.println("\nGmail subscribers:");
        subscriberProcessor.findSubscribers(subscriberRepository.findAll(), SubscriberRules.subscribersByEmailProvider("gmail.com"))
                .forEach(System.out::println);

        // Example.com subscribers
        System.out.println("\nExample.com subscribers:");

        subscriberProcessor.findSubscribers(subscriberRepository.findAll(), SubscriberRules.exampleDomainSubscribers())
                .forEach(System.out::println);
    }

    // ==========================================
    // PROCESS SUBSCRIBERS
    // ==========================================

    private void processSubscribers() {
        System.out.println("\n--- PROCESS SUBSCRIBERS ---");

        subscriberProcessor.applyToMatching(subscriberRepository.findAll(),
                SubscriberRules.exampleDomainSubscribers(),
                SubscriberRules.printEmail()
        );
    }

    // ==========================================
    // SUBSCRIPTION MANAGEMENT
    // ==========================================

    private void manageSubscriptions() {

        System.out.println("\n--- MANAGING SUBSCRIPTIONS ---");

        // Activate subscriber 1
        subscriptionService.activateSubscription(1);

        System.out.println("Subscriber 1 activated.");

        // Upgrade subscriber 1 to BASIC
        subscriptionService.upgradePlan(1, Plan.BASIC);

        System.out.println("Subscriber 1 upgraded to BASIC.");

        // Renew subscriber 1 for 3 months
        subscriptionService.renewSubscription(1, 3);

        System.out.println("Subscriber 1 renewed for 3 months.");

        // Activate subscriber 2
        subscriptionService.activateSubscription(2);

        System.out.println("Subscriber 2 activated.");

        // Upgrade subscriber 2 to PRO
        subscriptionService.upgradePlan(2, Plan.PRO);

        System.out.println("Subscriber 2 upgraded to PRO.");

        // Cancel subscriber 2
        subscriptionService.cancelSubscription(2);

        System.out.println("Subscriber 2 cancelled.");

        // Display updated subscriptions
        System.out.println("\nUpdated subscribers:");

        subscriberRepository.findAll().forEach(System.out::println);
    }

    // ==========================================
    // DISPLAY EVENTS
    // ==========================================

    private void displayEvents() {
        System.out.println("\n--- SUBSCRIPTION EVENTS ---");
        List<SubscriptionEvent> events = subscriptionService.getEvents();

        if (events.isEmpty()) {
            System.out.println("No events recorded.");
            return;
        }

        events.forEach(System.out::println);
    }

    // ==========================================
    // STATISTICS
    // ==========================================

    private void displayStatistics() {

        System.out.println("\n--- SUBSCRIBER STATISTICS ---");

        List<Subscriber> subscribers = subscriberRepository.findAll();

        System.out.println("Total subscribers: " + subscriberRepository.count());

        System.out.println("Gmail subscribers: " +
                subscriberProcessor.countMatching(subscribers, SubscriberRules.subscribersByEmailProvider("gmail.com")));

        System.out.println(
                "Example.com subscribers: " +
                        subscriberProcessor.countMatching(subscribers, SubscriberRules.exampleDomainSubscribers()));

        System.out.println("Active subscribers: " +
                        subscriberProcessor.countMatching(subscribers, SubscriberRules.activeSubscribers()));

        System.out.println("Pending subscribers: " +
                subscriberProcessor.countMatching(subscribers, SubscriberRules.pendingSubscribers()));

        System.out.println("Premium subscribers: " +
                        subscriberProcessor.countMatching(subscribers, SubscriberRules.premiumSubscribers()));

        System.out.println("\nSubscribers grouped by plan:");

        subscriberProcessor.groupByPlan(subscribers)
                .forEach((plan, members) -> {
                    System.out.println(plan + ": " + members.size() + " subscriber(s)");
                });

        System.out.println("\n================================");
    }
}
