package marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import marketplace.catalog.Catalog;
import marketplace.catalog.Category;
import marketplace.catalog.DigitalProduct;
import marketplace.catalog.GiftCard;
import marketplace.catalog.PhysicalProduct;
import marketplace.catalog.Product;
import marketplace.catalog.ServiceProduct;
import marketplace.visitor.CostVisitor;
import marketplace.visitor.FilterVisitor;
import marketplace.visitor.StatsVisitor;
import marketplace.visitor.XmlVisitor;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

class VisitorTest {
    @Test
    void calculatesTaxAcrossFourProductTypes() {
        var actual = catalog().accept(new CostVisitor(CostVisitor.Mode.TAX));

        assertEquals(0, new BigDecimal("43.00").compareTo(actual));
    }

    @Test
    void chargesShippingOnlyForPhysicalProducts() {
        var actual = catalog().accept(new CostVisitor(CostVisitor.Mode.SHIPPING));

        assertEquals(0, new BigDecimal("6.00").compareTo(actual));
    }

    @Test
    void accumulatesProductCountAndStockThroughNestedCategories() {
        var actual = catalog().accept(new StatsVisitor());

        assertEquals(new StatsVisitor.Stats(4, 10), actual);
    }

    @Test
    void filtersProductsAcrossNestedCategories() {
        var actual = catalog().accept(new FilterVisitor(product -> product.quantity() <= 2));

        assertEquals(List.of("p1", "p3"), actual.stream().map(Product::id).toList());
    }

    @Test
    void exportsNestedXmlWithEscapedAttributes() throws Exception {
        var giftCard = new GiftCard("gift", "Card <VIP> & \"gold\"",
                new BigDecimal("25"), 1);
        var nested = new Category("nested", "Offers", List.of(), List.of(giftCard));
        var parent = new Category("parent", "Main", List.of(nested), List.of());
        var catalog = new Catalog("root", "Market & More", List.of(parent));

        var xml = catalog.accept(new XmlVisitor());
        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
        var root = document.getDocumentElement();
        var categories = root.getElementsByTagName("category");
        var products = root.getElementsByTagName("giftCard");

        assertEquals("catalog", root.getTagName());
        assertEquals("Market & More", root.getAttribute("name"));
        assertEquals(2, categories.getLength());
        assertEquals(1, products.getLength());
        assertEquals("Card <VIP> & \"gold\"",
                products.item(0).getAttributes().getNamedItem("name").getNodeValue());
        assertTrue(products.item(0).getParentNode().isSameNode(categories.item(1)));
    }

    private static Catalog catalog() {
        var physical = new PhysicalProduct("p1", "Laptop", new BigDecimal("100"),
                2, new BigDecimal("1.5"));
        var digital = new DigitalProduct("p2", "Book", new BigDecimal("50"),
                3, 1024);
        var service = new ServiceProduct("p3", "Setup", new BigDecimal("80"),
                1, 60);
        var giftCard = new GiftCard("p4", "Gift", new BigDecimal("25"), 4);
        var devices = new Category("c2", "Devices", List.of(), List.of(physical));
        var downloads = new Category("c3", "Downloads", List.of(),
                List.of(digital, service));
        var marketplace = new Category("c1", "Marketplace",
                List.of(devices, downloads), List.of(giftCard));
        return new Catalog("root", "Catalog", List.of(marketplace));
    }
}
