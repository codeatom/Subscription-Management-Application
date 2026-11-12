package app.manager.subscription.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Subscriber {

    private final int id;
    private String email;
    private Plan plan;
    private SubscriptionStatus status;
    private LocalDateTime expirationDate;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;

    public Subscriber(int id, String email) {
        this(id, email, Plan.FREE);
    }

    public Subscriber(int id, String email, Plan plan) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Subscriber ID must be positive"
            );
        }

        if (email == null || !email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            throw new IllegalArgumentException(
                    "Invalid email address"
            );
        }

        this.id = id;
        this.email = email.trim().toLowerCase();
        this.plan = Objects.requireNonNull(plan);

        this.status = SubscriptionStatus.PENDING;
        this.expirationDate = null;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.version = 0;
    }

    public void updateEmail(String newEmail) {
        if (newEmail == null || !newEmail.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            throw new IllegalArgumentException(
                    "Invalid email address"
            );
        }

        this.email = newEmail.trim().toLowerCase();
        updateTimestamp();
    }

    public void setPlan(Plan plan) {
        this.plan = Objects.requireNonNull(plan);
        updateTimestamp();
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = Objects.requireNonNull(status);
        updateTimestamp();
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
        updateTimestamp();
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Plan getPlan() {
        return plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Subscriber other)) {
            return false;
        }

        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", plan=" + plan +
                ", status=" + status +
                ", expirationDate=" + expirationDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
