package marketplace;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import marketplace.Events.CategoryReportReady;
import marketplace.Events.PriceChanged;
import marketplace.Events.StockChanged;
import marketplace.Events.Type;
import marketplace.catalog.Catalog;
import marketplace.catalog.Category;
import marketplace.catalog.DigitalProduct;
import marketplace.catalog.GiftCard;
import marketplace.catalog.PhysicalProduct;
import marketplace.catalog.Product;
import marketplace.catalog.ServiceProduct;
import marketplace.listeners.Analytics;
import marketplace.listeners.CatalogCache;
import marketplace.listeners.PriceDropAlert;
import marketplace.listeners.WishlistNotifier;
import marketplace.visitor.CostVisitor;
import marketplace.visitor.FilterVisitor;
import marketplace.visitor.XmlVisitor;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int threadCount = args.length == 0 ? 3 : Integer.parseInt(args[0]);
        if (args.length > 1 || threadCount < 3) {
            throw new IllegalArgumentException("thread count must be at least 3");
        }

        var laptop = new PhysicalProduct("laptop", "Laptop", new BigDecimal("1000"),
                3, new BigDecimal("2.5"));
        var ebook = new DigitalProduct("ebook", "E-book", new BigDecimal("30"),
                10, 1_000_000);
        var setup = new ServiceProduct("setup", "Setup", new BigDecimal("100"),
                5, 60);
        var giftCard = new GiftCard("gift-card", "Gift card", new BigDecimal("50"), 20);
        var devices = new Category("devices", "Devices", List.of(), List.of(laptop));
        var electronics = new Category("electronics", "Electronics", List.of(devices),
                List.of(ebook, giftCard));
        var services = new Category("services", "Services", List.of(), List.of(setup));
        var catalog = new Catalog("marketplace", "Marketplace", List.of(electronics, services));

        var priceSource = new MarketSource<PriceChanged>();
        var stockSource = new MarketSource<StockChanged>();
        var reportSource = new MarketSource<CategoryReportReady>();
        var wishlist = new WishlistNotifier(Set.of(laptop.id()));
        var priceDrops = new PriceDropAlert();
        var analytics = new Analytics();
        var cache = new CatalogCache();
        var categoryReports = new CategoryReportService(
                Map.of(electronics.id(), electronics, services.id(), services), reportSource);
        var reports = new ConcurrentLinkedQueue<CategoryReportReady>();

        priceSource.subscribe(Type.PRICE_CHANGED, wishlist);
        priceSource.subscribe(Type.PRICE_CHANGED, priceDrops);
        priceSource.subscribe(Type.PRICE_CHANGED, analytics);
        priceSource.subscribe(Type.PRICE_CHANGED, cache);
        stockSource.subscribe(Type.STOCK_CHANGED, analytics);
        stockSource.subscribe(Type.STOCK_CHANGED, cache);
        stockSource.subscribe(Type.STOCK_CHANGED, categoryReports);
        categoryReports.subscribe(Type.CATEGORY_REPORT_READY, analytics);
        categoryReports.subscribe(Type.CATEGORY_REPORT_READY, reports::add);

        runProducers(threadCount, priceSource, stockSource, laptop, setup);

        System.out.println("Price events: " + analytics.count(Type.PRICE_CHANGED));
        System.out.println("Stock events: " + analytics.count(Type.STOCK_CHANGED));
        System.out.println("Reports: " + List.copyOf(reports));
        System.out.println("Wishlist notifications: " + wishlist.notifications().size());
        System.out.println("Price drop alerts: " + priceDrops.alerts().size());
        System.out.println("Cached laptop price: " + cache.price(laptop.id()).orElseThrow());
        System.out.println("Tax: " + catalog.accept(new CostVisitor(CostVisitor.Mode.TAX)));
        System.out.println("Shipping: "
                + catalog.accept(new CostVisitor(CostVisitor.Mode.SHIPPING)));
        System.out.println("Low stock: " + catalog.accept(new FilterVisitor(
                product -> product.quantity() <= 5)).stream().map(Product::name).toList());
        System.out.println(catalog.accept(new XmlVisitor()));
    }

    private static void runProducers(int threadCount, MarketSource<PriceChanged> priceSource,
            MarketSource<StockChanged> stockSource, PhysicalProduct laptop,
            ServiceProduct setup) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            List<Future<?>> tasks = List.of(
                    executor.submit(() -> {
                        var oldPrice = laptop.updatePrice(new BigDecimal("900"));
                        priceSource.publish(new PriceChanged("electronics", laptop.id(),
                                oldPrice, laptop.price()));
                    }),
                    executor.submit(() -> stockSource.publish(new StockChanged(
                            "electronics", laptop.id(), laptop.quantity(), 4))),
                    executor.submit(() -> stockSource.publish(new StockChanged(
                            "services", setup.id(), setup.quantity(), 6))));
            executor.shutdown();
            ExecutionException failure = null;
            for (var task : tasks) {
                try {
                    task.get();
                } catch (ExecutionException exception) {
                    if (failure == null) {
                        failure = exception;
                    } else {
                        failure.addSuppressed(exception);
                    }
                }
            }
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Producers did not terminate");
            }
            if (failure != null) {
                throw failure;
            }
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }
    }
}
