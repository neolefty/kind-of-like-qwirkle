package qwirkle.game.base.impl;

import qwirkle.game.base.QwirkleGrid;
import qwirkle.game.base.QwirkleLocation;

import java.util.Iterator;

public class GridWalkerImpl implements QwirkleGrid.GridWalker {
    private int padding;
    private QwirkleGrid grid;
    private boolean horizontal;

    /** Padding is width of empty space around outside. */
    public GridWalkerImpl(QwirkleGrid grid, int padding, boolean horizontal) {
        this.grid = grid;
        this.padding = padding;
        this.horizontal = horizontal;
    }

    @Override
    public Iterator<QwirkleGrid.LineWalker> iterator() {
        return new Griderator(grid, horizontal);
    }

    private class Griderator implements Iterator<QwirkleGrid.LineWalker> {
        private boolean horizontal;
        private int xmin, ymin, xmax, ymax;
        private int cur;

        Griderator(QwirkleGrid grid, boolean horizontal) {
            this.horizontal = horizontal;
            xmin = grid.getXMin();
            xmax = grid.getXMax();
            ymin = grid.getYMin();
            ymax = grid.getYMax();
            cur = (horizontal ? ymin : xmin) - (padding + 1);
        }

        @Override
        public boolean hasNext() {
            return cur < (horizontal ? ymax : xmax) + padding;
        }

        @Override
        public QwirkleGrid.LineWalker next() {
            return new Griderator.LineWalkerImpl();
        }

        @Override public void remove() { throw new UnsupportedOperationException(); }

        private class LineWalkerImpl implements QwirkleGrid.LineWalker {
            @Override
            public Iterator<QwirkleLocation> iterator() {
                if (horizontal)
                    return new Griderator.Linerator(xmin, xmax, ++cur);
                else
                    return new Griderator.Linerator(ymin, ymax, ++cur);
            }
        }

        private class Linerator implements Iterator<QwirkleLocation> {
            private int max;
            private int line; // the x coord if this is vertical / y coord if horizontal
            private int location;

            private Linerator(int min, int max, int line) {
                this.max = max;
                this.line = line;
                this.location = min - padding - 1;
            }

            @Override
            public boolean hasNext() {
                return location < max + padding;
            }

            @Override
            public QwirkleLocation next() {
                if (horizontal)
                    return new QwirkleLocation(++location, line);
                else
                    return new QwirkleLocation(line, ++location);
            }

            @Override public void remove() { throw new UnsupportedOperationException(); }
        }
    }
}
