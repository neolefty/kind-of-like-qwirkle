package qwirkle.event;

/** Notification that an event is about to be posted, so get your stories straight! */
public class PreEvent {
    private Object event;

    public PreEvent(Object event) {
        if (event == null)
            throw new NullPointerException("event is null");
        this.event = event;
    }
    public Object getEvent() { return event; }
}
