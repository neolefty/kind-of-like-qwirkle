package qwirkle.ui.event;

import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.ui.control.PlayerHandTracker;

import java.util.*;

/** A player's hand display should be updated because of an interactive play they are contemplating. */
public class UpdateHand {
    private PlayerHandTracker tracker;

    public UpdateHand(PlayerHandTracker tracker) {
        this.tracker = tracker;
    }

    public PlayerHandTracker getTracker() { return tracker; }
    public QwirklePlayer getPlayer() { return tracker.getPlayer(); }
    public HandPlacements getHandPlacements(boolean vertical) {
        return tracker.getHandPlacements(vertical);
    }

    public static class HandPlacements {
        private Map<QwirklePiece, QwirklePlacement> visiblePieces;
        private Collection<QwirkleLocation> emptySpots;
        private Map<QwirklePiece, QwirklePlacement> allPieces;

        public static HandPlacements createEmpty() {
            return new HandPlacements(
                    Collections.<QwirklePiece, QwirklePlacement>emptyMap(),
                    Collections.<QwirkleLocation>emptyList(),
                    Collections.<QwirklePiece, QwirklePlacement>emptyMap());
        }

        public HandPlacements(Map<QwirklePiece, QwirklePlacement> visiblePieces,
                              Collection<QwirkleLocation> emptySpots,
                              Map<QwirklePiece, QwirklePlacement> allPieces)
        {
            this.visiblePieces = Collections.unmodifiableMap(visiblePieces);
            this.emptySpots = Collections.unmodifiableCollection(emptySpots);
            this.allPieces = Collections.unmodifiableMap(allPieces);
        }

        /** Get the placements for these pieces. */
        public Collection<QwirklePlacement> getPlacements(Collection<QwirklePiece> pieces, boolean includeHidden) {
            if (pieces == null)
                return Collections.emptyList();
            else {
                Map<QwirklePiece, QwirklePlacement> map = includeHidden ? allPieces : visiblePieces;
                List<QwirklePlacement> result = new ArrayList<>();
                for (QwirklePiece piece : pieces)
                    if (map.containsKey(piece))
                        result.add(map.get(piece));
                return result;
            }
        }

        public Map<QwirklePiece, QwirklePlacement> getVisiblePieces() { return visiblePieces; }
        public Collection<QwirkleLocation> getEmptySpots() { return emptySpots; }
        public Map<QwirklePiece, QwirklePlacement> getAllPieces() { return allPieces; }
    }
}
