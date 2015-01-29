package qwirkle.ui.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirklePiecePainter;
import qwirkle.ui.paint.QwirkleShapePainter;

import java.awt.*;
import java.awt.geom.AffineTransform;

/** A circle of circles, filled in in the middle. */
public class PaintFlower implements QwirkleShapePainter {
    // radius of small circles
    private double r1 = QwirklePiecePainter.FILL_FRACTION * QwirklePiecePainter.WIDTH * 0.47;
    // radius of circle where small circles are centered
    private double r2 = r1 * 1.3;

    private int petals = 4;
    private QwirkleShape shape = QwirkleShape.flower;

    public PaintFlower() { }

    public PaintFlower(int petals, QwirkleShape shape) {
        this.petals = petals;
        this.shape = shape;
    }

    public QwirkleShape getShape() { return shape; }

    @Override
    public void paint(Graphics2D g) {
        AffineTransform trans = g.getTransform();
        g.translate(QwirklePiecePainter.WIDTH / 2, QwirklePiecePainter.HEIGHT / 2);
        for (int i = 0; i < petals; ++i) {
            double a = Math.PI / petals + i * (2 * Math.PI / petals);
            double cx = Math.cos(a) * r2, cy = Math.sin(a) * r2;
            g.fillOval((int) (cx - r1), (int) (cy - r1), (int) (r1 + r1), (int) (r1 + r1));
        }
        g.fillOval((int) -r2, (int) -r2, (int) (r2 + r2), (int) (r2 + r2));
        g.setTransform(trans);
    }
}
