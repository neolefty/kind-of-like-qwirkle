package qwirkle.ui.swing.piece;

import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.base.QwirkleShape;
import qwirkle.ui.swing.piece.impl.*;
import qwirkle.ui.view.HasTransparency;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/** Paint a qwirkle piece. */
public class QwirklePiecePainter implements HasTransparency {
    /** The dimensions of the area we're responsible to paint. */
    public static final int HEIGHT = 100, WIDTH = 100;

    /** The amount of the square to fill with ink when we draw a shape (approximate). */
    public static double FILL_FRACTION = 0.3;

    /** How transparent should the shapes be? 0 = opaque, 1 = invisible.
     *  Default 0. */
    private float transparency = 0;

    private Map<QwirkleShape, QwirkleShapePainter> shapePainters = new HashMap<>();

    public QwirklePiecePainter() {
        register(new PaintCircle());
        register(new PaintSquare());
        register(new PaintAnotherStar());
        register(new PaintRotate(QwirkleShape.diamond, new PaintSquare(), Math.PI / 4));
        register(new PaintStar(8, QwirkleShape.star8));
        register(new PaintStar(5, QwirkleShape.star5, 2.3));
        register(new PaintPolygon(5, QwirkleShape.pentagon));
        register(new PaintStar(4, QwirkleShape.star4));
        register(new PaintPolygon(3, QwirkleShape.triangle));
        register(new PaintFlower());
        register(new PaintSmiley());
        register(new PaintA());
        register(new PaintGaga());
        register(new PaintHeart());
//        register(new PaintStar5());
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
        Color c = new Color(piece.getColor().getColorInt());
        if (transparency != 0)
            c = new Color(c.getRed(), c.getGreen(), c.getBlue(), getAlpha());
        g.setColor(c);
        pickPainter(piece.getShape()).paint(g);

        // paint coordinates
//        g.setColor(Color.GRAY);
//        g.translate(0, 20);
//        new PaintToString(placement.getLocation()).paint(g);
//        g.translate(0, -20);

        g.setStroke(oldStroke);
        g.setColor(oldColor);
    }

    private int getAlpha() {
        return Math.max(0, Math.min(255, (int) ((1-transparency) * 256)));
    }

    public QwirkleShapePainter pickPainter(QwirkleShape shape) {
        QwirkleShapePainter result = shapePainters.get(shape);
        if (result == null)
            result = new PaintToString(shape);
        return result;
    }

    public void setTransparency(double transparency) {
        setTransparency((float) transparency);
    }
    public void setTransparency(float transparency) {
        if (transparency < 0 || transparency > 1)
            throw new IllegalArgumentException("Transparency must be 0 to 1 (" + transparency + ")");
        this.transparency = transparency;
    }
}
