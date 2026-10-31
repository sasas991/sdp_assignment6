package marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import marketplace.catalog.Catalog;
import marketplace.catalog.Category;
import marketplace.catalog.DigitalProduct;
import marketplace.catalog.GiftCard;
import marketplace.catalog.PhysicalProduct;
import marketplace.catalog.ServiceProduct;
import marketplace.visitor.CostVisitor;
import marketplace.visitor.StatsVisitor;
import org.junit.jupiter.api.Test;

class VisitorTest {
    @Test
    void calculatesTaxAcrossFourProductTypes() {
        var actual = catalog().accept(new CostVisitor(CostVisitor.Mode.TAX));

        assertEquals(0, new BigDecimal("43.00").compareTo(actual));
    }

    @Test
    void chargesShippingOnlyForPhysicalProducts() {
        var actual = catalog().accept(new CostVisitor(CostVisitor.Mode.SHIPPING));

        assertEquals(0, new BigDecimal("6.00").compareTo(actual));
    }

    @Test
    void accumulatesProductCountAndStockThroughNestedCategories() {
        var actual = catalog().accept(new StatsVisitor());

        assertEquals(new StatsVisitor.Stats(4, 10), actual);
    }

    private static Catalog catalog() {
        var physical = new PhysicalProduct("p1", "Laptop", new BigDecimal("100"),
                2, new BigDecimal("1.5"));
        var digital = new DigitalProduct("p2", "Book", new BigDecimal("50"),
                3, 1024);
        var service = new ServiceProduct("p3", "Setup", new BigDecimal("80"),
                1, 60);
        var giftCard = new GiftCard("p4", "Gift", new BigDecimal("25"), 4);
        var devices = new Category("c2", "Devices", List.of(), List.of(physical));
        var downloads = new Category("c3", "Downloads", List.of(),
                List.of(digital, service));
        var marketplace = new Category("c1", "Marketplace",
                List.of(devices, downloads), List.of(giftCard));
        return new Catalog("root", "Catalog", List.of(marketplace));
    }
}
