package marketplace.catalog;

import java.math.BigDecimal;

public final class PhysicalProduct extends Product {
    private final BigDecimal weightKg;

    public PhysicalProduct(String id, String name, BigDecimal price, int quantity,
            BigDecimal weightKg) {
        super(id, name, price, quantity);
        if (weightKg == null || weightKg.signum() <= 0) {
            throw new IllegalArgumentException("weight must be positive");
        }
        this.weightKg = weightKg;
    }

    public BigDecimal weightKg() {
        return weightKg;
    }
}
