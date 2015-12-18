package qwirkle.ui.swing.game;

import com.google.common.eventbus.EventBus;
import qwirkle.ui.event.HighlightTurn;
import qwirkle.game.event.TurnCompleted;
import qwirkle.ui.view.QwirkleGridDisplay;
import qwirkle.ui.view.colors.Colors;
import qwirkle.ui.swing.util.HighlightLabel;

import java.awt.*;
import java.util.concurrent.Callable;

// TODO highlight all the pieces relevant to the score, and the played piece with a color?
/** A JLabel that highlights a turn by firing HighlightTurn events. */
public class TurnHighlightingLabel extends HighlightLabel {
    private EventBus bus;

    public TurnHighlightingLabel(EventBus bus, Component parent, double fraction, Callable<TurnCompleted> getter) {
        super(parent, fraction, new Color(Colors.MOUSE_HL.getColorInt()));
        TurnHighlighter highlighter = new TurnHighlighter(getter);
        setHighlightAction(highlighter.createHighlighter(true));
        setUnhighlightAction(highlighter.createHighlighter(false));
        this.bus = bus;
    }

    /** Creates <tt>Callable</tt>s to highlight turns. */
    private class TurnHighlighter {
        private TurnCompleted lastHighlight = null;
        private Callable<TurnCompleted> turnGetter;

        TurnHighlighter(Callable<TurnCompleted> getter) {
            this.turnGetter = getter;
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
                            postHighlight(turnGetter.call());
                        } catch (Exception e) {
                            e.printStackTrace(); // can probably safely ignore this, since it's only cosmetic
                        }
                }
            };
        }

        // highlight something
        private synchronized void postHighlight(TurnCompleted turn) {
            if (turn != null) {
                this.lastHighlight = turn;
                bus.post(new HighlightTurn(turn, QwirkleGridDisplay.DisplayType.gameboard, true));
            }
        }

        // undo the previous highlight, if there is one
        private synchronized void postUnhighlight() {
            if (lastHighlight != null) {
                bus.post(new HighlightTurn(lastHighlight, QwirkleGridDisplay.DisplayType.gameboard, false));
                lastHighlight = null;
            }
        }
    }
}
