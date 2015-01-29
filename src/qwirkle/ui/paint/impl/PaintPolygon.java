package qwirkle.ui.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirkleShapePainter;

import java.awt.Graphics2D;

public class PaintPolygon implements QwirkleShapePainter {
    private QwirkleShape shape;
    private int sides;

    public PaintPolygon(int sides, QwirkleShape shape) {
        this.sides = sides;
        this.shape = shape;
    }

    @Override
    public QwirkleShape getShape() {
        return shape;
    }

    @Override
    public void paint(Graphics2D g) {
        int[] xs = new int[sides], ys = new int[sides];
        // 3: 50, 4:
    }
}
