package qwirkle.ui.board;

import com.google.common.eventbus.EventBus;
import qwirkle.control.event.HighlightTurn;
import qwirkle.game.QwirkleTurn;
import qwirkle.ui.main.SwingMain;
import qwirkle.ui.swing.HighlightLabel;

import java.awt.*;
import java.util.concurrent.Callable;

/** A JLabel that highlights a turn by firing HighlightTurn events. */
public class TurnHighlightingLabel extends HighlightLabel {
    private EventBus bus;

    public TurnHighlightingLabel(EventBus bus, Component parent, double fraction, Callable<QwirkleTurn> getter) {
        super(parent, fraction, SwingMain.Colors.MOUSE_HL);
        TurnHighlighter highlighter = new TurnHighlighter(getter);
        setHighlightAction(highlighter.createHighlighter(true));
        setUnhighlightAction(highlighter.createHighlighter(false));
        this.bus = bus;
    }

    /** Creates TurnGetters to highlight turns. */
    private class TurnHighlighter {
        private QwirkleTurn lastHighlight = null;
        private Callable<QwirkleTurn> getter;

        TurnHighlighter(Callable<QwirkleTurn> getter) {
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
                        try {
                            postHighlight(getter.call());
                        } catch (Exception e) {
                            e.printStackTrace(); // can probably safely ignore this, since it's only cosmetic
                        }
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
}
