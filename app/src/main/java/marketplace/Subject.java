package marketplace;

import marketplace.Events.Event;
import marketplace.Events.Type;

public interface Subject<E extends Event> {
    void subscribe(Type type, Observer<? super E> observer);

    void unsubscribe(Type type, Observer<? super E> observer);

    void publish(E event);
}
