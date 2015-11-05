package qwirkle.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.event.PlayTurn;
import qwirkle.game.QwirkleSettings;

/** Interactive event processing and game model, encapsulated. */
public class GameController {
    private GameModel game;
    private InteractionController interact;

    public GameController(QwirkleSettings settings, ThreadingStrategy threading) {
        this.interact = new InteractionController();
        this.game = new GameModel(interact.getEventBus(), settings, threading);

        // link the two together by watching for PlayTurn
        interact.register(new Object() {
            @Subscribe
            public void turnRequested(PlayTurn turn) {
                game.play(game.getCurrentPlayer(), turn.getPlacements());
            }
        });
    }

    public GameController(GameModel game, InteractionController interact) {
        this.game = game;
        this.interact = interact;
    }

    public GameModel getGame() { return game; }
    public InteractionController getInteraction() { return interact; }
    public HypotheticalPlay getHypothetical() { return interact.getHypotheticalPlay(); }
    public EventBus getEventBus() { return interact.getEventBus(); }

    /** Convenience method. Calls {@link EventBus#post}. */
    public void post(Object event) { interact.post(event); }

    /** Convenience method. Calls {@link EventBus#register}. */
    public void register(Object subscriber) { interact.register(subscriber); }
    public void unregister(Object subscriber) { interact.unregister(subscriber); }
}
