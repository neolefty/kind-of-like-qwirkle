package qwirkle.attic.swing;

import com.google.common.eventbus.EventBus;
import qwirkle.game.QwirkleGrid;
import qwirkle.game.QwirkleLocation;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;
import qwirkle.ui.swing.paint.QwirklePiecePainter;
import qwirkle.ui.swing.colors.Colors;

import java.awt.*;
import java.awt.geom.AffineTransform;

/** A QwirkleBoard panel that shows the board as a single monolithic object.
 *  Makes it hard to mouse-y stuff. */
public class BoardMonolithicPanel extends QwirkleGridSubscriberPanel {
    public BoardMonolithicPanel(EventBus bus) {
        super(bus);
    }

    /** Don't need to do anything -- it's all in paint. */
    @Override
    public void onUpdate(QwirkleGrid grid) { }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform oldTransform = g2.getTransform();

        // background - black
        g.setColor(Colors.BG);
        g.fillRect(0, 0, getWidth(), getHeight());

        // transform
//        g2.translate(-200, -200);
//        g2.scale(0.5, 0.5);
        // - board dimensions + 1 padding
        QwirkleGrid grid = getGrid();
        int bxMin = grid.getXMin() - 1, bxMax = grid.getXMax() + 1,
                byMin = grid.getYMin() - 1, byMax = grid.getYMax() + 1,
                bWidth = bxMax - bxMin + 1, bHeight = byMax - byMin + 1;
        double xPerTile = getWidth() / bWidth, yPerTile = getHeight() / bHeight;
        // - scale to height & width
        double xScale = Math.min(xPerTile, yPerTile) / QwirklePiecePainter.WIDTH;
        double yScale = Math.min(xPerTile, yPerTile) / QwirklePiecePainter.HEIGHT;
//        System.out.println("scale = (" + xScale + ", " + yScale + ")");
        g2.scale(xScale, yScale);
        // - translate to lower right (min x, y)
//        g2.translate(-bxMin * QwirklePiecePainter.WIDTH, -byMin * QwirklePiecePainter.HEIGHT);
        AffineTransform lowerRightTransform = g2.getTransform();

        // paint each tile
        QwirklePiecePainter piecePainter = new QwirklePiecePainter();
        for (QwirkleGrid.LineWalker line : grid.getHorizontalWalker(1))
            for (QwirkleLocation location : line) {
                // how many tiles away from the left edge (min x) & bottom edge (min y) are we?
                int xOffset = location.getX() - bxMin, yOffset = location.getY() - byMin;
                g2.setTransform(lowerRightTransform);

                QwirklePiece piece = grid.get(location);
                QwirklePlacement placement = piece == null ? null : new QwirklePlacement(piece, location);

                g2.translate(xOffset * QwirklePiecePainter.WIDTH, yOffset * QwirklePiecePainter.HEIGHT);
//                g2.setStroke(new BasicStroke(3));
//                g2.setColor(piece == null ? Color.GRAY : piece.getColor().getColor());
//                g2.drawRect(3, 3, 97, 97);

                if (piece != null)
                    piecePainter.paint(g2, placement);
            }

//        g2.setTransform(lowerRightTransform);
//        g2.setColor(new Color(200, 100, 50, 50));
//        g2.fillOval(-100, -100, 300, 300);

        // put the transform back the way we found it
        g2.setTransform(oldTransform);
    }
}
