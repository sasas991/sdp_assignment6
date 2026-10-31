package marketplace;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import marketplace.Events.Event;
import marketplace.Events.Subject;
import marketplace.Events.Type;

public final class MarketSource<E extends Event> implements Subject<E> {
    private final ConcurrentMap<Type, CopyOnWriteArrayList<Observer<? super E>>> observers =
            new ConcurrentHashMap<>();

    @Override
    public void subscribe(Type type, Observer<? super E> observer) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(observer);
        observers.computeIfAbsent(type, ignored -> new CopyOnWriteArrayList<>())
                .addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(Type type, Observer<? super E> observer) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(observer);
        var listeners = observers.get(type);
        if (listeners != null) {
            listeners.remove(observer);
        }
    }

    @Override
    public void publish(E event) {
        Objects.requireNonNull(event);
        var listeners = observers.get(event.type());
        if (listeners == null) {
            return;
        }

        RuntimeException failure = null;
        for (var observer : listeners) {
            try {
                observer.onEvent(event);
            } catch (RuntimeException exception) {
                if (failure == null) {
                    failure = exception;
                } else if (failure != exception) {
                    failure.addSuppressed(exception);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
