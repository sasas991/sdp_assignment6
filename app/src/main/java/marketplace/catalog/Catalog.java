package marketplace.catalog;

import java.util.List;
import java.util.Objects;

public final class Catalog {
    private final String id;
    private final String name;
    private final List<Category> categories;

    public Catalog(String id, String name, List<Category> categories) {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("id and name cannot be blank");
        }
        this.id = id;
        this.name = name;
        this.categories = List.copyOf(Objects.requireNonNull(categories));
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public List<Category> categories() {
        return categories;
    }
}
