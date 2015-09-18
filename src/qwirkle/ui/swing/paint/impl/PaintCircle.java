package qwirkle.ui.swing.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.swing.paint.QwirkleShapePainter;

import java.awt.*;

import static qwirkle.ui.swing.paint.QwirklePiecePainter.*;

public class PaintCircle implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.circle;
    }

    @Override
    public void paint(Graphics2D g) {
        // area = pi r^2 = fraction * width * height
        double radius = Math.sqrt(FILL_FRACTION * WIDTH * HEIGHT / Math.PI);
        g.fillOval((int) ((WIDTH / 2) - radius), (int) ((HEIGHT / 2) - radius),
                (int) (radius * 2), (int) (radius * 2));
    }
}
