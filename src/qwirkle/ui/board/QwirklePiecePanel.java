package qwirkle.ui.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.control.event.HighlightTurn;
import qwirkle.control.event.PieceDrag;
import qwirkle.game.*;
import qwirkle.ui.paint.QwirklePiecePainter;
import qwirkle.ui.swing.DragHelper;
import qwirkle.ui.swing.MouseSensitivePanel;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import static qwirkle.ui.main.SwingMain.Colors.*;

public class QwirklePiecePanel extends MouseSensitivePanel implements HasQwirkleLocation, QwirklePieceDisplay {
    private static final QwirklePiecePainter painter = new QwirklePiecePainter();

    private QwirklePiece piece;
    private QwirkleLocation location;
    private EventBus bus;
    private DragHelper dragHelper;
    private QwirkleGrid grid;

    public QwirklePiecePanel(EventBus bus, QwirkleGrid grid, int x, int y) {
        this(bus, grid, x, y, false);
    }

    public QwirklePiecePanel(EventBus bus, QwirkleGrid grid, int x, int y, boolean highlight) {
        this(bus, grid, new QwirkleLocation(x, y), highlight);
    }

    /** Create a QwirklePiecePanel.
     *  @param bus The EventBus to post drag events to. Can be null if this won't post drag events. */
    public QwirklePiecePanel(final EventBus bus, final QwirkleGrid grid, final QwirkleLocation location, boolean highlight) {
        super(highlight ? BG_HL : BG, highlight ? MOUSE_HL : MOUSE, highlight ? CLICK_HL : CLICK);

        this.location = location;
        this.piece = grid.get(location);
        this.bus = bus;
        this.grid = grid;
        initEvents();

        // TODO in production, hide coords -- they are meaningless to users because graphical layout is visible
        if (piece != null)
            setToolTipText(new QwirklePlacement(piece, location).toString());
        else
            setToolTipText(location.toString());
    }

    private void initEvents() {
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

    /** Highlight this piece if it is part of the {@link HighlightTurn}. */
    @Subscribe public void highlight(HighlightTurn hl) {
        // we can tell based on location
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
    public QwirklePiece getPiece() { return piece; }

    @Override
    public String toString() { return location + ": " + (piece == null ? "blank" : piece); }

    public boolean isDraggable() {
        return dragHelper != null && dragHelper.isDraggable();
    }

    public void setDraggable(boolean draggable) {
        if (draggable) { // start listening for drag events
            if (dragHelper == null)
                initDragHelper();
            else
                dragHelper.setDraggable(true);
        }
        else { // stop listening for drag events
            if (dragHelper != null)
                dragHelper.setDraggable(false);
        }
    }

    private void initDragHelper() {
        dragHelper = new DragHelper(this, new DragHelper.DragHandler() {
            PieceDrag event;

            @Override
            public void startDrag(MouseEvent e) {
                event = post(PieceDrag.createPickup(grid, location));
            }

            @Override
            public void keepDragging(MouseEvent e) {
                event = post(event.sustain());
            }

            // TODO change the location to be the destination -- do we need both source & dest?
            @Override
            public void endDrag(MouseEvent e) {
                event = post(event.drop());
            }

            @Override
            public void cancelDrag() {
                post(event.cancel());
                event = null;
            }

            private PieceDrag post(PieceDrag event) {
                bus.post(event);
                return event;
            }
        });
    }
}
