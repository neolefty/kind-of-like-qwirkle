package qwirkle.ui.paint;

import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;
import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.impl.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/** Paint a qwirkle piece. */
public class QwirklePiecePainter {
    /** The dimensions of the area we're responsible to paint. */
    public static final int HEIGHT = 100, WIDTH = 100;

    /** The amount of the square to fill with ink when we draw a shape (approximate). */
    public static double FILL_FRACTION = 0.3;

    private Map<QwirkleShape, QwirkleShapePainter> shapePainters = new HashMap<>();

    public QwirklePiecePainter() {
        register(new PaintCircle());
        register(new PaintSquare());
        register(new PaintAnotherStar());
        register(new PaintRotate(QwirkleShape.diamond, new PaintSquare(), Math.PI / 4));
        register(new PaintStar(8, QwirkleShape.star8));
        register(new PaintStar(4, QwirkleShape.star4));
        register(new PaintStar(3, QwirkleShape.triangle));
        register(new PaintFlower());
        register(new PaintSmiley());
        register(new PaintA());
        register(new PaintGaga());
        register(new PaintHeart());
        register(new PaintStar5());
        register(new PaintButterfly());
    }

    private void register(QwirkleShapePainter painter) {
        shapePainters.put(painter.getShape(), painter);
    }

    public void paint(Graphics2D g, QwirklePlacement placement) {
        paint(g, placement.getPiece());
    }
    /** Paint in the space from (0,0) to (99,99). */
    public void paint(Graphics2D g, QwirklePiece piece) {
        Color oldColor = g.getColor();
        Stroke oldStroke = g.getStroke();

        // colored shape
        g.setColor(piece.getColor().getColor());
        pickPainter(piece.getShape()).paint(g);

        // paint coordinates
//        g.setColor(Color.GRAY);
//        g.translate(0, 20);
//        new PaintToString(placement.getLocation()).paint(g);
//        g.translate(0, -20);

        g.setStroke(oldStroke);
        g.setColor(oldColor);
    }

    public QwirkleShapePainter pickPainter(QwirkleShape shape) {
        QwirkleShapePainter result = shapePainters.get(shape);
        if (result == null)
            result = new PaintToString(shape);
        return result;
    }
}
