package marketplace.listeners;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import marketplace.Events.Event;
import marketplace.Events.Type;
import marketplace.Observer;

public final class Analytics implements Observer<Event> {
    private final Map<Type, LongAdder> counts;

    public Analytics() {
        var counters = new EnumMap<Type, LongAdder>(Type.class);
        for (var type : Type.values()) {
            counters.put(type, new LongAdder());
        }
        counts = Map.copyOf(counters);
    }

    @Override
    public void onEvent(Event event) {
        counts.get(Objects.requireNonNull(event).type()).increment();
    }

    public long count(Type type) {
        return counts.get(Objects.requireNonNull(type)).sum();
    }
}
