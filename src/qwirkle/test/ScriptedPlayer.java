package qwirkle.test;

import com.google.common.base.Splitter;
import qwirkle.control.GameManager;
import qwirkle.control.SingleThreaded;
import qwirkle.game.*;
import qwirkle.players.MaxPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** A player who follows a script. When you use this, make sure that the
 *  first player actually deserves to go first (has the best first play)
 *  by the rules of the game.
 *
 *  <p>Configured by a string, For example, "ph,0,0,pc,0,1;d:rc,ys;gd,2,2".
 *   Separate turns with semicolon; discard prefix with "d:"</p>*/
public class ScriptedPlayer implements QwirklePlayer {
    public List<P> plays = new ArrayList<>();
    public String name;
    public String config;
    public int i;

    /** Makes decisions after the script finishes. */
    public QwirklePlayer afterScript = new MaxPlayer();

    public ScriptedPlayer(String name, String config) {
        this.name = name;
        this.config = config;
        plays = parse(config);
    }

    @Override
    public Collection<QwirklePlacement> play(QwirkleBoard board, List<QwirklePiece> hand) {
        if (i < plays.size()) {
            P p = plays.get(i);
            if (!p.discard) { // regular play
                ++i;
                return p.placements;
            }
            else // discard
                return null; // Note: we'll increment i during discard
        }
        else
            return afterScript.play(board, hand);
    }

    @Override
    public Collection<QwirklePiece> discard(QwirkleBoard board, List<QwirklePiece> hand) {
        if (i < plays.size()) {
            P p = plays.get(i++);
            if (!p.discard) throw new IllegalStateException("not a discard");
            else return p.discards;
        }
        else
            return afterScript.discard(board, hand);
    }

    @Override public String getName() { return name; }

    @Override public String toString() { return name + " (" + config + ")"; }

    /** A single play, either regular or discard. */
    private static class P {
        public Collection<QwirklePlacement> placements;
        public Collection<QwirklePiece> discards;
        public boolean discard;
    }

    public static class ScriptedSettings extends QwirkleSettings {
        public String config;

        /** When you use this, make sure that the first player actually deserves to go first
         *  (has the best first play) by the rules of the game.
         *  @param config a list of pieces, for example "gs,oc,gd,os,..." */
        public ScriptedSettings(Collection<AsyncPlayer> players, String config) {
            super(players, 1);
            this.config = config;
        }

        /** Add the scripted pieces to the start of the deck. */
        @Override
        public List<QwirklePiece> generate() {
            List<QwirklePiece> result = new ArrayList<>();
            for (String s : Splitter.on(',').split(config))
                if (s != null && !s.isEmpty())
                    result.add(new QwirklePiece(s));
            result.addAll(super.generate());
            return result;
        }
    }

    public static class ScriptedGameManager extends GameManager {
        public ScriptedGameManager() {
            super(new SingleThreaded());
            setRandomDealing(false);
        }
    }

    private static List<P> parse(String init) {
        List<P> result = new ArrayList<>();
        for (String turn : Splitter.on(';').split(init)) {
            P p = new P();
            if (turn.isEmpty()) continue;
            else if (turn.startsWith("d:")) { // discard: "d:yc,rc,bd" or "d:"
                p.discards = new ArrayList<>();
                p.discard = true;
                if (turn.length() > 2) {
                    turn = turn.substring(2);
                    for (String piece : Splitter.on(',').split(turn))
                        p.discards.add(new QwirklePiece(piece));
                }
            } else { // regular play: "rc,1,1,yc,2,1,gc,3,1"
                p.placements = new ArrayList<>();
                Iterator<String> i = Splitter.on(',').split(turn).iterator();
                while(i.hasNext())
                    p.placements.add(new QwirklePlacement(i.next(),
                            Integer.parseInt(i.next()), Integer.parseInt(i.next())));
            }
            result.add(p);
        }
        return result;
    }

    // Too hard!
//    /** Stack a deck so that it will allow these players to play what they want. */
//    private static List<QwirklePiece> extractDeck(Collection<ScriptedPlayer> players) {
//        List<QwirklePiece> result = new ArrayList<>();
//        int i = 0;
//        OUTER: while(true) {
//            for (ScriptedPlayer player : players) {
//                if (i >= player.plays.size())
//                    break OUTER;
//                P p = player.plays.get(i);
//                if (p.discard)
//
//            }
//        }
//    }
}
