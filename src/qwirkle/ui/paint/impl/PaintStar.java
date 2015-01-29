package qwirkle.ui.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirklePiecePainter;
import qwirkle.ui.paint.QwirkleShapePainter;

import java.awt.*;

public class PaintStar implements QwirkleShapePainter {
    private final int points;
    private double r1, r2;
    private QwirkleShape shape;

    public PaintStar(int points, QwirkleShape shape) {
        this.points = points;
        this.shape = shape;
        r1 = QwirklePiecePainter.FILL_FRACTION * QwirklePiecePainter.WIDTH * 0.5;
        r2 = r1 * 2.7;
    }

    @Override
    public QwirkleShape getShape() {
        return shape;
    }

    @Override
    public void paint(Graphics2D g) {
        double d = Math.PI * 2 / points;
        double d2 = d / 2;
        int r = QwirklePiecePainter.WIDTH / 2;
        int[] xs = new int[points * 2], ys = new int[points * 2];
        for (int i = 0; i < points; ++i) {
            double a = d * i;
            xs[i * 2] = (int) (Math.cos(a) * r1) + r;
            ys[i * 2] = (int) (Math.sin(a) * r1) + r;
            xs[i * 2 + 1] = (int) (Math.cos(a + d2) * r2) + r;
            ys[i * 2 + 1] = (int) (Math.sin(a + d2) * r2) + r;
        }

        g.fillPolygon(xs, ys, xs.length);
    }
}
