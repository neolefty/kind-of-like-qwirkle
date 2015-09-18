package qwirkle.ui.swing.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.swing.paint.QwirkleShapePainter;

import java.awt.*;

import static qwirkle.ui.swing.paint.QwirklePiecePainter.FILL_FRACTION;
import static qwirkle.ui.swing.paint.QwirklePiecePainter.HEIGHT;
import static qwirkle.ui.swing.paint.QwirklePiecePainter.WIDTH;

public class PaintSquare implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.square;
    }

    @Override
    public void paint(Graphics2D g) {
        double width = Math.sqrt(HEIGHT * WIDTH * FILL_FRACTION);
        int pos = (int) ((WIDTH - width) / 2);
        g.fillRect(pos, pos, (int) width, (int) width);
    }
}
