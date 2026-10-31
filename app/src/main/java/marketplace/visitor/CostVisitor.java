package marketplace.visitor;

import java.math.BigDecimal;
import java.util.Objects;
import marketplace.catalog.Catalog;
import marketplace.catalog.Category;
import marketplace.catalog.DigitalProduct;
import marketplace.catalog.PhysicalProduct;
import marketplace.catalog.Product;
import marketplace.catalog.ServiceProduct;

public final class CostVisitor implements Visitor<BigDecimal> {
    public enum Mode {
        TAX,
        SHIPPING
    }

    private static final BigDecimal PHYSICAL_TAX = new BigDecimal("0.12");
    private static final BigDecimal DIGITAL_TAX = new BigDecimal("0.10");
    private static final BigDecimal SERVICE_TAX = new BigDecimal("0.05");
    private static final BigDecimal SHIPPING_PER_KG = new BigDecimal("2.00");

    private final Mode mode;

    public CostVisitor(Mode mode) {
        this.mode = Objects.requireNonNull(mode);
    }

    @Override
    public BigDecimal visit(Catalog catalog) {
        var total = BigDecimal.ZERO;
        for (var category : catalog.categories()) {
            total = total.add(category.accept(this));
        }
        return total;
    }

    @Override
    public BigDecimal visit(Category category) {
        var total = BigDecimal.ZERO;
        for (var subcategory : category.subcategories()) {
            total = total.add(subcategory.accept(this));
        }
        for (var product : category.products()) {
            total = total.add(product.accept(this));
        }
        return total;
    }

    @Override
    public BigDecimal visit(PhysicalProduct product) {
        if (mode == Mode.TAX) {
            return tax(product, PHYSICAL_TAX);
        }
        return product.weightKg()
                .multiply(SHIPPING_PER_KG)
                .multiply(BigDecimal.valueOf(product.quantity()));
    }

    @Override
    public BigDecimal visit(DigitalProduct product) {
        return mode == Mode.TAX ? tax(product, DIGITAL_TAX) : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal visit(ServiceProduct product) {
        return mode == Mode.TAX ? tax(product, SERVICE_TAX) : BigDecimal.ZERO;
    }

    private static BigDecimal tax(Product product, BigDecimal rate) {
        return product.price()
                .multiply(BigDecimal.valueOf(product.quantity()))
                .multiply(rate);
    }
}
