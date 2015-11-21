package qwirkle.game.control.players;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import qwirkle.game.base.*;
import qwirkle.game.base.QwirkleAI;

import java.util.*;
import java.util.concurrent.Executors;

/** Looks for the play with the best score. */
public class ThreadedMaxAI implements QwirkleAI {
    private final static String[] PREFIXES_1 = { "M", "H", "P", "St", "B", "Gr", "Sn", "C", "D", "Fl", "R", "McM" };
    private final static String[] PREFIXES_2 = { "m", "h", "p", "st", "b", "gr", "sn", "c", "d", "fl", "r" };
    private final static String[] SUFFIXES = { "uff", "uck", "ogg", "igg", "iff" };
    private static final Random r = new Random();

    private int nThreads;
    private String name;

    public ThreadedMaxAI(String name, int threads) {
        this(threads);
        this.name = name;
    }

    public ThreadedMaxAI(int threads) {
        this.nThreads = threads;
        String suffix = SUFFIXES[r.nextInt(SUFFIXES.length)];
        name = PREFIXES_1[r.nextInt(PREFIXES_1.length)] + suffix + "le"
                + PREFIXES_2[r.nextInt(PREFIXES_2.length)] + suffix;
    }

    @Override
    public Collection<QwirklePlacement> play(QwirkleBoard board, List<QwirklePiece> hand) {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(8));

//        long start = System.currentTimeMillis();
        Set<Set<QwirklePlacement>> plays = new HashSet<>();
        Set<QwirklePiece> toPlay = new HashSet<>(hand);
        Set<QwirklePlacement> played = new HashSet<>();
        build(service, board, played, toPlay, plays);

//        long end = System.currentTimeMillis();
//        System.out.println(getName() + " found " + plays.size() + " moves in " + (end - start) + " ms: " + plays);
//        System.out.println(getName() + " found " + plays.size() + " moves in " + (end - start) + " ms.");

        // map of score to move -- highest score first
        Map<Integer, Set<QwirklePlacement>> moves = new TreeMap<>(Collections.reverseOrder());
        // 0 points for playing nothing, so that we return an empty move if nothing is possible
        moves.put(0, new HashSet<QwirklePlacement>());
        for (Set<QwirklePlacement> play : plays)
            moves.put(board.play(play).getLastScore(), play);
        int bestScore = moves.keySet().iterator().next();
//        System.out.println("Best (" + bestScore + "): " + moves.get(bestScore));
        return moves.get(bestScore);
    }

    /** Recursively build up a set of all possible moves (depth-first tree search).
     *   Note: Be sure to balance modifications of collections that come from above,
     *   except for adding to <tt>plays</tt>. */
    private void build(ListeningExecutorService service, QwirkleBoard board, Set<QwirklePlacement> played, Set<QwirklePiece> toPlay,
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
                build(service, board, played, toPlayScratch, plays);
                 // undo
                toPlayScratch.add(place.getPiece());
                played.remove(place);
            }
        }
        // if we couldn't add any pieces to our current move, it's a leaf
        if (!foundOne && !played.isEmpty())
            plays.add(new HashSet<>(played));
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
}
