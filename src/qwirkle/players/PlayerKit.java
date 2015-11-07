package qwirkle.players;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;
import qwirkle.test.Stopwatch;

import java.util.*;

/** Helper functions for players. */
public class PlayerKit {

    private static Comparator setComparator = new Comparator<Set<Comparable>>() {
        @Override
        public int compare(Set<Comparable> a, Set<Comparable> b) {
            //noinspection unchecked
            if (a.size() == b.size()) {
                Iterator<Comparable> ia = a.iterator(), ib = b.iterator();
                while (ia.hasNext() && ib.hasNext()) {
                    //noinspection unchecked
                    int c = ia.next().compareTo(ib.next());
                    if (c != 0)
                        return c;
                }
                // one of them finished
                if (ia.hasNext()) return 1;
                else if (ib.hasNext()) return -1;
                else return 0; // all elements matched
            } else {
                return a.size() - b.size();
            }
        }
    };

    private static Comparator hashComparator = new Comparator() {
        @Override
        public int compare(Object a, Object b) {
            int ha = a == null ? 0 : a.hashCode(), hb = b == null ? 0 : b.hashCode();
            return ha - hb;
        }
    };

    // TODO add limits such as time and result count
    // TODO write multithreaded version?
    /** Find all possible plays on this board for this hand.
     * @param includeShorties if true, include plays that are suboptimal because they're short
     *                         (that is, their supersets are also present)
     * @param w*/
    public static Set<Set<QwirklePlacement>> findAllPossiblePlays
        (QwirkleBoard board, List<QwirklePiece> hand, boolean includeShorties, Stopwatch w)
    {
        Set<Set<QwirklePlacement>> result = new HashSet<>();
        Set<Set<QwirklePlacement>> prunes = includeShorties
                ? result : new HashSet<Set<QwirklePlacement>>();
        HashSet<QwirklePiece> toPlay = new HashSet<>(hand);
        HashSet<QwirklePlacement> played = new HashSet<>();
        buildAllPossiblePlays(board, played, toPlay, result, prunes);
        w.mark("found " + result.size() + " plays" + (includeShorties ? "" : " (pruned " + prunes.size() + ")"));
        return result;
//        long end = System.currentTimeMillis();
//        System.out.println(getName() + " found " + plays.size() + " moves in " + (end - start) + " ms: " + plays);
//        System.out.println(getName() + " found " + plays.size() + " moves in " + (end - start) + " ms.");
    }

    /** Recursively build up a set of all possible moves (depth-first tree search).
     *   Note: Be sure to balance modifications of collections that come from above,
     *   except for adding to <tt>plays</tt>. Note the use of HashSets -- they need to
     *   all be that exact type because we rely on HashSet.equals().
     *   @param board make hypothetical plays on it
     *   @param played a hypothetical play we're considering, which we'll add to
     *   @param toPlay the pieces we have left that we can add to the play
     *   @param plays the plays we've found and considered so far
     *   @param prunes the branches we've already considered */
    private static void buildAllPossiblePlays
        (QwirkleBoard board, Set<QwirklePlacement> played, Set<QwirklePiece> toPlay,
         Set<Set<QwirklePlacement>> plays, Set<Set<QwirklePlacement>> prunes)
    {
        // for each piece, find a place to add it to the current play
        boolean leaf = false;
        HashSet<QwirklePiece> toPlayScratch = new HashSet<>(toPlay); // avoid concurrent mod
        for (QwirklePiece piece : toPlay) {
            // each possible place is a new potential play
            Collection<QwirklePlacement> places = board.getLegalPlacements(played, piece);
            if (!places.isEmpty())
                leaf = true;
            // recurse
            for (QwirklePlacement place : places) {
                // do
                played.add(place);
                toPlayScratch.remove(place.getPiece());
                // descend
                if (!prunes.contains(played)) { // relies on HashSet.equals()
                    prunes.add(new HashSet<>(played));
                    buildAllPossiblePlays(board, played, toPlayScratch, plays, prunes);
                }
                 // undo
                toPlayScratch.add(place.getPiece());
                played.remove(place);
            }
        }
        // if we couldn't add any pieces to our current move, it's a leaf
        if (!leaf && !played.isEmpty()) {
            HashSet<QwirklePlacement> copied = new HashSet<>(played);
            prunes.add(copied);
            if (plays != prunes) // they're the same when we want to include all the shorties
                plays.add(copied);
        }
    }

    /** Find all moves and rank them, in reverse order, with the highest-scoring first.
     *  @return a multimap of score to move -- that is, a score can have multiple moves
     *  @param includeEmptyPlay if true, include a 0-score empty move
     *                          (so that it's still there if there are no legal moves).
     *  @param includeShorties if true, include plays that are suboptimal because they're short
     *                         (that is, their supersets are also present) */
    public static Multimap<Integer, Set<QwirklePlacement>> rankAllMoves
            (QwirkleBoard board, List<QwirklePiece> hand, boolean includeEmptyPlay, boolean includeShorties)
    {
        Stopwatch w = new Stopwatch();

        // all the possible moves, but without scores
        Set<Set<QwirklePlacement>> plays = findAllPossiblePlays(board, hand, includeShorties, w);

        // map of score to move -- highest score first
        //noinspection unchecked
        Multimap<Integer, Set<QwirklePlacement>> result
                = TreeMultimap.create(Collections.reverseOrder(), setComparator);

        // 0 points for playing nothing, so that we return an empty move if nothing is possible
        if (includeEmptyPlay)
            result.put(0, new HashSet<QwirklePlacement>());

        // rank them
        for (Set<QwirklePlacement> play : plays) {
//            int score = board.play(play).getLastScore();
//            System.out.print(play + "(" + score + ") before " + result.size());
//            result.put(score, play);
//            System.out.println(" after " + result.size());
            result.put(board.play(play).getLastScore(), play);
        }
        w.mark("ranked " + result.size());
        System.out.println(w);

        return result;
    }
}
