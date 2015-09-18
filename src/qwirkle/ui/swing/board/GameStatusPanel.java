package qwirkle.ui.swing.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameManager;
import qwirkle.control.GameStatus;
import qwirkle.event.GameOver;
import qwirkle.event.GameStarted;
import qwirkle.event.TurnStarting;
import qwirkle.event.QwirkleTurn;

import javax.swing.*;
import java.util.concurrent.Callable;

/** Show the status messages from a game. */
public class GameStatusPanel extends Box {
    // Show the status of the current turn and the overall game
    private TurnHighlightingLabel turnLabel, bestTurnLabel;
    private QwirkleTurn bestTurn, lastTurn;

    public GameStatusPanel(final GameManager mgr) {
        super(BoxLayout.X_AXIS);

        EventBus bus = mgr.getEventBus();
        turnLabel = new TurnHighlightingLabel(bus, this, 0.025, new Callable<QwirkleTurn>() {
            @Override public QwirkleTurn call() { return lastTurn; }
        });
        bestTurnLabel = new TurnHighlightingLabel(bus, this, 0.025, new Callable<QwirkleTurn>() {
            @Override public QwirkleTurn call() { return bestTurn; }
        });

        add(turnLabel, Box.LEFT_ALIGNMENT);
        add(Box.createGlue()); // fill space between labels
        add(bestTurnLabel, Box.RIGHT_ALIGNMENT);

        mgr.getEventBus().register(new Object() {
            @Subscribe public void gameOver(GameOver gameOver) {
                finished(gameOver.getStatus());
            }
            @Subscribe public void gameStarted(GameStarted started) {
                bestTurn = null;
                lastTurn = null;
//                bestTurnLabel.setText("");
            }
            @Subscribe public void turn(TurnStarting starting) {
                GameStatus status = starting.getStatus();
                if (status.getAnnotatedGame() != null
                        && status.getAnnotatedGame().getBestTurn() != null) {
                    bestTurn = status.getAnnotatedGame().getBestTurn();
                    // note spaces at left & right -- cosmetic
                    bestTurnLabel.setText(" Best: " + bestTurn.getSummary(true) + " ");
                    bestTurnLabel.setToolTipText("Best turn so far in this game: " + bestTurn.getSummary(false));
                }
            }
            @Subscribe public void turn(QwirkleTurn turn) {
                lastTurn = turn;
                // note spaces at left & right -- cosmetic
                turnLabel.setText(" Last: " + turn.getSummary(true) + " ");
                turnLabel.setToolTipText("Last turn: " + turn.getSummary(false));
            }
        });
    }

    private void finished(GameStatus status) {
        // TODO add a label that only pops up when the game ends. Maybe below the two turn labels?
        turnLabel.setText(" Game Over: " + status.getFinishedMessage() + " ");
        turnLabel.setToolTipText("Game Over: " + status.getFinishedMessage());
    }
}
