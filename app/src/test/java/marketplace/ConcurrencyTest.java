package marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import marketplace.Events.StockChanged;
import marketplace.Events.Type;
import marketplace.listeners.Analytics;
import org.junit.jupiter.api.Test;

class ConcurrencyTest {
    @Test
    void fourProducersDeliverAllFourThousandEvents() throws Exception {
        var source = new MarketSource<StockChanged>();
        var analytics = new Analytics();
        Set<String> receivedIds = ConcurrentHashMap.newKeySet();
        source.subscribe(Type.STOCK_CHANGED, analytics);
        source.subscribe(Type.STOCK_CHANGED, event -> receivedIds.add(event.productId()));
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(4);

        try {
            var tasks = new ArrayList<Future<?>>();
            for (int producer = 0; producer < 4; producer++) {
                int producerId = producer;
                tasks.add(executor.submit(() -> {
                    start.await();
                    for (int event = 0; event < 1_000; event++) {
                        source.publish(new StockChanged("category",
                                producerId + "-" + event, 0, 1));
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

        assertEquals(4_000, analytics.count(Type.STOCK_CHANGED));
        assertEquals(4_000, receivedIds.size());
    }
}
