package marketplace.listeners;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import marketplace.Events.Event;
import marketplace.Events.PriceChanged;
import marketplace.Events.StockChanged;
import marketplace.Observer;

public final class CatalogCache implements Observer<Event> {
    private final ConcurrentMap<String, BigDecimal> prices = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> quantities = new ConcurrentHashMap<>();

    @Override
    public void onEvent(Event event) {
        Objects.requireNonNull(event);
        if (event instanceof PriceChanged priceChanged) {
            prices.put(priceChanged.productId(), priceChanged.newPrice());
        } else if (event instanceof StockChanged stockChanged) {
            quantities.put(stockChanged.productId(), stockChanged.newQuantity());
        }
    }

    public Optional<BigDecimal> price(String productId) {
        return Optional.ofNullable(prices.get(Objects.requireNonNull(productId)));
    }

    public OptionalInt quantity(String productId) {
        var value = quantities.get(Objects.requireNonNull(productId));
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }
}
