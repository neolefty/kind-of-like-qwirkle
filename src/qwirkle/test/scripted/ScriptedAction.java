package qwirkle.test.scripted;

import com.google.common.base.Splitter;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;

import java.util.*;

/** A single play, either placing pieces or discarding. */
public class ScriptedAction {
    public Collection<QwirklePlacement> placements;
    public Collection<QwirklePiece> discards;
    public boolean discard;

    /** The number of cards involved, regardless of it's a discard or a play. */
    public int getPieceCount() {
        return discard ? discards.size() : placements.size();
    }

    public Collection<QwirklePiece> getPieces() {
        return discard ? discards : getPieces(placements);
    }

    public static List<ScriptedAction> parse(String init) {
        List<ScriptedAction> result = new ArrayList<>();
        for (String turn : Splitter.on(';').split(init)) {
            ScriptedAction p = new ScriptedAction();
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

    private static List<QwirklePiece> getPieces(Collection<QwirklePlacement> placements) {
        List<QwirklePiece> result = new ArrayList<>();
        for (QwirklePlacement placement : placements)
            result.add(placement.getPiece());
        return Collections.unmodifiableList(result);
    }
}
