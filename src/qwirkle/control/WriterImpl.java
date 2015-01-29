package qwirkle.control;

import com.google.common.eventbus.EventBus;

/** Helper class for monitoring changes of a single property.
 *  Used internally to grant read-only access to a watcher who can receive change notifications.
 *  Decouples the type of the property (T) from the type of the notification (event). */
public class WriterImpl<T> implements Writer<T> {
    // if this is non-null & event bus is non-null, post it after setting
    private Object event;
    private EventBus bus;
    private T t;
    private boolean initialized = false;

    public WriterImpl() { }

    public WriterImpl(Object event, EventBus bus) {
        init(event, bus);
    }

    /** Initialize this.
     *  @param event the event to be posted when T property is changed.
     *  @param bus the event bus.
     */
    public void init(Object event, EventBus bus) {
        if (initialized)
            throw new IllegalStateException("Already initialized.");
        if (event == null)
            throw new NullPointerException("event is null");
        if (bus == null)
            throw new NullPointerException("bus is null");
        this.event = event;
        this.bus = bus;
        initialized = true;
    }

    @Override
    public T get() { return t; }

    @Override
    public void set(T t) {
        this.t = t;
        changed();
    }

    public void changed() {
        if (initialized)
            bus.post(event);
    }

    @Override
    public EventBus getBus() {
        return bus;
    }

    @Override
    public String toString() { return get() == null ? "" + null : get().toString(); }
}
