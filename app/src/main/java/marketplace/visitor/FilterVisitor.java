package marketplace.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import marketplace.catalog.Catalog;
import marketplace.catalog.Category;
import marketplace.catalog.DigitalProduct;
import marketplace.catalog.PhysicalProduct;
import marketplace.catalog.Product;
import marketplace.catalog.ServiceProduct;

public final class FilterVisitor implements Visitor<List<Product>> {
    private final Predicate<? super Product> predicate;

    public FilterVisitor(Predicate<? super Product> predicate) {
        this.predicate = Objects.requireNonNull(predicate);
    }

    @Override
    public List<Product> visit(Catalog catalog) {
        var matches = new ArrayList<Product>();
        for (var category : catalog.categories()) {
            matches.addAll(category.accept(this));
        }
        return List.copyOf(matches);
    }

    @Override
    public List<Product> visit(Category category) {
        var matches = new ArrayList<Product>();
        for (var subcategory : category.subcategories()) {
            matches.addAll(subcategory.accept(this));
        }
        for (var product : category.products()) {
            matches.addAll(product.accept(this));
        }
        return List.copyOf(matches);
    }

    @Override
    public List<Product> visit(PhysicalProduct product) {
        return match(product);
    }

    @Override
    public List<Product> visit(DigitalProduct product) {
        return match(product);
    }

    @Override
    public List<Product> visit(ServiceProduct product) {
        return match(product);
    }

    private List<Product> match(Product product) {
        return predicate.test(product) ? List.of(product) : List.of();
    }
}
