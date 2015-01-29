package qwirkle.ui.paint.impl;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirkleShapePainter;

import java.awt.*;

import static qwirkle.ui.paint.QwirklePiecePainter.FILL_FRACTION;
import static qwirkle.ui.paint.QwirklePiecePainter.HEIGHT;
import static qwirkle.ui.paint.QwirklePiecePainter.WIDTH;

public class PaintSquare implements QwirkleShapePainter {
    @Override
    public QwirkleShape getShape() {
        return QwirkleShape.square;
    }

    @Override
    public void paint(Graphics2D g) {
        double width = Math.sqrt(HEIGHT * WIDTH * FILL_FRACTION);
        int left = (int) ((WIDTH - width) / 2);
        g.fillRect(left, left, (int) width, (int) width);
    }
}
