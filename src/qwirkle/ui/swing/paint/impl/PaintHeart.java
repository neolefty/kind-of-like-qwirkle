package qwirkle.ui.swing.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.swing.paint.QwirkleShapePainter;

import java.awt.Graphics2D;

public class PaintHeart implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.heart;
    }

    @Override
    public void paint(Graphics2D g) {
        g.fillOval(20, 20, 31, 31);
        g.fillOval(49, 20, 31, 31);
//        int[] xs = { 23, 50, 77, 50 };
//        int[] ys = { 44, 35, 44, 80 };
        int[] xs = { 22, 50, 78, 50 };
        int[] ys = { 43, 35, 43, 80 };
        g.fillPolygon(xs, ys, 4);
    }
}
