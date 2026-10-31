package marketplace.listeners;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import marketplace.Events.PriceChanged;
import marketplace.Observer;

public final class WishlistNotifier implements Observer<PriceChanged> {
    private final Set<String> watchedProductIds;
    private final Queue<PriceChanged> notifications = new ConcurrentLinkedQueue<>();

    public WishlistNotifier(Set<String> watchedProductIds) {
        this.watchedProductIds = Set.copyOf(Objects.requireNonNull(watchedProductIds));
    }

    @Override
    public void onEvent(PriceChanged event) {
        Objects.requireNonNull(event);
        if (watchedProductIds.contains(event.productId())) {
            notifications.add(event);
        }
    }

    public List<PriceChanged> notifications() {
        return List.copyOf(notifications);
    }
}
