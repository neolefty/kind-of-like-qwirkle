package qwirkle.ui.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirkleShapePainter;

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
        int[] xs = { 23, 50, 77, 50 };
        int[] ys = { 44, 35, 44, 80 };
        g.fillPolygon(xs, ys, 4);
    }
}

// SQL example:
// SELECT name, grade FROM student WHERE grade < 3 AND grade > 2

// C#
// JavaScript
// HTML / HTML5