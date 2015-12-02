package qwirkle.ui.event;

import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.event.TurnCompleted;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** The current player interactively desires to play a turn. For confirmation, wait for a {@link TurnCompleted} event. */
public class PlayTurn {
    private List<QwirklePlacement> placements;
    private List<QwirklePiece> discards;

    /** Play some pieces. A static method to avoid ambiguities in naming. */
    public static PlayTurn play(Collection<QwirklePlacement> placements) {
        PlayTurn result = new PlayTurn();
        result.placements = Collections.unmodifiableList(new ArrayList<>(placements));
        return result;
    }

    /** Play some pieces. A static method to avoid ambiguities in naming. */
    public static PlayTurn discard(Collection<QwirklePiece> discards) {
        PlayTurn result = new PlayTurn();
        result.discards = Collections.unmodifiableList(new ArrayList<>(discards));
        return result;
    }

    private PlayTurn() { }

    public List<QwirklePlacement> getPlacements() { return placements; }

    public List<QwirklePiece> getDiscards() { return discards; }
}
