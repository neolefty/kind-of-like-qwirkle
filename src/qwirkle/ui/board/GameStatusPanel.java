package qwirkle.ui.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameManager;
import qwirkle.control.GameStatus;
import qwirkle.control.event.GameOver;
import qwirkle.control.event.GameStarted;
import qwirkle.control.event.HighlightTurn;
import qwirkle.game.QwirkleTurn;
import qwirkle.ui.swing.HighlightLabel;

import javax.swing.*;

/** Show the status messages from a game. */
public class GameStatusPanel extends Box {
    // TODO show scrolling messages "joe plays 3 pieces for 6 points ... bob wins with 192 points ... repeat"
    // Show the status of the current turn and the overall game
    private HighlightLabel turnLabel, gameLabel;
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

        // TODO reorganize color constants into their own place, and refer to them there from everywhere
        turnLabel = new HighlightLabel(this, 0.025, QwirklePiecePanel.MOUSE_HL,
                lastHL.createHighlighter(true), lastHL.createHighlighter(false));
        gameLabel = new HighlightLabel(this, 0.025, QwirklePiecePanel.MOUSE_HL,
                bestHL.createHighlighter(true), bestHL.createHighlighter(false));
        add(turnLabel, Box.LEFT_ALIGNMENT);
        add(Box.createGlue()); // fill space between labels
        add(gameLabel, Box.RIGHT_ALIGNMENT);
        mgr.getEventBus().register(new Object() {
            @Subscribe
            public void gameOver(GameOver gameOver) {
                finished(gameOver.getStatus());
            }
            @Subscribe public void gameStarted(GameStarted started) {
                bestTurn = null;
                lastTurn = null;
//                gameLabel.setText("");
            }
            @Subscribe public void status(GameStatus status) {
                if (status.isFinished()) {
                    finished(status);
                }
                else if (status.getAnnotatedGame() != null
                        && status.getAnnotatedGame().getBestTurn() != null) {
                    bestTurn = status.getAnnotatedGame().getBestTurn();
                    gameLabel.setText("Best so far: " + bestTurn.getSummary());
                }
            }
            @Subscribe public void turn(QwirkleTurn turn) {
                lastTurn = turn;
                turnLabel.setText(turn.getSummary());
            }
        });
    }

    /** Fetch a particular turn. */
    private interface TurnGetter {
        QwirkleTurn getTurn();
    }

    /** Creates runnables to highlight turns. */
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
        gameLabel.setText("Game Over: " + status.getFinishedMessage());
    }
}
