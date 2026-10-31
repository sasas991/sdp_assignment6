package marketplace.catalog;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import marketplace.visitor.Visitor;

public abstract class Product implements CatalogElement {
    private final String id;
    private final String name;
    private final AtomicReference<BigDecimal> price;
    private final AtomicInteger quantity;

    protected Product(String id, String name, BigDecimal price, int quantity) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.price = new AtomicReference<>(requirePrice(price));
        this.quantity = new AtomicInteger(requireQuantity(quantity));
    }

    @Override
    public abstract <R> R accept(Visitor<R> visitor);

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public BigDecimal price() {
        return price.get();
    }

    public int quantity() {
        return quantity.get();
    }

    public BigDecimal updatePrice(BigDecimal newPrice) {
        return price.getAndSet(requirePrice(newPrice));
    }

    public int updateQuantity(int newQuantity) {
        return quantity.getAndSet(requireQuantity(newQuantity));
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }

    private static BigDecimal requirePrice(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }
        return value;
    }

    private static int requireQuantity(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        return value;
    }
}
