package qwirkle.ui.swing.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.swing.paint.QwirkleShapePainter;

import java.awt.Graphics2D;

public class PaintStar5 implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.star5;
    }

    @Override
    public void paint(Graphics2D g) {
        int[] xs = { 50, 40, 70, 10, 60 },
                ys = {  1, 70, 20, 10, 70 };
        g.fillPolygon(xs, ys, 5);
    }
}
