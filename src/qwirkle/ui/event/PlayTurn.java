package qwirkle.ui.event;

import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.event.TurnCompleted;

import java.util.Collection;

/** The current player interactively desires to play a turn. For confirmation, wait for a {@link TurnCompleted} event. */
public class PlayTurn {
    private Collection<QwirklePlacement> placements;

    public PlayTurn(Collection<QwirklePlacement> placements) {
        this.placements = placements;
    }

    public Collection<QwirklePlacement> getPlacements() {
        return placements;
    }
}
