package qwirkle.ui.swing.piece;

import qwirkle.game.base.QwirkleShape;

import java.awt.*;

/** Draw a particular qwirkle shape. */
public interface QwirkleShapePainter {
    /** What shape does this paint? */
    QwirkleShape getShape();
    /** Draw the shape from (0,0) to (99,99) inclusive. */
    void paint(Graphics2D g);
}
