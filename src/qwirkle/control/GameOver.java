package qwirkle.control;

/** Event posted (by AnnotatedGame) when a game ends. */
public class GameOver {
    private GameStatus status;

    public GameOver(GameStatus status) { this.status = status; }

    public GameStatus getStatus() { return status; }

    /** The game-over message about who won. */
    public String getMessage() { return status.getFinishedMessage(); }
}
