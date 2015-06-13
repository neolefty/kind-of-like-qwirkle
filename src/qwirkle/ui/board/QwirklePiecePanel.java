package qwirkle.ui.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.control.event.HighlightTurn;
import qwirkle.game.*;
import qwirkle.ui.paint.QwirklePiecePainter;
import qwirkle.ui.swing.MouseSensitivePanel;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.geom.AffineTransform;

import static qwirkle.ui.main.SwingMain.Colors.*;

public class QwirklePiecePanel extends MouseSensitivePanel implements HasQwirkleLocation {
    private static final QwirklePiecePainter painter = new QwirklePiecePainter();

    private QwirklePiece piece;
    private QwirkleLocation location;

    public QwirklePiecePanel(EventBus bus, QwirkleGrid grid, int x, int y) {
        this(bus, grid, x, y, false);
    }

    public QwirklePiecePanel(EventBus bus, QwirkleGrid grid, int x, int y, boolean highlight) {
        this(bus, grid, new QwirkleLocation(x, y), highlight);
    }

    public QwirklePiecePanel(final EventBus bus, QwirkleGrid grid, QwirkleLocation location, boolean highlight) {
        this(bus, location, highlight);
        piece = grid.get(location);
        if (piece != null)
            setToolTipText(new QwirklePlacement(piece, location).toString());
    }

    public QwirklePiecePanel(EventBus bus, QwirklePlacement place, boolean highlight) {
        this(bus, place.getLocation(), highlight);
        this.piece = place.getPiece();
        setToolTipText(piece.toString());
    }

    /** The real constructor (all others call it). */
    public QwirklePiecePanel(EventBus bus, QwirkleLocation location, boolean highlight) {
        super(highlight ? BG_HL : BG, highlight ? MOUSE_HL : MOUSE, highlight ? CLICK_HL : CLICK);
        this.location = location;
        setToolTipText(location.toString());
        initEvents(bus);
    }

    private void initEvents(final EventBus bus) {
        if (bus != null) {
            // question: why do we register and deregister when hidden and shown?
            // answer: it's a hack, but it's the best I could figure out. The only
            // event I can find that you receive when you are removed from a container
            // is also fired when you are hidden from view (like, when the idle screen activates).
            // Plus, it works.
            addAncestorListener(new AncestorListener() {
                // be sure to unsubscribe when we're discarded
                @Override
                public void ancestorRemoved(AncestorEvent event) {
                    try {
                        bus.unregister(QwirklePiecePanel.this);
                    } catch(IllegalArgumentException ignored) {} // sometimes double-removed because of events
                }

                @Override public void ancestorAdded(AncestorEvent event) {
                    bus.register(QwirklePiecePanel.this);
                }

                @Override public void ancestorMoved(AncestorEvent event) { }
            });
        }
    }

    @Subscribe public void highlight(HighlightTurn hl) {
        if (hl.getTurn().containsLocation(location))
            setHighlighted(hl.isHighlighted());
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (piece != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform t = g2.getTransform();
            double xScale = getWidth() / 100., yScale = getHeight() / 100.;
            g2.scale(xScale, yScale);
            painter.paint((Graphics2D) g, new QwirklePlacement(piece, location));
            g2.setTransform(t);
        }
    }

    @Override
    public QwirkleLocation getQwirkleLocation() {
        return location;
    }

    @Override
    public String toString() {
        return location + ": " + (piece == null ? "blank" : piece);
    }
}
