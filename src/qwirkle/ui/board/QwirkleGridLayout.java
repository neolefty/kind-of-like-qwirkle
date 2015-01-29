package qwirkle.ui.board;

import qwirkle.game.HasQwirkleLocation;
import qwirkle.game.QwirkleGrid;
import qwirkle.game.QwirkleLocation;

import java.awt.*;

public class QwirkleGridLayout implements LayoutManager2 {
    private QwirkleGrid grid;
    private int margin;
//    private Set<HasQwirkleLocation> tiles = new HashSet<>();

    public QwirkleGridLayout() {}

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            if (grid != null) {
                int xMin = grid.getXMin() - getMargin(), yMin = grid.getYMin() - getMargin();
                Dimension fitInside = getFitInside(parent);
                int square = getSquareSize(fitInside, true);
                int xMargin = (parent.getWidth() - square * getNx()) / 2,
                        yMargin = (parent.getHeight() - square * getNy()) / 2;
//                for (HasQwirkleLocation tile : tiles) {
//                    Component comp = (Component) tile;
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

    /** Take parent's size, insets, and this' maxSize into account to
     *  find a max size to fit this' contents into. */
    private Dimension getFitInside(Container parent) {
        Insets pInsets = parent.getInsets();
        Dimension pSize = parent.getSize();
        // subtract insets
        Dimension result = new Dimension
                (pSize.width - pInsets.left - pInsets.right,
                pSize.height - pInsets.top - pInsets.bottom);
        return result;
    }

    /** Note: call layoutContainer separately. */
    public void setGrid(QwirkleGrid grid) {
        this.grid = grid;
    }

    // TODO remove unnecessary setMaxSize stuff starting in SwingDemo's component resize listener
    /** We're fine with whatever we're given, down to a minimum. */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        // what was I thinking using grandparent?
        //        Container gp = parent.getParent();
        //        Dimension result = fit((gp == null ? parent : gp).getSize());

        // we're fine with whatever we're given, down to a minimum
        Dimension pSize = parent.getSize();

        // leave enough space to draw recognizable icons
        Dimension min = minimumLayoutSize(parent);

        // if a minimum was set, then use the larger of the two minimums
        Dimension setmin = parent.getMinimumSize();
        if (setmin != null)
            min = new Dimension(Math.max(min.width, setmin.width), Math.max(min.height, setmin.height));

        // if the container is big enough, go with it; otherwise, return the minimum
        if (pSize == null || min.getHeight() > pSize.getHeight() || min.getWidth() > pSize.getWidth())
            return min;
        else
            return pSize;
//        System.out.println("Preferred size: " + result);
    }

    /** A size that allows a certain minimum for each square. */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        int square = 30;
        return new Dimension(getNx() * square, getNy() * square);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }

    @Override public float getLayoutAlignmentX(Container target) { return 0.5f; }
    @Override public float getLayoutAlignmentY(Container target) { return 0.5f; }

    @Override public void addLayoutComponent(Component comp, Object constraints) {
//        tiles.add((HasQwirkleLocation) comp);
    }

    @Override public void addLayoutComponent(String name, Component comp) {
//        tiles.add((HasQwirkleLocation) comp);
    }

    @Override public void removeLayoutComponent(Component comp) {
        //noinspection SuspiciousMethodCalls
//        tiles.remove(comp); // okay that it's the wrong type
    }

    @Override public void invalidateLayout(Container target) { }

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
