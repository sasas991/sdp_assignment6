package marketplace.catalog;

import java.util.List;
import java.util.Objects;
import marketplace.visitor.Visitor;

public final class Category implements CatalogElement {
    private final String id;
    private final String name;
    private final List<Category> subcategories;
    private final List<Product> products;

    public Category(String id, String name, List<Category> subcategories,
            List<Product> products) {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("id and name cannot be blank");
        }
        this.id = id;
        this.name = name;
        this.subcategories = List.copyOf(Objects.requireNonNull(subcategories));
        this.products = List.copyOf(Objects.requireNonNull(products));
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<Category> subcategories() {
        return subcategories;
    }

    public List<Product> products() {
        return products;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
