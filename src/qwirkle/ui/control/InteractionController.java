package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/** Umbrella for various UI-oriented event managers. */
public class InteractionController {
    private PieceDropWatcher pieceDropWatcher;
    private HypotheticalPlay hypotheticalPlay;
    private EventBus bus;

    public InteractionController() {
        bus = new EventBus(new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                System.out.println(context);
                exception.printStackTrace(System.out);
            }
        });

        pieceDropWatcher = new PieceDropWatcher(bus);
        hypotheticalPlay = new HypotheticalPlay(bus);
    }

    public EventBus getEventBus() { return bus; }
    public PieceDropWatcher getPieceDropWatcher() { return pieceDropWatcher; }
    public HypotheticalPlay getHypotheticalPlay() { return hypotheticalPlay; }

    /** Convenience method. Calls {@link EventBus#post}. */
    public void post(Object event) { bus.post(event); }

    /** Convenience method. Calls {@link EventBus#register}. */
    public void register(Object subscriber) { bus.register(subscriber); }
    public void unregister(Object subscriber) { bus.unregister(subscriber); }
}
