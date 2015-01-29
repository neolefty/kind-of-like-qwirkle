package qwirkle.ui.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirkleShapePainter;

import java.awt.Graphics2D;

public class PaintGaga implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.gaga;
    }

    @Override
    public void paint(Graphics2D g) {
        int[] xs = { 50, 20, 50, 50, 80, 50 };
        int[] ys = { 0, 60, 60, 100, 40, 40 };
        g.fillPolygon(xs, ys, 6);
    }
}
