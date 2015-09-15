package qwirkle.ui.main;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.control.event.PieceDrag;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;
import qwirkle.ui.board.QwirkleGridDisplay;
import qwirkle.ui.board.QwirklePieceDisplay;
import qwirkle.ui.paint.QwirklePiecePainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

/** Supports dragging {@link QwirklePiece}s around a UI. */
public class QwirkleDragPane extends JComponent {
    private Component under;
    private PieceDrag pickup;

    public QwirkleDragPane(EventBus bus, Component under) {
        this.under = under;
        bus.register(this);
        setOpaque(false);
    }

    @Subscribe
    public void dragEvent(PieceDrag event) {
        if (event.isPickup()) {
            setVisible(true);
            debugln("Picked up: " + event);
            this.pickup = event;
            repaint();
        }
        else if (event.isDrop() || event.isCancel()) {
            debugln("Dropped: " + event);
            this.pickup = null;
            setVisible(false);
        }
        else if (event.isSustain()) {
            debug("=");
            repaint();
        }
        else
            throw new IllegalStateException("Unknown drag event: " + event);
    }

    private static final boolean DEBUG = false;
    private void debug(String s) { if (DEBUG) System.out.print(s); }
    private void debugln(String s) { if (DEBUG) System.out.println(getClass().getSimpleName() + ": " + s); }

    private Point lastMouse = null;

    @Override
    public void paint(Graphics g) {
        if (pickup != null) {
            Point mouse = getMousePosition(true);
            if (mouse == null)
                mouse = lastMouse;
            else
                lastMouse = mouse;

            if (mouse != null) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AffineTransform t = g2.getTransform();
                double size = Math.sqrt(getHeight() * getWidth()) / 10;
                g2.translate(mouse.x, mouse.y);
                double scale = size / 100;
                g2.scale(scale, scale);
                // TODO preserve relative click position within piece?
                g2.translate(-50, -50); // mouse at center of shape
                new QwirklePiecePainter().paint(g2, pickup.getPlacement());
                g2.setTransform(t);
            }
        }
    }

    private QwirklePlacement findPlacement(MouseEvent e) {
        return findPlacement(under, e);
    }

    private QwirklePlacement findPlacement(Component comp, MouseEvent e) {
        QwirklePieceDisplay pd = null;
        QwirklePlacement placement = null;
        Point compPoint = SwingUtilities.convertPoint(this, e.getPoint(), comp);
        if (comp instanceof QwirkleGridDisplay) {
            QwirkleGridDisplay gd = (QwirkleGridDisplay) comp;
            System.out.print("Found grid display -- ");
            pd = gd.getPieceDisplay(compPoint.x, compPoint.y);
        }
        else if (comp instanceof QwirklePieceDisplay) {
            pd = (QwirklePieceDisplay) comp;
            System.out.print("Found piece display -- ");
        }
        else
            System.out.print("Didn't find a Qwirkle UI component: " + comp.getClass().getSimpleName() + " -- ");

        if (pd != null) {
//            MouseEvent compEvent = SwingUtilities.convertMouseEvent(this, e, comp);
//            comp.dispatchEvent(e);

            if (pd.getPiece() != null && pd.getQwirkleLocation() != null)
                placement = new QwirklePlacement(pd.getPiece(), pd.getQwirkleLocation());
            System.out.println(placement);
        }
        else
            System.out.println("no piece");

        return placement;
    }
}
