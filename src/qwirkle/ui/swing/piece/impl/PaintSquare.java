package qwirkle.ui.swing.piece.impl;

import qwirkle.game.base.QwirkleShape;
import qwirkle.ui.swing.piece.QwirkleShapePainter;

import java.awt.*;

import static qwirkle.ui.swing.piece.QwirklePiecePainter.FILL_FRACTION;
import static qwirkle.ui.swing.piece.QwirklePiecePainter.HEIGHT;
import static qwirkle.ui.swing.piece.QwirklePiecePainter.WIDTH;

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
