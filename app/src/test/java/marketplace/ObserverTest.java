package marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import marketplace.Events.Event;
import marketplace.Events.PriceChanged;
import marketplace.Events.StockChanged;
import marketplace.Events.Type;
import marketplace.listeners.PriceDropAlert;
import marketplace.listeners.WishlistNotifier;
import org.junit.jupiter.api.Test;

class ObserverTest {
    @Test
    void deliversOnlyEventsOfSubscribedType() {
        var source = new MarketSource<Event>();
        var priceEvents = new AtomicInteger();
        var stockEvents = new AtomicInteger();
        source.subscribe(Type.PRICE_CHANGED, event -> priceEvents.incrementAndGet());
        source.subscribe(Type.STOCK_CHANGED, event -> stockEvents.incrementAndGet());

        source.publish(priceChanged("p1", "10", "9"));
        source.publish(new StockChanged("c1", "p1", 2, 3));
        source.publish(priceChanged("p2", "10", "8"));

        assertEquals(2, priceEvents.get());
        assertEquals(1, stockEvents.get());
    }

    @Test
    void unsubscribeDuringNotificationTakesEffectOnNextEvent() {
        var source = new MarketSource<PriceChanged>();
        var secondCalls = new AtomicInteger();
        Observer<PriceChanged> second = event -> secondCalls.incrementAndGet();
        source.subscribe(Type.PRICE_CHANGED,
                event -> source.unsubscribe(Type.PRICE_CHANGED, second));
        source.subscribe(Type.PRICE_CHANGED, second);

        source.publish(priceChanged("p1", "10", "9"));
        source.publish(priceChanged("p1", "9", "8"));

        assertEquals(1, secondCalls.get());
    }

    @Test
    void subscribeDuringNotificationTakesEffectOnNextEvent() {
        var source = new MarketSource<PriceChanged>();
        var addedCalls = new AtomicInteger();
        Observer<PriceChanged> added = event -> addedCalls.incrementAndGet();
        source.subscribe(Type.PRICE_CHANGED,
                event -> source.subscribe(Type.PRICE_CHANGED, added));

        source.publish(priceChanged("p1", "10", "9"));
        source.publish(priceChanged("p1", "9", "8"));

        assertEquals(1, addedCalls.get());
    }

    @Test
    void failingObserverDoesNotPreventOtherNotifications() {
        var source = new MarketSource<PriceChanged>();
        var failure = new IllegalStateException("listener failed");
        var nextCalls = new AtomicInteger();
        source.subscribe(Type.PRICE_CHANGED, event -> {
            throw failure;
        });
        source.subscribe(Type.PRICE_CHANGED, event -> nextCalls.incrementAndGet());

        var thrown = assertThrows(IllegalStateException.class,
                () -> source.publish(priceChanged("p1", "10", "9")));

        assertSame(failure, thrown);
        assertEquals(1, nextCalls.get());
    }

    @Test
    void wishlistAndPriceAlertApplyDifferentRules() {
        var source = new MarketSource<PriceChanged>();
        var wishlist = new WishlistNotifier(Set.of("watched"));
        var alert = new PriceDropAlert();
        source.subscribe(Type.PRICE_CHANGED, wishlist);
        source.subscribe(Type.PRICE_CHANGED, alert);
        var watchedIncrease = priceChanged("watched", "10", "12");
        var otherDrop = priceChanged("other", "10", "8");
        var watchedDrop = priceChanged("watched", "12", "7");

        source.publish(watchedIncrease);
        source.publish(otherDrop);
        source.publish(watchedDrop);

        assertEquals(java.util.List.of(watchedIncrease, watchedDrop),
                wishlist.notifications());
        assertEquals(java.util.List.of(otherDrop, watchedDrop), alert.alerts());
    }

    private static PriceChanged priceChanged(String productId, String oldPrice,
            String newPrice) {
        return new PriceChanged("c1", productId, new BigDecimal(oldPrice),
                new BigDecimal(newPrice));
    }
}
