package qwirkle.control.event;

import qwirkle.control.GameStatus;
import qwirkle.game.AsyncPlayer;

/** It's time for a new turn to start. */
public class TurnStarting {
    private GameStatus status;

    public TurnStarting(GameStatus status) { this.status = status; }

    public GameStatus getStatus() { return status; }

    public AsyncPlayer getCurPlayer() { return status.getCurPlayer(); }
}
