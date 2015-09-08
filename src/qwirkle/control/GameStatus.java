package qwirkle.control;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.event.GameOver;
import qwirkle.control.event.GameStarted;
import qwirkle.control.event.PreEvent;
import qwirkle.game.*;

import java.util.List;

/** The read-only, but live & changing, current state of a game.
 *  Gives a view of the game without giving access to GameManager. */
public class GameStatus {
    private GameManager game;
    private AnnotatedGame annotatedGame;

    /** Use Prestarter for a public constructor. */
    public GameStatus(final GameManager game) {
        this.game = game;
        game.getEventBus().register(new Object() {
            // start a new AnnotatedGame each time a game begins
            @Subscribe public void gameStarted(PreEvent pre) {
                if (pre.getEvent() instanceof GameStarted)
                    // note: AnnotatedGame will handle announcing itself
                    annotatedGame = new AnnotatedGame(game.getEventBus());
            }
        });
    }

    /** Listen for updates to the game status. */
    public void listen(StatusListener listener) { game.getEventBus().register(listener); }

    /** Listen for new turns. */
    public void listen(TurnListener listener) { game.getEventBus().register(listener); }

    /** What does the playing surface look like? */
    public QwirkleBoard getBoard() { return game.getBoard(); }

    /** What was the reason the game ended? Null if the game hasn't ended yet. */
    public String getFinishedMessage() { return game.getFinishedMessage(); }

    /** Who is the current player? */
    public AsyncPlayer getCurPlayer() { return game.getCurrentPlayer(); }

    /** What pieces remain to be drawn? */
    public List<QwirklePiece> getDeck() { return game.getDeck(); }

    /** What is a player's score? */
    public int getScore(AsyncPlayer player) { return annotatedGame.getScore(player); }

    /** What are the current settings for this game? */
    public QwirkleSettings getSettings() { return game.getSettings(); }

    /** Is the game finished? */
    public boolean isFinished() { return game.isFinished(); }

    /** The current game, annotated with scores etc. Created anew every time a game starts. */
    public AnnotatedGame getAnnotatedGame() { return annotatedGame; }

    /** The current leader. */
    public AsyncPlayer getLeader() { return annotatedGame.getLeader(); }

    public interface StatusListener {
        @Subscribe void gameStarted(GameStarted started);
        @Subscribe void gameOver(GameOver over);
    }

    public interface TurnListener {
        @Subscribe void turn(QwirkleTurn turn);
    }
}
