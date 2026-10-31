package marketplace.listeners;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import marketplace.Events.PriceChanged;
import marketplace.Observer;

public final class PriceDropAlert implements Observer<PriceChanged> {
    private final Queue<PriceChanged> alerts = new ConcurrentLinkedQueue<>();

    @Override
    public void onEvent(PriceChanged event) {
        Objects.requireNonNull(event);
        if (event.newPrice().compareTo(event.oldPrice()) < 0) {
            alerts.add(event);
        }
    }

    public List<PriceChanged> alerts() {
        return List.copyOf(alerts);
    }
}
