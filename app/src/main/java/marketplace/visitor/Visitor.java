package marketplace.visitor;

import marketplace.catalog.Catalog;
import marketplace.catalog.Category;
import marketplace.catalog.DigitalProduct;
import marketplace.catalog.PhysicalProduct;
import marketplace.catalog.ServiceProduct;

public interface Visitor<R> {
    R visit(Catalog catalog);

    R visit(Category category);

    R visit(PhysicalProduct product);

    R visit(DigitalProduct product);

    R visit(ServiceProduct product);
}
