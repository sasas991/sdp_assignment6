package marketplace.catalog;

import java.math.BigDecimal;
import marketplace.visitor.Visitor;

public final class GiftCard extends Product {
    public GiftCard(String id, String name, BigDecimal price, int quantity) {
        super(id, name, price, quantity);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
