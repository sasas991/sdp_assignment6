package marketplace.catalog;

import marketplace.visitor.Visitor;

public interface CatalogElement {
    String id();

    String name();

    <R> R accept(Visitor<R> visitor);
}
