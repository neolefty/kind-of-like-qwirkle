package qwirkle.game.control;

import qwirkle.game.base.QwirkleBoard;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.QwirkleSettings;

/** A read-only snapshot of a game state.
 *  Gives a view of the game without giving access to GameController.
 *  Note that annotatedGame continues to update in real time, up to the end
 *  of the current game but not beyond it. */
public class GameStatus {
    private AnnotatedGame annotatedGame;

    private String finishedLong, finishedShort;
    private QwirkleSettings settings;
    private QwirkleBoard board;
    private QwirklePlayer curPlayer;
    private int deckRemaining;

    /** Use Prestarter for a public constructor. */
    public GameStatus(final GameController game) {
        this.annotatedGame = game.getAnnotated();
        finishedLong = game.getFinishedMessageLong();
        finishedShort = game.getFinishedMessageShort();
        settings = game.getSettings();
        board = game.getBoard();
        deckRemaining = game.getDeck().size();
        curPlayer = game.getCurrentPlayer();
    }

    /** What does the playing surface look like? */
    public QwirkleBoard getBoard() { return board; }

    /** A detailed explanation of how the game ended. Null if the game hasn't ended yet. */
    public String getFinishedLong() { return finishedLong; }

    /** A short summary of how the game ended. Null if the game hasn't ended yet. */
    public String getFinishedShort() { return finishedShort; }

    /** Who is the current player? */
    public QwirklePlayer getCurPlayer() { return curPlayer; }

    /** What are the current settings for this game? */
    public QwirkleSettings getSettings() { return settings; }

    /** How many pieces remain to be drawn? */
    public int getDeckRemaining() { return deckRemaining; }

    /** Is the game finished? */
    public boolean isFinished() { return finishedLong != null; }

    /** The current game, annotated with scores etc.
     *  Unlike the rest of this status object, it is live until the current game ends,
     *  when it stops updating. Created anew every time a game starts. */
    public AnnotatedGame getAnnotated() { return annotatedGame; }
}
