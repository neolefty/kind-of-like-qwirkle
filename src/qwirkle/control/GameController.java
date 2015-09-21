package qwirkle.control;

import com.google.common.eventbus.EventBus;
import qwirkle.game.QwirkleSettings;

/** Interactive event processing and game model, encapsulated. */
public class GameController {
    private GameModel game;
    private EventsController events;

    public GameController(QwirkleSettings settings, ThreadingStrategy threading) {
        this.events = new EventsController();
        this.game = new GameModel(events.getEventBus(), settings, threading);
    }

    public GameController(GameModel game, EventsController events) {
        this.game = game;
        this.events = events;
    }

    public GameModel getGame() { return game; }
    public EventsController getEventsController() { return events; }
    public EventBus getEventBus() { return events.getEventBus(); }

    /** Convenience method. Calls {@link EventBus#post}. */
    public void post(Object event) { events.post(event); }

    /** Convenience method. Calls {@link EventBus#register}. */
    public void register(Object subscriber) { events.register(subscriber); }
}
