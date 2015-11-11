package qwirkle.players;

import qwirkle.game.*;

import java.util.*;

/** Helper class to {@link RainbowPlayer}. Mostly measures how un-rainbowy a board is.
 *  The idea is to consider the rainbow-ness of a board before and after each of
 *  the moves you are contemplating, to see which one is most harmonious.
 *
 *  <p>Approximates rainbow ordering by starting with the color that is farthest
 *  from its two neighbors, then "going around the ring" picking the nearest neighbors.</p> */
public class Rainbow {
    public static final int DEFAULT_DISLIKE_MONOCHROME = 1;

    Map<QwirkleColor, Integer> indexes = new HashMap<>();
    List<QwirkleColor> rainbow = new ArrayList<>();

    private int dislikeMonochrome = DEFAULT_DISLIKE_MONOCHROME;
    private int dislikeRainbow = 0;
    private int dislikeJumps = 5;

    public Rainbow(QwirkleColor[] colors) { this(Arrays.asList(colors)); }

    public Rainbow(Collection<QwirkleColor> colors) {
        Set<QwirkleColor> remaining = new HashSet<>(colors);
        QwirkleColor x = null;
        int i = 0;
        while (!remaining.isEmpty()) {
            x = (x == null ? pickStart(colors) : nearest(x, remaining));
            rainbow.add(x);
            indexes.put(x, i++);
            remaining.remove(x);
        }
    }

    public int computeRainbowDeviation(QwirkleBoard board) {
        // include padding so that we run off the end of groups to force a calculation
        return deviation(board, board.getHorizontalWalker(1))
                + deviation(board, board.getVerticalWalker(1));
    }

    /** How much do we penalize pieces in a sequence that are the same color,
     *  when it comes to measuring their rainbow deviation?  Default 1. */
    public int getDislikeMonochrome() { return dislikeMonochrome; }
    public void setDislikeMonochrome(int dislikeMonochrome) { this.dislikeMonochrome = dislikeMonochrome; }

    /** How much do we penalize pieces in a sequence that are rainbow neighbors, when
     *  considering their rainbow deviation? Default 0. */
    public int getDislikeRainbow() { return dislikeRainbow; }
    public void setDislikeRainbow(int dislikeRainbow) { this.dislikeRainbow = dislikeRainbow; }

    /** By what factor do we multiply the rainbow distance of pieces in a sequence
     *  that are neither rainbow neighbors nor the same color, when
     *  we measure the deviation of the sequence?  Default 5. */
    public int getDislikeJumps() { return dislikeJumps; }
    public void setDislikeJumps(int dislikeJumps) {
        this.dislikeJumps = dislikeJumps;
    }

    private int deviation(QwirkleBoard board, QwirkleGrid.GridWalker gi) {
        int result = 0;
        List<QwirkleColor> line = new ArrayList<>();
        for (QwirkleGrid.LineWalker li : gi) {
            for (QwirkleLocation location : li) {
                QwirklePiece piece = board.get(location);
                if (piece == null && !line.isEmpty()) {
                    result += deviation(line);
                    line.clear();
                }
                if (piece != null)
                    line.add(piece.getColor());
            }
        }
        return result;
    }

    private int deviation(List<QwirkleColor> sequence) {
        int result = 0;
        QwirkleColor prev = sequence.get(0);
        for (int i = 1; i < sequence.size(); ++i) {
            QwirkleColor next = sequence.get(i);
            result += Math.min(rainbowDistance(prev, next), rainbowDistance(next, prev));
            prev = next;
        }
        return result;
    }

    /** How non-rainbow-y is this sequence of colors? */
    private int deviation2(List<QwirkleColor> sequence) {
        if (sequence.size() == 1) return 0;
        else {
            int forward = 0;
            QwirkleColor prev = sequence.get(0);
            for (int i = 1; i < sequence.size(); ++i) {
                QwirkleColor next = sequence.get(i);
                forward += rainbowDistance(prev, next);
                prev = next;
            }
            int backward = 0;
            prev = sequence.get(sequence.size() - 1);
            for (int i = sequence.size() - 2; i >= 0; --i) {
                QwirkleColor next = sequence.get(i);
                backward += rainbowDistance(prev, next);
                prev = next;
            }
            return Math.min(forward, backward);
        }
    }

    /** A positive distance in the rainbow from a to b. */
    private int rainbowDistance(QwirkleColor a, QwirkleColor b) {
        int result = indexes.get(b) - indexes.get(a);
        if (result < 0) result += indexes.size();
        return (result == 1) ? dislikeRainbow
                : (result == 0 ? dislikeMonochrome : result * dislikeJumps);
    }

    /** Pick the color that is farthest from its two nearest neighbors -- it will help us find a cycle. */
    private static QwirkleColor pickStart(Collection<QwirkleColor> colors) {
        if (colors.size() < 3)
            return colors.iterator().next();
        else {
            QwirkleColor pick = null;
            int max = 0;
            for (QwirkleColor x : colors) {
                // 2 nearest neighbors
                QwirkleColor a = nearest(x, colors);
                QwirkleColor b = nearest(x, colors, a);
                int distance = colorDistance(x, a) + colorDistance(x, b);
                if (distance > max) {
                    pick = x;
                    max = distance;
                }
            }
            return pick;
        }
    }

    private static QwirkleColor nearest(QwirkleColor x, Collection<QwirkleColor> colors) {
        return nearest(x, colors, null);
    }

    private static QwirkleColor nearest
            (QwirkleColor x, Collection<QwirkleColor> colors, QwirkleColor exceptFor)
    {
        int min = Integer.MAX_VALUE;
        QwirkleColor result = null;
        for (QwirkleColor y : colors) {
            if (y != x && y != exceptFor) { // don't pick self
                int d = colorDistance(x, y);
                if (d < min) {
                    min = d;
                    result = y;
                }
            }
        }
        return result;
    }

    public static int colorDistance(Integer a, Integer b) {
        int ra = a >> 16 & 255, ga = a >> 8 & 255, ba = a & 255;
        int rb = a >> 16 & 255, gb = a >> 8 & 255, bb = a & 255;
        return Math.abs(ra - rb) + Math.abs(ga - gb) + Math.abs(ba - bb);
    }

    // should we use distance squared?
    private static int colorDistance(QwirkleColor a, QwirkleColor b) {
        return Math.abs(a.getR() - b.getR())
                + Math.abs(a.getG() - b.getG())
                + Math.abs(a.getB() - b.getB());
    }
}
