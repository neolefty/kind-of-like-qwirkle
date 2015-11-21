package qwirkle.test.scripted;

import com.google.common.eventbus.EventBus;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.control.GameController;
import qwirkle.game.control.impl.SingleThreadedStrict;

import java.util.*;

/** GameManager for scripted games. */
public class ScriptedGameController extends GameController {
    public ScriptedGameController(EventBus bus) {
        super(bus, new SingleThreadedStrict());
        setRandomDealing(false);
    }

    @Override
    public List<QwirklePiece> deal(QwirklePlayer player, Collection<QwirklePiece> hand) {
        ScriptedAI puppet = null;
        if (player.getAi() != null && player.getAi() instanceof ScriptedAI)
            puppet = (ScriptedAI) player.getAi();

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
    private List<QwirklePiece> lookAhead(ScriptedAI player, Collection<QwirklePiece> hand) {
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
