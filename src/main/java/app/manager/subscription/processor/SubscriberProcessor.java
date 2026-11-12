package app.manager.subscription.processor;

import app.manager.subscription.functional.SubscriberAction;
import app.manager.subscription.functional.SubscriberFilter;
import app.manager.subscription.model.Plan;
import app.manager.subscription.model.Subscriber;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SubscriberProcessor {

    public List<Subscriber> findSubscribers(List<Subscriber> list, SubscriberFilter filter) {
        return list.stream()
                .filter(filter::matches)
                .toList();
    }

    public List<Subscriber> applyToMatching(List<Subscriber> list, SubscriberFilter filter, SubscriberAction action) {
        List<Subscriber> matching = findSubscribers(list, filter);

        matching.forEach(action::run);

        return matching;
    }

    public long countMatching(List<Subscriber> list, SubscriberFilter filter) {
        return list.stream()
                .filter(filter::matches)
                .count();
    }

    public Map<Plan, List<Subscriber>> groupByPlan(List<Subscriber> list) {
        return list.stream().collect(Collectors.groupingBy(Subscriber::getPlan));
    }
}