package marketplace;

import java.util.Map;
import java.util.Objects;
import marketplace.Events.CategoryReportReady;
import marketplace.Events.StockChanged;
import marketplace.Events.Type;
import marketplace.catalog.Category;
import marketplace.visitor.FilterVisitor;
import marketplace.visitor.StatsVisitor;

public final class CategoryReportService
        implements Observer<StockChanged>, Subject<CategoryReportReady> {
    private final Map<String, Category> categories;
    private final Subject<CategoryReportReady> reports;

    public CategoryReportService(Map<String, Category> categories,
            Subject<CategoryReportReady> reports) {
        this.categories = Map.copyOf(Objects.requireNonNull(categories));
        this.reports = Objects.requireNonNull(reports);
    }

    @Override
    public void onEvent(StockChanged event) {
        Objects.requireNonNull(event);
        var category = categories.get(event.categoryId());
        if (category == null) {
            throw new IllegalArgumentException("Unknown category: " + event.categoryId());
        }

        synchronized (category) {
            var products = category.accept(new FilterVisitor(
                    product -> product.id().equals(event.productId())));
            if (products.size() != 1) {
                throw new IllegalArgumentException("Product must appear once in category: "
                        + event.productId());
            }

            products.get(0).updateQuantity(event.newQuantity());
            var stats = category.accept(new StatsVisitor());
            publish(new CategoryReportReady(category.id(), stats.productCount(),
                    stats.totalStock()));
        }
    }

    @Override
    public void subscribe(Type type, Observer<? super CategoryReportReady> observer) {
        reports.subscribe(type, observer);
    }

    @Override
    public void unsubscribe(Type type, Observer<? super CategoryReportReady> observer) {
        reports.unsubscribe(type, observer);
    }

    @Override
    public void publish(CategoryReportReady event) {
        reports.publish(event);
    }
}
