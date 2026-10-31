package marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import marketplace.Events.CategoryReportReady;
import marketplace.Events.StockChanged;
import marketplace.Events.Type;
import marketplace.catalog.Category;
import marketplace.catalog.GiftCard;
import marketplace.catalog.PhysicalProduct;
import marketplace.catalog.ServiceProduct;
import marketplace.listeners.Analytics;
import org.junit.jupiter.api.Test;

class IntegrationTest {
    @Test
    void stockEventUpdatesNestedProductAndReportsOnlyItsCategory() {
        var fixture = fixture();

        fixture.stockSource.publish(new StockChanged("electronics", "laptop", 2, 5));

        assertEquals(5, fixture.laptop.quantity());
        assertEquals(7, fixture.setup.quantity());
        assertEquals(List.of(new CategoryReportReady("electronics", 2, 9)),
                List.copyOf(fixture.reports));
    }

    @Test
    void reportEventReachesAnalyticsAfterStockEvent() {
        var fixture = fixture();
        var analytics = new Analytics();
        fixture.stockSource.subscribe(Type.STOCK_CHANGED, analytics);
        fixture.reportService.subscribe(Type.CATEGORY_REPORT_READY, analytics);

        fixture.stockSource.publish(new StockChanged("services", "setup", 7, 3));

        assertEquals(1, analytics.count(Type.STOCK_CHANGED));
        assertEquals(1, analytics.count(Type.CATEGORY_REPORT_READY));
        assertEquals(List.of(new CategoryReportReady("services", 1, 3)),
                List.copyOf(fixture.reports));
    }

    @Test
    void unknownCategoryDoesNotChangeProductOrEmitReport() {
        var fixture = fixture();

        assertThrows(IllegalArgumentException.class,
                () -> fixture.stockSource.publish(
                        new StockChanged("missing", "laptop", 2, 5)));

        assertEquals(2, fixture.laptop.quantity());
        assertTrue(fixture.reports.isEmpty());
    }

    @Test
    void productOutsideCategoryDoesNotChangeStockOrEmitReport() {
        var fixture = fixture();

        assertThrows(IllegalArgumentException.class,
                () -> fixture.stockSource.publish(
                        new StockChanged("services", "laptop", 2, 5)));

        assertEquals(2, fixture.laptop.quantity());
        assertEquals(7, fixture.setup.quantity());
        assertTrue(fixture.reports.isEmpty());
    }

    private static Fixture fixture() {
        var laptop = new PhysicalProduct("laptop", "Laptop", new BigDecimal("100"),
                2, new BigDecimal("1"));
        var giftCard = new GiftCard("gift", "Gift", new BigDecimal("25"), 4);
        var setup = new ServiceProduct("setup", "Setup", new BigDecimal("80"),
                7, 60);
        var devices = new Category("devices", "Devices", List.of(), List.of(laptop));
        var electronics = new Category("electronics", "Electronics", List.of(devices),
                List.of(giftCard));
        var services = new Category("services", "Services", List.of(), List.of(setup));
        var stockSource = new MarketSource<StockChanged>();
        var reportSource = new MarketSource<CategoryReportReady>();
        var reportService = new CategoryReportService(
                Map.of("electronics", electronics, "services", services), reportSource);
        var reports = new ConcurrentLinkedQueue<CategoryReportReady>();
        stockSource.subscribe(Type.STOCK_CHANGED, reportService);
        reportService.subscribe(Type.CATEGORY_REPORT_READY, reports::add);
        return new Fixture(stockSource, reportService, laptop, setup, reports);
    }

    private record Fixture(
            MarketSource<StockChanged> stockSource,
            CategoryReportService reportService,
            PhysicalProduct laptop,
            ServiceProduct setup,
            ConcurrentLinkedQueue<CategoryReportReady> reports
    ) {
    }
}
