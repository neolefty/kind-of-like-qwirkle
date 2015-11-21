package qwirkle.game.event;

import qwirkle.game.control.GameStatus;
import qwirkle.game.base.QwirkleSettings;

/** Event posted (by GameStatus) when a game is started. */
public class GameStarted {
    private GameStatus status;
    public GameStarted(GameStatus status) { this.status = status; }
    public GameStatus getStatus() { return status; }
    public QwirkleSettings getSettings() { return status.getSettings(); }
    @Override
    public String toString() { return "Game started: " + getSettings(); }
}
