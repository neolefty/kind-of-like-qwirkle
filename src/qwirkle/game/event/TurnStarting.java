package qwirkle.game.event;

import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.control.GameStatus;

/** It's time for a new turn to start. */
public class TurnStarting {
    private GameStatus status;

    public TurnStarting(GameStatus status) { this.status = status; }

    public GameStatus getStatus() { return status; }

    public QwirklePlayer getCurPlayer() { return status.getCurPlayer(); }

    @Override
    public String toString() {
        return "Starting turn for " + getCurPlayer().getName() + ".";
    }
}
