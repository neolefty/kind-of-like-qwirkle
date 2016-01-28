package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.control.*;
import qwirkle.ui.event.PlayTurn;
import qwirkle.game.base.QwirkleSettings;

/** Interactive event processing and game model, encapsulated, plus convenience methods. */
public class QwirkleUIController {
    private GameController game;
    private InteractionController interact;
    private QwirkleThreads threads;

    public QwirkleUIController(QwirkleSettings settings, ThreadingStrategy threading) {
        this.interact = new InteractionController();
        this.game = new GameController(interact.getEventBus(), settings, threading);

        // link the interaction controller with the game controller by watching for PlayTurn events
        interact.register(new Object() {
            @Subscribe
            public void turnRequested(PlayTurn turn) {
                if (turn.getPlacements() != null)
                    game.play(game.getCurrentPlayer(), turn.getPlacements());
                else if (turn.getDiscards() != null)
                    game.discard(game.getCurrentPlayer(), turn.getDiscards());
            }
        });
        threads = new QwirkleThreads(game);
    }

    public QwirkleUIController(GameController game, InteractionController interact) {
        this.game = game;
        this.interact = interact;
    }

    public GameController getGame() { return game; }
    public InteractionController getInteraction() { return interact; }
    public HypotheticalPlayController getHypothetical() { return interact.getHypotheticalPlay(); }
    public EventBus getEventBus() { return interact.getEventBus(); }
    public QwirkleThreads getThreads() { return threads; }

    /** Convenience method. Calls {@link EventBus#post}. */
    public void post(Object event) { interact.post(event); }

    /** Convenience method. Calls {@link EventBus#register}. */
    public void register(Object subscriber) { interact.register(subscriber); }
    public void unregister(Object subscriber) { interact.unregister(subscriber); }

    public DiscardTracker getDiscardController() {
        return getInteraction().getDiscardTracker();
    }
}
