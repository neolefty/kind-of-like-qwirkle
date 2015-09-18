package qwirkle.test.scripted;

import qwirkle.control.GameManager;
import qwirkle.control.SingleThreadedStrict;
import qwirkle.game.AsyncPlayer;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.impl.AsyncPlayerWrapper;

import java.util.*;

/** GameManager for scripted games. */
public class ScriptedGameManager extends GameManager {
    public ScriptedGameManager() {
        super(new SingleThreadedStrict());
        setRandomDealing(false);
    }

    @Override
    public List<QwirklePiece> deal(AsyncPlayer player, Collection<QwirklePiece> hand) {
        ScriptedPlayer puppet = null;
        if (player instanceof AsyncPlayerWrapper) {
            QwirklePlayer qp = ((AsyncPlayerWrapper) player).getPlayer();
            if (qp instanceof ScriptedPlayer)
                puppet = (ScriptedPlayer) qp;
        }

        if (puppet == null)
            return super.deal(player, hand);
        else {
            List<QwirklePiece> needed = lookAhead(puppet, hand);
            List<QwirklePiece> handWithNeeds = new ArrayList<>(hand);
            handWithNeeds.addAll(needed);
            // do we still need more pieces?
            List<QwirklePiece> more = super.deal(player, handWithNeeds);
            // put the two deals together
            List<QwirklePiece> result = new ArrayList<>(needed);
            result.addAll(more);
            return Collections.unmodifiableList(result);
        }
    }

    /** look ahead to what pieces the player needs */
    private List<QwirklePiece> lookAhead(ScriptedPlayer player, Collection<QwirklePiece> hand) {
        int max = getSettings().getHandSize() * 2; // can't need more than 2x hand size
        List<QwirklePiece> future = player.getFuturePieces(max);
        Set<QwirklePiece> alreadyHave = new HashSet<>(hand);
        List<QwirklePiece> toDeal = new ArrayList<>();
        for (QwirklePiece needed : future) {
            // do we still need more pieces?
            boolean needMore = hand.size() + toDeal.size() < getSettings().getHandSize();
            boolean dontAlreadyHave = !alreadyHave.remove(needed);
            // if we don't already have it, add it to the list of pieces to deal
            if (needMore && dontAlreadyHave)
                toDeal.add(needed);
        }
        return Collections.unmodifiableList(toDeal);
    }
}
