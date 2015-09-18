package qwirkle.ui.swing.board;

import qwirkle.game.HasQwirkleLocation;
import qwirkle.game.QwirkleGrid;
import qwirkle.game.QwirkleLocation;
import qwirkle.ui.swing.util.LayoutBase;

import java.awt.*;

public class QwirkleGridLayout extends LayoutBase {
    private QwirkleGrid grid;
    private int margin;

    public QwirkleGridLayout() {}

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            if (grid != null) {
                int xMin = grid.getXMin() - getMargin(), yMin = grid.getYMin() - getMargin();
                int square = getSquareSize(parent);
                int xMargin = (parent.getWidth() - square * getNx()) / 2,
                        yMargin = (parent.getHeight() - square * getNy()) / 2;
                for (int i = 0; i < parent.getComponentCount(); ++i) {
                    Component comp = parent.getComponent(i);
                    HasQwirkleLocation tile = (HasQwirkleLocation) comp;
                    QwirkleLocation ql = tile.getQwirkleLocation();
                    int x = xMargin + (ql.getX() - xMin) * square, y = yMargin + (ql.getY() - yMin) * square;
                    comp.setBounds(x, y, square, square);
                }
            }
        }
    }

    private transient int lastSquareSize = -1;
    private int getSquareSize(Container parent) {
        Dimension fitInside = getFitInside(parent);
        int square = getSquareSize(fitInside, true);
        lastSquareSize = square;
        return square;
    }

    /** How big were the pieces, that last time this was drawn? -1 if this hasn't been drawn yet. */
    public int getPieceSize() {
        return lastSquareSize;
    }

    /** Note: call layoutContainer separately. */
    public void setGrid(QwirkleGrid grid) {
        this.grid = grid;
    }

    /** A size that allows a certain minimum for each square. */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        int square = 30;
        return new Dimension(getNx() * square, getNy() * square);
    }

    /** How big would the board be if we fit it into the size given by dim?
     *  Keeps the pieces square. */
    private Dimension fit(Dimension dim) {
        if (dim != null) {
            if (grid == null)
                return new Dimension(0, 0);
            else {
                int sq = getSquareSize(dim, false);
                return new Dimension(sq * getNx(), sq * getNy());
            }
        }
        else
            return null;
    }

    /** Number of x spaces (columns) to allow for the board. */
    private int getNx() {
        return grid == null ? margin : grid.getWidth() + margin * 2;
    }

    /** Number of y spaces (rows) to allow for the board. */
    private int getNy() {
        return grid == null ? margin : grid.getHeight() + margin * 2;
    }

    private int getSquareSize(Dimension dim, boolean min) {
        int xPerSquare = ((int)dim.getWidth()) / getNx(),
                yPerSquare = ((int)dim.getHeight()) / getNy();
        return min ? Math.min(xPerSquare, yPerSquare) : Math.max(xPerSquare, yPerSquare);
    }

    public int getMargin() { return margin; }
    public void setMargin(int margin) { this.margin = margin; }
}
