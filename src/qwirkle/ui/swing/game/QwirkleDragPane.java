package qwirkle.ui.swing.game;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.ui.event.PassOver;
import qwirkle.ui.event.DragPiece;
import qwirkle.game.base.QwirklePiece;
import qwirkle.ui.swing.paint.QwirklePiecePainter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/** Supports dragging {@link QwirklePiece}s around a UI. */
public class QwirkleDragPane extends JComponent {
    private DragPiece pickup;
    private PassOver passOver;

    public QwirkleDragPane(EventBus bus) {
        bus.register(this);
        setOpaque(false);
    }

    @Subscribe
    public void dragEvent(DragPiece event) {
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

    @Subscribe
    public void passOver(PassOver event) {
        this.passOver = event;
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
                // 1 setup
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AffineTransform t = g2.getTransform();

                // 2 transform
                g2.translate(mouse.x, mouse.y);
                g2.scale(getPieceWidth() / 100, getPieceHeight() / 100);
                g2.translate(-50, -50); // mouse at center of shape

                // 3 paint
                new QwirklePiecePainter().paint(g2, pickup.getSourcePlacement());

                // 4 cleanup
                g2.setTransform(t);
            }
        }
    }

    private double getPieceWidth() {
        return passOver == null ? getDefaultPieceSize() : passOver.getDisplay().getPieceWidth();
    }

    private double getPieceHeight() {
        return passOver == null ? getDefaultPieceSize() : passOver.getDisplay().getPieceHeight();
    }

    private double getDefaultPieceSize() {
        return Math.sqrt(getHeight() * getWidth()) / 10;
    }
}
