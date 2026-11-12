package app.manager.subscription.model;

public enum Plan {

    FREE(0, 0, 1),
    BASIC(9, 100, 2),
    PRO(19, 1000, 3);

    private final double monthlyPrice;
    private final int usageLimit;
    private final int priority;

    Plan(double monthlyPrice, int usageLimit, int priority) {
        this.monthlyPrice = monthlyPrice;
        this.usageLimit = usageLimit;
        this.priority = priority;
    }

    public double getMonthlyPrice() {
        return monthlyPrice;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public int getPriority() {
        return priority;
    }
}