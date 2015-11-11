package qwirkle.players;

import com.google.common.collect.Multimap;
import qwirkle.game.*;
import qwirkle.test.Stopwatch;

import java.util.*;

// TODO make the rainbow deviation a cost, to be subtracted from the move's score?
/** Tries to harmonize with the rest of the board */
public class RainbowPlayer implements QwirklePlayer {
    private static final Random r = new Random();
    private static final String[] prefixes = { "Color", "Paint", "Rain" }, suffixes = { "ful", "ing", "er" };

    private int bias = 1;
    private Rainbow rainbow;
    private String name;

    private static String namePrefix() { return prefixes[r.nextInt(prefixes.length)]; }
    private static String nameSuffix() { return suffixes[r.nextInt(suffixes.length)]; }
    private static String namePart() { return namePrefix() + nameSuffix(); }
    private static String buildName() {
        String result = namePart();
        while (!result.endsWith(suffixes[2]))
            result += " " + namePart();
        return result;
    }

    private static final boolean DEBUG = false;
    private static void debug(String s) { if (DEBUG) System.out.println(s); }

    public RainbowPlayer(String name, QwirkleSettings settings) {
        this(name, settings.getColors());
    }

    public RainbowPlayer(String name, Collection<QwirkleColor> colors) {
        this.name = name;
        this.rainbow = new Rainbow(colors);
    }

    public RainbowPlayer(QwirkleSettings settings) { this(settings.getColors()); }
    public RainbowPlayer(Collection<QwirkleColor> colors) {
        this(buildName(), colors);
    }

    /** How biased is this player toward cooperation? How many points are they willing
     *  to give up per turn to achieve it? Default 1. */
    public int getBias() { return bias; }

    /** The rainbow this player uses to make its aesthetic choices. */
    public Rainbow getRainbow() { return rainbow; }

    @Override public String getName() { return name; }

    /** How biased is this player toward cooperation? How many points are they willing
     *  to give up per turn to achieve it? Default 1. */
    public void setBias(int bias) { this.bias = bias; }

    @Override
    public Collection<QwirklePlacement> play(QwirkleBoard board, List<QwirklePiece> hand) {
        Stopwatch w = new Stopwatch();

        // ranked by score, highest first
        // (note we include the empty play, since if we just have junk plays, we'd rather draw and try for a rainbow)
        Multimap<Integer, Set<QwirklePlacement>> ranked = PlayerKit.rankAllMoves(board, hand, true, true);
        w.mark("rank by score");

        // find the one with the best rainbow-ness, within our bias
//        Rainbow rainbow = new Rainbow(compileColors(board, hand));
        int max = ranked.keySet().iterator().next();
        Set<QwirklePlacement> best = null;
        int minDeviation = Integer.MAX_VALUE;
        int nConsidered = 0;
        for (Integer score : ranked.keySet()) {
            if (max - score > bias) break;
            Collection<Set<QwirklePlacement>> moves = ranked.get(score);
            for (Set<QwirklePlacement> move : moves) {
                ++nConsidered;
                int deviation = rainbow.computeRainbowDeviation(board.play(move));
                if (deviation < minDeviation) {
                    best = move;
                    minDeviation = deviation;
                }
            }
        }
        w.mark("rank by rainbow");
        debug(w + ": total possible moves " + ranked.size() + "; considered " + nConsidered);
        return best;
    }

    private static Collection<QwirkleColor> compileColors(QwirkleBoard board, Collection<QwirklePiece> hand) {
        Set<QwirkleColor> result = new HashSet<>();
        if (hand != null)
            for (QwirklePiece p : hand)
                result.add(p.getColor());
        if (board != null)
            for (QwirklePlacement p : board.getPlacements())
                result.add(p.getColor());
        return result;
    }

    /** If we're going to discard, we must have junk, so discard our whole hand. */
    @Override
    public Collection<QwirklePiece> discard(QwirkleBoard board, List<QwirklePiece> hand) {
        return new ArrayList<>(hand); // don't pass hand -- it will lead to bugs
    }

    @Override public String toString() { return name; }
}
