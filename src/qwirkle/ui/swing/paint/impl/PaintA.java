package qwirkle.ui.swing.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.swing.paint.QwirkleShapePainter;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;

public class PaintA implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.ay;
    }

    @Override
    public void paint(Graphics2D g) {
        Stroke stroke
                = new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g.setStroke(stroke);

        int[] xs = { 20, 50, 80, 72, 28 };
        int[] ys = { 90, 10, 90, 70, 70 };
        Polygon p = new Polygon(xs, ys, 5);
        g.drawPolygon(p);
//        g.drawPolygon(xs, ys, 5);

//        g.drawLine(50,10,20,90);
//        g.drawLine(50,10,80,90);
//        g.drawLine(28,70,72,70);
    }
}
