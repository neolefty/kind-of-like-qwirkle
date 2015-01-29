package qwirkle.ui.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirkleShapePainter;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class PaintSmiley implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.smiley;
    }

    @Override
    public void paint(Graphics2D g) {
        Stroke oldStroke = g.getStroke();
        BasicStroke stroke = new BasicStroke(6);
        g.setStroke(stroke);
        g.drawOval(15, 15, 70, 70);
        g.drawArc(30, 30, 40, 40, 210, 120);
        g.drawLine(38, 33, 38, 45);
        g.drawLine(62, 33, 62, 45);
        g.setStroke(oldStroke);
    }
}
