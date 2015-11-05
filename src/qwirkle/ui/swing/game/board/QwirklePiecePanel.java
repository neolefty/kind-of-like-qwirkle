package qwirkle.ui.swing.game.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.event.DragPiece;
import qwirkle.event.HighlightTurn;
import qwirkle.event.PassOver;
import qwirkle.event.PlayPiece;
import qwirkle.game.*;
import qwirkle.ui.QwirklePieceDisplay;
import qwirkle.ui.swing.colors.ColorSets;
import qwirkle.ui.swing.main.UIConstants;
import qwirkle.ui.swing.paint.QwirklePiecePainter;
import qwirkle.ui.swing.util.DragHelper;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

// TODO allow cancelling a re-pick -- that is, if you pick up a play from the board, allow putting it back in the same spot.
public class QwirklePiecePanel extends JPanel implements HasQwirkleLocation, QwirklePieceDisplay {
    private static final QwirklePiecePainter painter = new QwirklePiecePainter();

    private QwirklePiece piece;
    private QwirkleLocation location;
    private EventBus bus;
    private DragHelper dragHelper;
    private QwirkleGrid grid;

    private BackgroundManager bgMgr;

    public QwirklePiecePanel(EventBus bus, QwirkleGrid grid, int x, int y) {
        this(bus, grid, x, y, false);
    }

    public QwirklePiecePanel(EventBus bus, QwirkleGrid grid, int x, int y, boolean highlight) {
        this(bus, grid, new QwirkleLocation(x, y), highlight);
    }

    /** Create a QwirklePiecePanel.
     *  @param bus The EventBus to post {@link DragPiece} events to. Can be null if this won't post drag events. */
    public QwirklePiecePanel(final EventBus bus, final QwirkleGrid grid, final QwirkleLocation location, boolean highlight) {
        bgMgr = new BackgroundManager(this, highlight ? ColorSets.BG_HIGHLIGHT : ColorSets.BG_NORMAL);

        this.location = location;
        this.piece = grid.get(location);
        this.bus = bus;
        this.grid = grid;
        initEvents();

        if (piece != null)
            //noinspection ConstantConditions
            setToolTipText(UIConstants.PRODUCTION
                    ? piece.toString()
                    : new QwirklePlacement(piece, location).toString());
        else
            //noinspection ConstantConditions
            setToolTipText(UIConstants.PRODUCTION
                    ? "empty space"
                    : "empty space at " + location.toString());
    }

    private void initEvents() {
        // unsubscribe from bus on destruction
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

        // post PassOver events
        if (bus != null) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    bus.post(new PassOver(QwirklePiecePanel.this, true));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    bus.post(new PassOver(QwirklePiecePanel.this, false));
                }
            });
        }
    }

    public BackgroundManager getBackgroundManager() { return bgMgr; }

    /** Highlight this piece if it is part of the {@link HighlightTurn}. */
    @Subscribe public void highlight(HighlightTurn hl) {
        // we can tell based on location
        if (hl.getTurn().containsLocation(location))
            bgMgr.setHighlighted(hl.isHighlighted());
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (piece != null && !isDragging()) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform t = g2.getTransform();
            double xScale = getWidth() / 100., yScale = getHeight() / 100.;
            g2.scale(xScale, yScale);
            painter.paint((Graphics2D) g, new QwirklePlacement(piece, location));
            g2.setTransform(t);
        }
    }

    // implement QwirklePieceDisplay
    @Override public QwirkleLocation getQwirkleLocation() { return location; }
    @Override public QwirklePiece getPiece() { return piece; }
    @Override public int getPieceHeight() { return getHeight(); }
    @Override public int getPieceWidth() { return getWidth(); }

    @Override
    public String toString() { return location + ": " + (piece == null ? "blank" : piece); }

    private boolean isDragging() {
        return dragHelper != null && dragHelper.isDragging();
    }

    public boolean isDraggable() {
        return dragHelper != null && dragHelper.isDraggable();
    }

    /** Start listening for drag events and assume they're actions of <tt>player</tt>.
     *  @param player the player who will be doing the dragging
     *  @param context is this piece part of a hypothetical play that might be canceled, or what?
     *                      (null if no context) */
    public void makeDraggable(AsyncPlayer player, PlayPiece context) {
        if (dragHelper == null)
            initDragHelper(player, context);
        else
            dragHelper.makeDraggable(player);
    }

    public void makeUndraggable() {
        if (dragHelper != null)
            dragHelper.makeUndraggable();
    }

    private void initDragHelper(final AsyncPlayer player, final PlayPiece context) {
        if (context != null && !context.isAccept())
            throw new IllegalArgumentException("Unsupported state: " + context);
        dragHelper = new DragHelper(this, new DragHelper.DragHandler() {
            DragPiece event;

            @Override
            public void startDrag(MouseEvent e) {
                if (context != null) {
                    if (context.isAccept()) {
                        bus.post(context.unpropose());
                    }
                }
                event = post(DragPiece.createPickup(player, grid, location));
            }

            @Override
            public void keepDragging(MouseEvent e) {
                if (event != null)
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

            private DragPiece post(DragPiece event) {
                bus.post(event);
                return event;
            }
        });
    }
}
