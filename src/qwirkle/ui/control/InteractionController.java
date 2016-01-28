package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.control.GameHistory;

import java.util.HashMap;
import java.util.Map;

/** Umbrella for various UI-oriented event managers. */
public class InteractionController {
    private HypotheticalPlayController hypotheticalPlay;
    private EventBus bus;
    private final Map<QwirklePlayer, PlayerHandTracker> handTrackers = new HashMap<>();

    public InteractionController() {
        bus = new EventBus(new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                System.out.println(context);
                exception.printStackTrace(System.out);
            }
        });

        new DragToPlayPromoter(bus);
        hypotheticalPlay = new HypotheticalPlayController(bus);
        new GameHistory(bus);
    }

    public EventBus getEventBus() { return bus; }
    public HypotheticalPlayController getHypotheticalPlay() { return hypotheticalPlay; }
    public DiscardTracker getDiscardTracker() { return hypotheticalPlay.getDiscardTracker(); }

    public PlayerHandTracker getHandTracker(QwirklePlayer player) {
        synchronized (handTrackers) {
            if (!handTrackers.containsKey(player))
                handTrackers.put(player, new PlayerHandTracker(bus, player));
            return handTrackers.get(player);
        }
    }

    /** Convenience method. Calls {@link EventBus#post}. */
    public void post(Object event) { bus.post(event); }
    /** Convenience method. Calls {@link EventBus#register}. */
    public void register(Object subscriber) { bus.register(subscriber); }

    public void unregister(Object subscriber) { bus.unregister(subscriber); }
}
