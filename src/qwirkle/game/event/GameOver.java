package qwirkle.game.event;

import qwirkle.game.control.GameStatus;

/** Event posted (by AnnotatedGame) when a game ends. */
public class GameOver {
    private GameStatus status;

    public GameOver(GameStatus status) { this.status = status; }

    public GameStatus getStatus() { return status; }
}
