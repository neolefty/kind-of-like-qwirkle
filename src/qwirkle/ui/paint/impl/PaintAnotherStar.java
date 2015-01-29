package qwirkle.ui.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirkleShapePainter;

import java.awt.Graphics2D;

public class PaintAnotherStar implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.anotherStar;
    }

    @Override
    public void paint(Graphics2D g) {
        int[] xs = { 50, 100, 0, 100, 0 },
                ys = { 0,  100, 25, 25, 100 };
        g.fillPolygon(xs, ys, 5);
    }
}
