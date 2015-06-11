package qwirkle.ui.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameManager;
import qwirkle.control.GameStatus;
import qwirkle.control.event.GameOver;
import qwirkle.control.event.GameStarted;
import qwirkle.control.event.HighlightTurn;
import qwirkle.game.QwirkleTurn;
import qwirkle.ui.main.SwingMain;
import qwirkle.ui.swing.HighlightLabel;

import javax.swing.*;

/** Show the status messages from a game. */
public class GameStatusPanel extends Box {
    // Show the status of the current turn and the overall game
    private HighlightLabel turnLabel, bestTurnLabel;
    private QwirkleTurn bestTurn, lastTurn;
    private EventBus bus;

    public GameStatusPanel(final GameManager mgr) {
        super(BoxLayout.X_AXIS);
        this.bus = mgr.getEventBus();
        TurnHighlighter bestHL = new TurnHighlighter(new TurnGetter() {
            @Override public QwirkleTurn getTurn() { return bestTurn; }
        });
        TurnHighlighter lastHL = new TurnHighlighter(new TurnGetter() {
            @Override public QwirkleTurn getTurn() { return lastTurn; }
        });
        turnLabel = new HighlightLabel(this, 0.025, SwingMain.Colors.MOUSE_HL,
                lastHL.createHighlighter(true), lastHL.createHighlighter(false));
        bestTurnLabel = new HighlightLabel(this, 0.025, SwingMain.Colors.MOUSE_HL,
                bestHL.createHighlighter(true), bestHL.createHighlighter(false));
//        add(Box.createHorizontalStrut(5), Box.LEFT_ALIGNMENT);
        add(turnLabel, Box.LEFT_ALIGNMENT);
        add(Box.createGlue()); // fill space between labels
        add(bestTurnLabel, Box.RIGHT_ALIGNMENT);
//        add(Box.createHorizontalStrut(5), Box.RIGHT_ALIGNMENT);
        mgr.getEventBus().register(new Object() {
            @Subscribe
            public void gameOver(GameOver gameOver) {
                finished(gameOver.getStatus());
            }
            @Subscribe public void gameStarted(GameStarted started) {
                bestTurn = null;
                lastTurn = null;
//                bestTurnLabel.setText("");
            }
            @Subscribe public void status(GameStatus status) {
                if (status.isFinished()) {
                    finished(status);
                }
                else if (status.getAnnotatedGame() != null
                        && status.getAnnotatedGame().getBestTurn() != null) {
                    bestTurn = status.getAnnotatedGame().getBestTurn();
                    bestTurnLabel.setText(" Best: " + bestTurn.getSummary(true) + " ");
                    bestTurnLabel.setToolTipText("Best turn so far in this game: " + bestTurn.getSummary(false));
                }
            }
            @Subscribe public void turn(QwirkleTurn turn) {
                lastTurn = turn;
                turnLabel.setText(" Last: " + turn.getSummary(true) + " ");
                turnLabel.setToolTipText("Last turn: " + turn.getSummary(false));
            }
        });
    }

    /** Fetch a particular turn. */
    private interface TurnGetter {
        QwirkleTurn getTurn();
    }

    /** Creates TurnGetters to highlight turns. */
    private class TurnHighlighter {
        private QwirkleTurn lastHighlight = null;
        private TurnGetter getter;

        TurnHighlighter(TurnGetter getter) {
            this.getter = getter;
        }

        /** A runnable that will begin or end highlighting this turn. */
        Runnable createHighlighter(final boolean highlight) {
            return new Runnable() {
                @Override
                public void run() {
                    // undo the previous highlight, if there is one
                    postUnhighlight();
                    // do the new highlight
                    if (highlight)
                        postHighlight(getter.getTurn());
                }
            };
        }

        // highlight something
        private synchronized void postHighlight(QwirkleTurn turn) {
            if (turn != null) {
                this.lastHighlight = turn;
                bus.post(new HighlightTurn(turn, true));
            }
        }

        // undo the previous highlight, if there is one
        private synchronized void postUnhighlight() {
            if (lastHighlight != null) {
                bus.post(new HighlightTurn(lastHighlight, false));
                lastHighlight = null;
            }
        }
    }

    private void finished(GameStatus status) {
        // TODO add a label that only pops up when the game ends. Maybe below the two turn labels?
        turnLabel.setText(" Game Over: " + status.getFinishedMessage() + " ");
        turnLabel.setToolTipText("Game Over: " + status.getFinishedMessage());
    }
}
