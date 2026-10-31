package marketplace.visitor;

import marketplace.catalog.Catalog;
import marketplace.catalog.Category;
import marketplace.catalog.DigitalProduct;
import marketplace.catalog.PhysicalProduct;
import marketplace.catalog.Product;
import marketplace.catalog.ServiceProduct;

public final class StatsVisitor implements Visitor<StatsVisitor.Stats> {
    public record Stats(int productCount, int totalStock) {
    }

    private int productCount;
    private int totalStock;

    @Override
    public Stats visit(Catalog catalog) {
        for (var category : catalog.categories()) {
            category.accept(this);
        }
        return result();
    }

    @Override
    public Stats visit(Category category) {
        for (var subcategory : category.subcategories()) {
            subcategory.accept(this);
        }
        for (var product : category.products()) {
            product.accept(this);
        }
        return result();
    }

    @Override
    public Stats visit(PhysicalProduct product) {
        return count(product);
    }

    @Override
    public Stats visit(DigitalProduct product) {
        return count(product);
    }

    @Override
    public Stats visit(ServiceProduct product) {
        return count(product);
    }

    private Stats count(Product product) {
        productCount = Math.addExact(productCount, 1);
        totalStock = Math.addExact(totalStock, product.quantity());
        return result();
    }

    private Stats result() {
        return new Stats(productCount, totalStock);
    }
}
