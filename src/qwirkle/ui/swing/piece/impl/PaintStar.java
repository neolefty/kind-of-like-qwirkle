package qwirkle.ui.swing.piece.impl;

import qwirkle.game.base.QwirkleShape;
import qwirkle.ui.swing.piece.QwirklePiecePainter;
import qwirkle.ui.swing.piece.QwirkleShapePainter;

import java.awt.*;

public class PaintStar implements QwirkleShapePainter {
    public static final double DEFAULT_OUTER_RADIUS = 2.7;

    private final int points;
    private double r1, r2;
    private QwirkleShape shape;

    public PaintStar(int points, QwirkleShape shape) {
        this(points, shape, DEFAULT_OUTER_RADIUS);
    }

    public PaintStar(int points, QwirkleShape shape, double outerRadius) {
        this.points = points;
        this.shape = shape;
        r1 = QwirklePiecePainter.FILL_FRACTION * QwirklePiecePainter.WIDTH * 0.5
                // normalize to fill fraction
                * DEFAULT_OUTER_RADIUS / outerRadius;
        r2 = r1 * outerRadius;
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
        // start at 0 (left center) for even-numbered stars, top for odd-numbered stars.
        double start = points % 2 == 0 ? 0 : Math.PI / 2;
        for (int i = 0; i < points; ++i) {
            double a = d * i + start;
            xs[i * 2] = (int) (Math.cos(a) * r1) + r;
            ys[i * 2] = (int) (Math.sin(a) * r1) + r;
            xs[i * 2 + 1] = (int) (Math.cos(a + d2) * r2) + r;
            ys[i * 2 + 1] = (int) (Math.sin(a + d2) * r2) + r;
        }

        g.fillPolygon(xs, ys, xs.length);
    }
}
