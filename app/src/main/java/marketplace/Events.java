package marketplace;

import java.math.BigDecimal;
import java.util.Objects;

public final class Events {
    private Events() {
    }

    public enum Type {
        PRICE_CHANGED,
        STOCK_CHANGED,
        CATEGORY_REPORT_READY
    }

    public sealed interface Event permits PriceChanged, StockChanged, CategoryReportReady {
        Type type();
    }

    public record PriceChanged(
            String categoryId,
            String productId,
            BigDecimal oldPrice,
            BigDecimal newPrice
    ) implements Event {
        public PriceChanged {
            requireId(categoryId);
            requireId(productId);
            requireNonNegative(oldPrice);
            requireNonNegative(newPrice);
        }

        @Override
        public Type type() {
            return Type.PRICE_CHANGED;
        }
    }

    public record StockChanged(
            String categoryId,
            String productId,
            int oldQuantity,
            int newQuantity
    ) implements Event {
        public StockChanged {
            requireId(categoryId);
            requireId(productId);
            if (oldQuantity < 0 || newQuantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
        }

        @Override
        public Type type() {
            return Type.STOCK_CHANGED;
        }
    }

    public record CategoryReportReady(
            String categoryId,
            int productCount,
            int totalStock
    ) implements Event {
        public CategoryReportReady {
            requireId(categoryId);
            if (productCount < 0 || totalStock < 0) {
                throw new IllegalArgumentException("Report values cannot be negative");
            }
        }

        @Override
        public Type type() {
            return Type.CATEGORY_REPORT_READY;
        }
    }

    private static void requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be blank");
        }
    }

    private static void requireNonNegative(BigDecimal value) {
        if (Objects.requireNonNull(value).signum() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
}
