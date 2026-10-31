package marketplace.catalog;

import java.math.BigDecimal;

public final class ServiceProduct extends Product {
    private final int durationMinutes;

    public ServiceProduct(String id, String name, BigDecimal price, int quantity,
            int durationMinutes) {
        super(id, name, price, quantity);
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("duration must be positive");
        }
        this.durationMinutes = durationMinutes;
    }

    public int durationMinutes() {
        return durationMinutes;
    }
}
