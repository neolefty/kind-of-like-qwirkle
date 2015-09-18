package qwirkle.ui.swing.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.swing.paint.QwirkleShapePainter;

import java.awt.*;

public class PaintButterfly implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.butterfly;
    }

    @Override
    public void paint(Graphics2D g) {
        int bodyW = 14, topDiam = 22, botDiam = 28;

        // top left
        //noinspection SuspiciousNameCombination
        g.fillOval(48-topDiam, 25, topDiam, topDiam);
        // top right
        //noinspection SuspiciousNameCombination
        g.fillOval(52, 25, topDiam, topDiam);
        // bottom left
        g.fillOval(50-botDiam, 42, botDiam, botDiam+3);
        // bottom right
        g.fillOval(50, 42, botDiam, botDiam+3);
        // middle
        g.fillOval(43, 24, bodyW, 55);
    }
}
