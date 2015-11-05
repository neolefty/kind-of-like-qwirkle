package qwirkle.players;

import com.google.common.collect.Multimap;
import qwirkle.game.*;
import qwirkle.test.Stopwatch;

import java.util.*;

/** Tries to harmonize with the rest of the board */
public class RainbowPlayer extends MaxPlayer {
    private static final Random r = new Random();
    private static final String[] prefixes = { "Help", "Aid", "Assist" }, suffixes = { "ful", "ing", "er" };

    private int bias = 3;

    private static String namePrefix() { return prefixes[r.nextInt(prefixes.length)]; }
    private static String nameSuffix() { return suffixes[r.nextInt(suffixes.length)]; }
    private static String namePart() { return namePrefix() + nameSuffix(); }
    private static String buildName() {
        String result = namePart();
        while (!result.endsWith(suffixes[2]))
            result += " " + namePart();
        return result;
    }

    public RainbowPlayer(String name) {
        super(name);
    }

    public RainbowPlayer() {
        this(buildName());
    }

    /** How biased is this player toward cooperation? How many points are they willing
     *  to give up per turn to achieve it? Default 3. */
    public int getBias() { return bias; }

    /** How biased is this player toward cooperation? How many points are they willing
     *  to give up per turn to achieve it? Default 3. */
    public void setBias(int bias) { this.bias = bias; }

    @Override
    public Collection<QwirklePlacement> play(QwirkleBoard board, List<QwirklePiece> hand) {
        Stopwatch w = new Stopwatch();
        // ranked by score, highest first
        Multimap<Integer, Set<QwirklePlacement>> ranked = QwirkleKit.rankAllMoves(board, hand, false);
        w.mark("rank by score");

        // no moves?
        if (ranked.size() == 0) return null;

        // find the one with the best rainbow-ness, within our bias
        Rainbow rainbow = new Rainbow(compileColors(board, hand));
        int max = ranked.keySet().iterator().next();
        Set<QwirklePlacement> best = null;
        int minDeviation = Integer.MAX_VALUE;
        for (Integer score : ranked.keySet()) {
            if (max - score > bias) break;
            Collection<Set<QwirklePlacement>> moves = ranked.get(score);
            for (Set<QwirklePlacement> move : moves) {
                int deviation = rainbow.rainbowDeviation(board.play(move));
                if (deviation < minDeviation) {
                    best = move;
                    minDeviation = deviation;
                }
            }
        }
        w.mark("rank by rainbow");
        System.out.println(w);
        return best;
    }

    private static Collection<QwirkleColor> compileColors(QwirkleBoard board, Collection<QwirklePiece> hand) {
        Set<QwirkleColor> result = new HashSet<>();
        for (QwirklePiece p : hand)
            result.add(p.getColor());
        for (QwirklePlacement p : board.getPlacements())
            result.add(p.getColor());
        return result;
    }

    private static class Rainbow {
        Map<QwirkleColor, Integer> indexes = new HashMap<>();
        List<QwirkleColor> rainbow = new ArrayList<>();

        /** Approximate a rainbow by starting with the color that is farthest from its two neighbors,
         *  then "going around the ring" picking the nearest neighbors. */
        Rainbow(Collection<QwirkleColor> colors) {
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

        int rainbowDeviation(QwirkleBoard board) {
            // include padding so that we run off the end of groups to force a calculation
            return rainbowDeviation(board, board.getHorizontalWalker(1))
                    + rainbowDeviation(board, board.getVerticalWalker(1));
        }

        private int rainbowDeviation(QwirkleBoard board, QwirkleGrid.GridWalker gi) {
            int result = 0;
            List<QwirkleColor> line = new ArrayList<>();
            for (QwirkleGrid.LineWalker li : gi) {
                for (QwirkleLocation location : li) {
                    QwirklePiece piece = board.get(location);
                    if (piece == null && !line.isEmpty()) {
                        result += rainbowDeviation(line);
                        line.clear();
                    }
                    if (piece != null)
                        line.add(piece.getColor());
                }
            }
            return result;
        }

        // TODO: try calculating max rainbow distance between any two colors in a sequence instead?
        // TODO: discourage same-color neighbors?

        /** How non-rainbow-y is this sequence of colors? */
        private int rainbowDeviation(List<QwirkleColor> sequence) {
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
//            return result;
            // count same color as worse than neighbor (swap 1 and 0)
//            return (result == 1) ? 0 : (result == 0 ? 1 : result);
            // count neighbor color just as good as same color
//            return (result == 1 ? 0 : result);
            // count same color only slightly worse than neighbor color
            return (result == 1) ? 0 : (result == 0 ? 1 : result * 5);
        }

        /** Pick the color that is farthest from its two nearest neighbors -- it will help us find a cycle. */
        static QwirkleColor pickStart(Collection<QwirkleColor> colors) {
            QwirkleColor pick = null;
            int max = 0;
            for (QwirkleColor x : colors) {
                // 2 nearest neighbors
                QwirkleColor a = nearest(x, colors);
                QwirkleColor b = nearest(x, colors, a);
                int distance = distance(x, a) + distance(x, b);
                if (distance > max) {
                    pick = x;
                    max = distance;
                }
            }
            return pick;
        }

        static QwirkleColor nearest(QwirkleColor x, Collection<QwirkleColor> colors) { return nearest(x, colors, null); }

        static QwirkleColor nearest(QwirkleColor x, Collection<QwirkleColor> colors, QwirkleColor exceptFor) {
            int min = Integer.MAX_VALUE;
            QwirkleColor result = null;
            for (QwirkleColor y : colors) {
                if (y != x && y != exceptFor) { // don't pick self
                    int d = distance(x, y);
                    if (d < min) {
                        min = d;
                        result = y;
                    }
                }
            }
            return result;
        }

        // should we use distance squared?
        static int distance(QwirkleColor a, QwirkleColor b) {
            return Math.abs(a.getR() - b.getR())
                    + Math.abs(a.getG() - b.getG())
                    + Math.abs(a.getB() - b.getB());
        }
    }
}
