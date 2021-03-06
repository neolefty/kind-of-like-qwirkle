package qwirkle.game.control.players;

import com.google.common.collect.Multimap;
import qwirkle.game.base.*;
import qwirkle.game.base.QwirkleAI;
import qwirkle.util.Stopwatch;

import java.util.*;

/** Looks for the play with the best score.
 *  Possible improvements:
 *  <ul>
 *      <li>prefer to play duplicates</li>
 *      <li>play more pieces</li>
 *      <li>maximize commonalities among remaining pieces</li>
 *      <li>think about board position strategically</li>
 *      <li>Avoid leaving open 5's (sets can be completed by a single piece)</li>
 *  </ul> */
public class MaxAI extends TimeLimitAI {
    private final static String[] PREFIXES_1 = { "M", "H", "P", "St", "B", "Gr", "Sn", "C", "D", "Fl", "R", "McM" };
    private final static String[] PREFIXES_2 = { "m", "h", "p", "st", "b", "gr", "sn", "c", "d", "fl", "r" };
    private final static String[] SUFFIXES = { "uff", "uck", "ogg", "igg", "iff" };
    private static final Random r = new Random();
    private String name;

    public MaxAI(String name) {
        this.name = name;
    }

    public MaxAI() {
        String suffix = SUFFIXES[r.nextInt(SUFFIXES.length)];
        name = PREFIXES_1[r.nextInt(PREFIXES_1.length)] + suffix + "le"
                + PREFIXES_2[r.nextInt(PREFIXES_2.length)] + suffix;
    }

    @Override
    public Collection<QwirklePlacement> play(QwirkleBoard board, List<QwirklePiece> hand) {
        Stopwatch w = new Stopwatch();
        Multimap<Integer, Set<QwirklePlacement>> moves
                = PlayerKit.rankAllMoves(board, hand, w, getMaxMillis(), true, false);
        int bestScore = moves.keySet().iterator().next();
        w.mark("finished: " + bestScore);
        setLastMoveWatch(w);
        return moves.get(bestScore).iterator().next(); // randomly choose one of the best
    }

    /** Keep the largest group; discard the rest. Discard duplicates. */
    @Override
    public Collection<QwirklePiece> discard(QwirkleBoard board, List<QwirklePiece> hand) {
        if (hand.isEmpty())
            return null;
        // sort by shape and color
        Map<QwirkleShape, Set<QwirklePiece>> byShape = new HashMap<>();
        Map<QwirkleColor, Set<QwirklePiece>> byColor = new HashMap<>();
        for (QwirklePiece piece : hand) {
            if (!byShape.containsKey(piece.getShape()))
                byShape.put(piece.getShape(), new HashSet<QwirklePiece>());
            if (!byColor.containsKey(piece.getColor()))
                byColor.put(piece.getColor(), new HashSet<QwirklePiece>());
            byShape.get(piece.getShape()).add(piece);
            byColor.get(piece.getColor()).add(piece);
        }
        // order: largest first
        Set<Set<QwirklePiece>> groupings = new TreeSet<>(new Comparator<Set<QwirklePiece>>() {
            @Override public int compare(Set<QwirklePiece> a, Set<QwirklePiece> b) {
                return b.size() - a.size();
            }
        });
        groupings.addAll(byShape.values());
        groupings.addAll(byColor.values());
        Set<QwirklePiece> keep = groupings.iterator().next();
        Set<QwirklePiece> discard = new HashSet<>(hand);
        if (keep.size() > 1)
            discard.removeAll(keep);
        return discard;
    }

    @Override public String getName() { return name; }
    @Override public String toString() { return name; }
}
