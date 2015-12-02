package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/** Umbrella for various UI-oriented event managers. */
public class InteractionController {
    private DragToPlayPromoter dragToPlayPromoter;
    private HypotheticalPlay hypotheticalPlay;
    private DiscardController discardController;
    private EventBus bus;

    public InteractionController() {
        bus = new EventBus(new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                System.out.println(context);
                exception.printStackTrace(System.out);
            }
        });

        dragToPlayPromoter = new DragToPlayPromoter(bus);
        hypotheticalPlay = new HypotheticalPlay(bus);
        discardController = new DiscardController(this);
    }

    public EventBus getEventBus() { return bus; }
    public DragToPlayPromoter getDragToPlayPromoter() { return dragToPlayPromoter; }
    public HypotheticalPlay getHypotheticalPlay() { return hypotheticalPlay; }
    public DiscardController getDiscardController() { return discardController; }

    /** Convenience method. Calls {@link EventBus#post}. */
    public void post(Object event) { bus.post(event); }
    /** Convenience method. Calls {@link EventBus#register}. */
    public void register(Object subscriber) { bus.register(subscriber); }

    public void unregister(Object subscriber) { bus.unregister(subscriber); }
}
