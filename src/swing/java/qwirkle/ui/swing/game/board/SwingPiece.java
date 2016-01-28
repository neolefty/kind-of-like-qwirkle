package qwirkle.ui.swing.game.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.*;
import qwirkle.ui.view.BackgroundManager;
import qwirkle.ui.view.QwirkleGridDisplay;
import qwirkle.ui.view.QwirklePieceDisplay;
import qwirkle.ui.control.SelfDisposingEventSubscriber;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.event.HighlightTurn;
import qwirkle.ui.event.PassOver;
import qwirkle.ui.view.colors.ColorSets;
import qwirkle.ui.swing.util.SwingPlatformAttacher;
import qwirkle.ui.UIConstants;
import qwirkle.ui.swing.piece.QwirklePiecePainter;
import qwirkle.ui.swing.util.SwingDragHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

/** A JPanel that displays a single Qwirkle piece. */
public class SwingPiece extends JPanel implements QwirklePieceDisplay {
    private static final QwirklePiecePainter painter = new QwirklePiecePainter();

    private QwirklePiece piece;
    private QwirkleLocation location;
    private EventBus bus;
    private SwingDragHelper dragHelper;
    private QwirkleGridDisplay parent;

    private BackgroundManager bgMgr;

    public SwingPiece(EventBus bus, QwirkleGridDisplay parent, int x, int y, boolean highlight) {
        this(bus, parent, new QwirkleLocation(x, y), highlight);
    }

    /** Create a QwirklePiecePanel.
     *  @param bus The EventBus to post {@link DragPiece} events to. Can be null if this won't post drag events. */
    public SwingPiece(EventBus bus, QwirkleGridDisplay parent, QwirkleLocation location, boolean highlight) {
        bgMgr = new BackgroundManager(this, highlight ? ColorSets.BG_HIGHLIGHT : ColorSets.BG_NORMAL);
        setOpaque(true);

        this.location = location;
        this.parent = parent;
        this.piece = (parent.getGrid() == null ? null : parent.getGrid().get(location));
        this.bus = bus;
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
        if (bus != null) {
            new SelfDisposingEventSubscriber(bus, new SwingPlatformAttacher(this)) {
                /** Highlight this piece if it is part of the {@link HighlightTurn}. */
                @Subscribe public void highlight(HighlightTurn hl) {
                    // we can tell based on location
                    if (getDisplay().getDisplayType() == hl.getType() && hl.getTurn().containsLocation(location))
                        bgMgr.setHighlighted(hl.isHighlighted());
                }
            };

            // post PassOver events & alert background manager
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    bgMgr.setHover(true);
                    bus.post(new PassOver(SwingPiece.this, true));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    bgMgr.setHover(false);
                    bus.post(new PassOver(SwingPiece.this, false));
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    bgMgr.setMousePressed(true);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    bgMgr.setMousePressed(false);
                }
            });
        }
    }

    @Override
    public void setBackground(QwirkleColor background) {
        setBackground(background == null ? null : new Color(background.getColorInt()));
    }

    public BackgroundManager getBackgroundManager() { return bgMgr; }

    public QwirkleGrid getGrid() { return parent.getGrid(); }

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
    @Override public QwirkleGridDisplay getDisplay() { return parent; }
    @Override public QwirklePlacement getPlacement() { return getGrid().getPlacement(location); }

    @Override
    public String toString() { return location + ": " + (piece == null ? "blank" : piece); }

    private boolean isDragging() {
        return dragHelper != null && dragHelper.isDragging();
    }

    public boolean isDraggable() {
        return dragHelper != null && dragHelper.isDraggable();
    }

    /** Start listening for drag events. */
    public void makeDraggable(QwirklePlayer currentPlayer) {
        if (dragHelper == null)
            dragHelper = new SwingDragHelper(this, currentPlayer, bus, this);
        else
            dragHelper.setDraggable(true);
    }

    /** Stop listening for drag events. */
    public void makeUndraggable() {
        if (dragHelper != null)
            dragHelper.setDraggable(false);
    }
}
