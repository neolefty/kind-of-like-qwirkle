package qwirkle.control;

/** Event posted (by GameStatus) when a game is started. */
public class GameStarted {
    private GameStatus status;
    public GameStarted(GameStatus status) { this.status = status; }
    public GameStatus getStatus() { return status; }
}
