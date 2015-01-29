package qwirkle.game.impl;

import qwirkle.game.*;

import java.util.*;

/** Basic geometry of a Qwirkle board. For game logic, see subclass. */
public class QwirkleGridImpl implements QwirkleGrid {
    private List<QwirklePlacement> placements = new ArrayList<>();
    private Map<QwirkleLocation, QwirklePlacement> board = new HashMap<>();

    public QwirkleGridImpl(Collection<QwirklePlacement> placements) {
        this.placements = new ArrayList<>(placements);
        for (QwirklePlacement placement : placements)
            board.put(placement.getLocation(), placement);
    }

    /** Create an empty grid. */
    public QwirkleGridImpl() {
        this(new ArrayList<QwirklePlacement>());
    }

    @Override
    public int size() {
        return placements.size();
    }

    public boolean hasPieceAt(QwirkleLocation location) {
        return board.keySet().contains(location);
    }

    @Override
    public QwirklePiece get(QwirkleLocation location) {
        QwirklePlacement p = getPlacement(location);
        return (p == null ? null : p.getPiece());
    }

    @Override
    public QwirklePlacement getPlacement(QwirkleLocation location) {
        return board.get(location);
    }

    @Override
    public QwirklePlacement getPlacement(int x, int y) {
        return getPlacement(new QwirkleLocation(x, y));
    }

    @Override
    public List<QwirklePlacement> getPlacements() {
        return Collections.unmodifiableList(placements);
    }

    @Override
    public QwirkleGrid.GridWalker getHorizontalWalker(int padding) {
        return new GridWalkerImpl(this, padding, true);
    }

    @Override
    public GridWalker getVerticalWalker(int padding) {
        return new GridWalkerImpl(this, padding, false);
    }

    private static class CacheableInt {
        private interface Compute { int compute(); }
        private Integer value = null;
        int get(Compute compute) {
            if (value == null)
                value = compute.compute();
            return value;
        }
    }

    // 0 = x min, 1 = x max, 2 = y min, 3 = y max
    CacheableInt[] minMax = { new CacheableInt(), new CacheableInt(), new CacheableInt(), new CacheableInt() };

    @Override public int getXMin() {
        return minMax[0].get(new CacheableInt.Compute() {
            @Override
            public int compute() {
                return QwirkleGridTools.getXMin(placements);
            }
        });
    }
    @Override public int getXMax() {
        return minMax[1].get(new CacheableInt.Compute() {
            @Override
            public int compute() {
                return QwirkleGridTools.getXMax(placements);
            }
        });
    }
    @Override public int getYMin() {
        return minMax[2].get(new CacheableInt.Compute() {
            @Override public int compute() { return QwirkleGridTools.getYMin(placements); }
        });
    }
    @Override public int getYMax() {
        return minMax[3].get(new CacheableInt.Compute() {
            @Override
            public int compute() {
                return QwirkleGridTools.getYMax(placements);
            }
        });
    }
    @Override public int getWidth() { return getXMax() - getXMin() + 1; }
    @Override public int getHeight() { return getYMax() - getYMin() + 1; }

/*
    @Override
    public boolean isSame(QwirkleGrid that) {
        if (that == null)
            throw new NullPointerException();
        else if (that == this)
            return true;
        else if (that instanceof QwirkleGridImpl)
            return ((QwirkleGridImpl) that).board.equals(board);
        else
            throw new UnsupportedOperationException
                    ("comparison to other implementations of grid is not implemented: "
                            + that.getClass().getName());
    }
*/

    @Override
    public String toString() { return toString(""); }

    @Override
    public String toString(String linePrefix) {
        StringBuilder s = new StringBuilder();
        QwirklePiece sample = new QwirklePiece(QwirkleColor.blue, QwirkleShape.circle);
        String space = "          ".substring(0, sample.getAbbrev().length());
        for (LineWalker line : getHorizontalWalker(0)) {
            s.append(linePrefix);
            for (QwirkleLocation location : line) {
                QwirklePiece piece = get(location);
                String abbrev = piece == null ? space : piece.getAbbrev();
                // highlight the center of the board -- frame (0,0) by putting bars '|' on either side
                String frame = (location.getY() == 0 && (location.getX() == -1 || location.getX() == 0)) ? "|" : " ";
                s.append(abbrev).append(frame);
            }
            s.append("\n");
        }
//        for (QwirkleLine line : lines)
//            s.append("   ").append(line).append("\n");
        return s.toString();
    }
}
