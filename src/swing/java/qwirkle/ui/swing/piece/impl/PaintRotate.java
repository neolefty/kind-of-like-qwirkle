package qwirkle.ui.swing.piece.impl;

import qwirkle.game.base.QwirkleShape;
import qwirkle.ui.swing.piece.QwirklePiecePainter;
import qwirkle.ui.swing.piece.QwirkleShapePainter;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class PaintRotate implements QwirkleShapePainter {
    private QwirkleShape shape;
    private QwirkleShapePainter painter;
    private double rotation;

    public PaintRotate(QwirkleShape shape, QwirkleShapePainter painter, double rotation) {
        this.shape = shape;
        this.painter = painter;
        this.rotation = rotation;
    }

    @Override
    public QwirkleShape getShape() {
        return shape;
    }

    @Override
    public void paint(Graphics2D g) {
        AffineTransform old = g.getTransform();
        // rotate around the center of our rectangle
        g.translate(QwirklePiecePainter.WIDTH / 2, QwirklePiecePainter.HEIGHT / 2);
        g.rotate(rotation);
        g.translate(-QwirklePiecePainter.WIDTH / 2, -QwirklePiecePainter.HEIGHT / 2);
        painter.paint(g);
        g.setTransform(old);
    }
}
