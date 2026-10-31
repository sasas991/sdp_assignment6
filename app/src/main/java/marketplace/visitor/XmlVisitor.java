package marketplace.visitor;

import marketplace.catalog.Catalog;
import marketplace.catalog.CatalogElement;
import marketplace.catalog.Category;
import marketplace.catalog.DigitalProduct;
import marketplace.catalog.PhysicalProduct;
import marketplace.catalog.Product;
import marketplace.catalog.ServiceProduct;

public final class XmlVisitor implements Visitor<String> {
    @Override
    public String visit(Catalog catalog) {
        var xml = startTag("catalog", catalog);
        xml.insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append('>');
        for (var category : catalog.categories()) {
            xml.append(category.accept(this));
        }
        return xml.append("</catalog>").toString();
    }

    @Override
    public String visit(Category category) {
        var xml = startTag("category", category).append('>');
        for (var subcategory : category.subcategories()) {
            xml.append(subcategory.accept(this));
        }
        for (var product : category.products()) {
            xml.append(product.accept(this));
        }
        return xml.append("</category>").toString();
    }

    @Override
    public String visit(PhysicalProduct product) {
        return productTag("physicalProduct", product, "weightKg",
                product.weightKg().toPlainString());
    }

    @Override
    public String visit(DigitalProduct product) {
        return productTag("digitalProduct", product, "downloadSizeBytes",
                Long.toString(product.downloadSizeBytes()));
    }

    @Override
    public String visit(ServiceProduct product) {
        return productTag("serviceProduct", product, "durationMinutes",
                Integer.toString(product.durationMinutes()));
    }

    private static String productTag(String tag, Product product, String attribute,
            String value) {
        return startTag(tag, product)
                .append(" price=\"").append(product.price().toPlainString())
                .append("\" quantity=\"").append(product.quantity())
                .append("\" ").append(attribute).append("=\"").append(escape(value))
                .append("\"/>")
                .toString();
    }

    private static StringBuilder startTag(String tag, CatalogElement element) {
        return new StringBuilder("<").append(tag)
                .append(" id=\"").append(escape(element.id()))
                .append("\" name=\"").append(escape(element.name())).append('"');
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
