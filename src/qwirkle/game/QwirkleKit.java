package qwirkle.game;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import java.util.*;

/** Miscellaneous utility methods. */
public class QwirkleKit {
    /** Used for deciding who goes first, based on who has the largest
     *  playable set of pieces. How many of the same piece or color are there in this set of pieces? */
    public static int countMatches(Collection<QwirklePiece> pieces) {
        // only count unique pieces (drop dupes)
        Set<QwirklePiece> unique = new HashSet<>(pieces);

        // 1. compile list of all the shapes and colors that are represented
        Set<QwirkleColor> colors = new HashSet<>();
        Set<QwirkleShape> shapes = new HashSet<>();
        for (QwirklePiece piece : unique) {
            colors.add(piece.getColor());
            shapes.add(piece.getShape());
        }

        // 2. what is the largest number of the same shape?
        int max = 0;
        for (QwirkleShape shape : shapes) {
            int n = countShapes(unique, shape);
            if (n > max)
                max = n;
        }

        // 3. what is the largest number of the same color?
        for (QwirkleColor color : colors) {
            int n = countColors(unique, color);
            if (n > max)
                max = n;
        }

        return max;
    }

    private static int countShapes(Collection<QwirklePiece> pieces, QwirkleShape shape) {
        int result = 0;
        for (QwirklePiece p : pieces)
            if (p.getShape() == shape)
                ++result;
        return result;
    }

    private static int countColors(Collection<QwirklePiece> pieces, QwirkleColor color) {
        int result = 0;
        for (QwirklePiece p : pieces)
            if (p.getColor() == color)
                ++result;
        return result;
    }

    // TODO add limits such as time and result count
    /** Find all possible plays for this hand. */
    public static Set<Set<QwirklePlacement>> findAllPossiblePlays(QwirkleBoard board, List<QwirklePiece> hand) {
//        long start = System.currentTimeMillis();
        Set<Set<QwirklePlacement>> result = new HashSet<>();
        Set<QwirklePiece> toPlay = new HashSet<>(hand);
        Set<QwirklePlacement> played = new HashSet<>();
        build(board, played, toPlay, result);
        return result;
//        long end = System.currentTimeMillis();
//        System.out.println(getName() + " found " + plays.size() + " moves in " + (end - start) + " ms: " + plays);
//        System.out.println(getName() + " found " + plays.size() + " moves in " + (end - start) + " ms.");
    }

    /** Recursively build up a set of all possible moves (depth-first tree search).
     *   Note: Be sure to balance modifications of collections that come from above,
     *   except for adding to <tt>plays</tt>. */
    private static void build(QwirkleBoard board, Set<QwirklePlacement> played, Set<QwirklePiece> toPlay,
                       Set<Set<QwirklePlacement>> plays)
    {
        // for each piece, find a place to add it to the current play
        boolean foundOne = false;
        Set<QwirklePiece> toPlayScratch = new HashSet<>(toPlay); // avoid concurrent mod
        for (QwirklePiece piece : toPlay) {
            // each possible place is a new potential play
            Collection<QwirklePlacement> places = board.getLegalPlacements(played, piece);
            if (!places.isEmpty())
                foundOne = true;
            // recur
            for (QwirklePlacement place : places) {
                // do
                played.add(place);
                toPlayScratch.remove(place.getPiece());
                // descend
                build(board, played, toPlayScratch, plays);
                 // undo
                toPlayScratch.add(place.getPiece());
                played.remove(place);
            }
        }
        // if we couldn't add any pieces to our current move, it's a leaf
        if (!foundOne && !played.isEmpty())
            plays.add(new HashSet<>(played));
    }

    private static Comparator hashComparator = new Comparator() {
        @Override
        public int compare(Object a, Object b) {
            int ha = a == null ? 0 : a.hashCode(), hb = b == null ? 0 : b.hashCode();
            return ha - hb;
        }
    };

    /** Find all moves and rank them, in reverse order, with the highest-scoring first.
     *  @return a multimap of score to move -- that is, a score can have multiple moves
     *  @param includeEmptyPlay if true, include a 0-score empty move
     *                          (so that it's still there if there are no legal moves). */
    public static Multimap<Integer, Set<QwirklePlacement>> rankAllMoves
            (QwirkleBoard board, List<QwirklePiece> hand, boolean includeEmptyPlay)
    {
        // all the possible moves, but without scores
        Set<Set<QwirklePlacement>> plays = findAllPossiblePlays(board, hand);

        // map of score to move -- highest score first
        Multimap<Integer, Set<QwirklePlacement>> result
                = TreeMultimap.create(Collections.reverseOrder(), hashComparator);

        // 0 points for playing nothing, so that we return an empty move if nothing is possible
        if (includeEmptyPlay)
            result.put(0, new HashSet<QwirklePlacement>());

        // rank them
        for (Set<QwirklePlacement> play : plays)
            result.put(board.play(play).getLastScore(), play);
//        System.out.println("Best (" + bestScore + "): " + moves.get(bestScore));

        return result;
    }
}
