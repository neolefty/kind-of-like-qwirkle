package qwirkle.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.event.GameStarted;
import qwirkle.event.PreEvent;
import qwirkle.game.AsyncPlayer;
import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirkleSettings;

import java.util.List;

/** The read-only, but live & changing, current state of a game.
 *  Gives a view of the game without giving access to GameManager. */
public class GameStatus {
    private GameModel game;
    private AnnotatedGame annotatedGame;

    /** Use Prestarter for a public constructor. */
    public GameStatus(final EventBus bus, final GameModel game) {
        this.game = game;
        bus.register(new Object() {
            // start a new AnnotatedGame each time a game begins
            @Subscribe
            public void gameStarted(PreEvent pre) {
                if (pre.getEvent() instanceof GameStarted)
                    // note: AnnotatedGame will handle announcing itself
                    annotatedGame = new AnnotatedGame(bus);
            }
        });
    }

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
}
