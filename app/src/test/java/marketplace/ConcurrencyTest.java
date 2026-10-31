package marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import marketplace.Events.CategoryReportReady;
import marketplace.Events.StockChanged;
import marketplace.Events.Type;
import marketplace.catalog.Category;
import marketplace.catalog.GiftCard;
import marketplace.catalog.Product;
import marketplace.listeners.Analytics;
import marketplace.visitor.StatsVisitor;
import org.junit.jupiter.api.Test;

class ConcurrencyTest {
    @Test
    void fourProducersDeliverAllFourThousandEvents() throws Exception {
        var source = new MarketSource<StockChanged>();
        var analytics = new Analytics();
        Set<String> receivedIds = ConcurrentHashMap.newKeySet();
        source.subscribe(Type.STOCK_CHANGED, analytics);
        source.subscribe(Type.STOCK_CHANGED, event -> receivedIds.add(event.productId()));

        runFourProducers(1_000, eventId -> source.publish(new StockChanged(
                "category", Integer.toString(eventId), 0, 1)));

        assertEquals(4_000, analytics.count(Type.STOCK_CHANGED));
        assertEquals(4_000, receivedIds.size());
    }

    @Test
    void concurrentStockEventsProduceConsistentOrderedReports() throws Exception {
        List<Product> products = new ArrayList<>();
        for (int id = 0; id < 400; id++) {
            products.add(new GiftCard("item-" + id, "Gift", BigDecimal.ONE, 0));
        }
        var category = new Category("category", "Gifts", List.of(), products);
        var stockSource = new MarketSource<StockChanged>();
        var reportService = new CategoryReportService(
                Map.of(category.id(), category), new MarketSource<CategoryReportReady>());
        var reports = new ConcurrentLinkedQueue<CategoryReportReady>();
        stockSource.subscribe(Type.STOCK_CHANGED, reportService);
        reportService.subscribe(Type.CATEGORY_REPORT_READY, reports::add);

        runFourProducers(100, eventId -> stockSource.publish(new StockChanged(
                category.id(), "item-" + eventId, 0, 1)));

        assertEquals(400, reports.size());
        assertEquals(IntStream.rangeClosed(1, 400).boxed().toList(),
                reports.stream().map(CategoryReportReady::totalStock).toList());
        assertTrue(reports.stream().allMatch(report -> report.productCount() == 400));
        assertEquals(new StatsVisitor.Stats(400, 400), category.accept(new StatsVisitor()));
    }

    private static void runFourProducers(int eventsPerProducer, IntConsumer publish)
            throws Exception {
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(4);

        try {
            var tasks = new ArrayList<Future<?>>();
            for (int producer = 0; producer < 4; producer++) {
                int producerId = producer;
                tasks.add(executor.submit(() -> {
                    start.await();
                    for (int event = 0; event < eventsPerProducer; event++) {
                        publish.accept(producerId * eventsPerProducer + event);
                    }
                    return null;
                }));
            }
            executor.shutdown();
            start.countDown();
            for (var task : tasks) {
                task.get(15, TimeUnit.SECONDS);
            }
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }
    }
}
