package qwirkle.ui.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirkleShapePainter;

import java.awt.Graphics2D;

public class PaintHeart2 implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.heart;
    }

    @Override
    public void paint(Graphics2D g) {
        g.drawOval(20, 30, 30, 30);
        g.drawOval(50, 30, 30, 30);
        int[] xs = { 25, 75, 50 };
        int[] ys = { 50, 50, 60 };
        g.drawPolygon(xs, ys, 3);
    }
}
