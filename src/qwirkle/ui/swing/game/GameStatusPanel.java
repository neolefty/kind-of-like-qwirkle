package qwirkle.ui.swing.game;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.game.control.GameStatus;
import qwirkle.game.event.GameOver;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.TurnCompleted;
import qwirkle.game.event.TurnStarting;

import javax.swing.*;
import java.util.concurrent.Callable;

/** Show the status messages from a game. */
public class GameStatusPanel extends Box {
    // Show the status of the current turn and the overall game
    private TurnHighlightingLabel turnLabel, bestTurnLabel;
    private TurnCompleted bestTurn, lastTurn;

    public GameStatusPanel(QwirkleUIController control) {
        super(BoxLayout.X_AXIS);

        EventBus bus = control.getEventBus();
        turnLabel = new TurnHighlightingLabel(bus, this, 0.023, new Callable<TurnCompleted>() {
            @Override public TurnCompleted call() { return lastTurn; }
        });
        bestTurnLabel = new TurnHighlightingLabel(bus, this, 0.023, new Callable<TurnCompleted>() {
            @Override public TurnCompleted call() { return bestTurn; }
        });

        add(turnLabel, Box.LEFT_ALIGNMENT);
        add(Box.createGlue()); // fill space between labels
        add(bestTurnLabel, Box.RIGHT_ALIGNMENT);

        control.register(new Object() {
            @Subscribe
            public void gameOver(GameOver gameOver) {
                finished(gameOver.getStatus());
            }

            @Subscribe
            public void gameStarted(GameStarted started) {
                bestTurn = null;
                lastTurn = null;
//                bestTurnLabel.setText("");
            }

            @Subscribe
            public void turn(TurnStarting starting) {
                GameStatus status = starting.getStatus();
                if (status.getAnnotated() != null
                        && status.getAnnotated().getBestTurn() != null) {
                    bestTurn = status.getAnnotated().getBestTurn();
                    // note spaces at left & right -- cosmetic
                    bestTurnLabel.setText(" Best: " + bestTurn.getSummary(true) + " ");
                    bestTurnLabel.setToolTipText("Best turn so far in this game: " + bestTurn.getSummary(false));
                }
            }

            @Subscribe
            public void turn(TurnCompleted turn) {
                lastTurn = turn;
                // note spaces at left & right -- cosmetic
                turnLabel.setText(" Last: " + turn.getSummary(true) + " ");
                turnLabel.setToolTipText("Last turn: " + turn.getSummary(false));
            }
        });
    }

    private void finished(GameStatus status) {
        // TODO add a label that only pops up when the game ends. Maybe below the two turn labels?
        turnLabel.setText(" Game Over: " + status.getFinishedShort() + " ");
        turnLabel.setToolTipText("Game Over: " + status.getFinishedLong());
    }
}
