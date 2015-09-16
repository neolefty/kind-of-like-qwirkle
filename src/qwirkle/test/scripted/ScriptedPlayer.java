package qwirkle.test.scripted;

import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;
import qwirkle.game.QwirklePlayer;
import qwirkle.players.MaxPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A player who follows a script. When you use this, make sure that the
 *  first player actually deserves to go first (has the best first play)
 *  by the rules of the game.
 *
 *  <p>Configured by a string, For example, "ph,0,0,pc,0,1;d:rc,ys;gd,2,2".
 *   Separate turns with semicolon; discard prefix with "d:"</p>*/
public class ScriptedPlayer implements QwirklePlayer {
    public List<ScriptedAction> plays = new ArrayList<>();
    public String name;
    public String config;
    public int i;

    /** Makes decisions after the script finishes. */
    public QwirklePlayer afterScript = new MaxPlayer();

    public ScriptedPlayer(String name, String config) {
        this.name = name;
        this.config = config;
        plays = ScriptedAction.parse(config);
    }

    @Override
    public Collection<QwirklePlacement> play(QwirkleBoard board, List<QwirklePiece> hand) {
        ScriptedAction action = getNextPlay();
        if (action == null)
            return afterScript.play(board, hand);
        else {
            if (!action.discard) { // regular play
                ++i;
                return action.placements;
            }
            else // discard
                return null; // Note: we'll increment i during discard
        }
    }

    @Override
    public Collection<QwirklePiece> discard(QwirkleBoard board, List<QwirklePiece> hand) {
        ScriptedAction action = getNextPlay();
        if (action == null)
            return afterScript.discard(board, hand);
        else {
            if (!action.discard) throw new IllegalStateException("not a discard");
            else {
                ++i; // turn is over; it's okay to advance
                return action.discards;
            }
        }
    }

    @Override public String getName() { return name; }

    @Override public String toString() { return name + " (" + config + ")"; }

    /** Compile pieces in future plays into a list of required pieces,
     *  in the order they'll be needed.
     *  @param max don't need more than this many */
    public List<QwirklePiece> getFuturePieces(int max) {
        List<QwirklePiece> result = new ArrayList<>();
        for (int k = i; k < plays.size() && k < (i+max); ++k)
            result.addAll(plays.get(k).getPieces());
        return result;
    }

    /** The next (as yet un-played) play this player will make,
     *  or null if we've completed our script. */
    public ScriptedAction getNextPlay() {
        if (i < plays.size())
            return plays.get(i);
        else
            return null;
    }
}
