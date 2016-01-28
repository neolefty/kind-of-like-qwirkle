package qwirkle.ui.event;

import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePlacement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** An interactive discard that a player is contemplating has been updated. */
public class UpdateDiscards {
    private final List<QwirklePlacement> placements;
    private final List<QwirkleLocation> alsoVisible;

    public UpdateDiscards(Collection<QwirklePlacement> placements, Collection<QwirkleLocation> alsoVisible) {
        this.placements = Collections.unmodifiableList(new ArrayList<>(placements));
        this.alsoVisible = Collections.unmodifiableList(new ArrayList<>(alsoVisible));
    }

    /** The non-empty placements for the interactive discard that the player is building. */
    public List<QwirklePlacement> getPlacements() { return placements; }

    /** Empty spots that should also be shown. */
    public List<QwirkleLocation> getAlsoVisible() { return alsoVisible; }
}
