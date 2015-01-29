package qwirkle.ui.paint;

import qwirkle.game.QwirkleShape;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static qwirkle.ui.paint.QwirklePiecePainter.HEIGHT;
import static qwirkle.ui.paint.QwirklePiecePainter.WIDTH;

public class PaintToString implements QwirkleShapePainter {
    private Object thing;

    public PaintToString(Object thing) {
        this.thing = thing;
    }

    /** Paints anything. */
    @Override
    public QwirkleShape getShape() {
        return null;
    }

    @Override
    public void paint(Graphics2D g) {
        String s = thing.toString();
        Rectangle2D bounds = g.getFontMetrics().getStringBounds(s, g);
        float x = (float) ((WIDTH - bounds.getWidth()) / 2),
                y = (float) ((HEIGHT - bounds.getHeight()) / 2);
        g.drawString(s, x, y);
    }
}
