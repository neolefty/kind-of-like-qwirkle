package qwirkle.ui.control;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.event.DrawPieces;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.TurnCompleted;
import qwirkle.ui.event.PlayPiece;
import qwirkle.ui.event.UpdateHand;

import java.util.*;

/** Track what's in a player's hand during an interactive play. */
public class PlayerHandTracker {
    private EventBus eventBus;
    private QwirklePlayer player;

    // The pieces that have been dragged out of our hand and placed on the board
    private Set<QwirklePiece> draggedOut = Sets.newIdentityHashSet();
    private List<QwirklePiece> pieces;

    // Where each of our pieces belongs -- constructed when we refresh this panel
    // (even if they are currently not in our hand because they are on the board)
    // Note: Follow concurrency discipline when modifying it
    private final Map<QwirklePiece, QwirklePlacement> handPlaces = new IdentityHashMap<>();

    public PlayerHandTracker(EventBus eventBus, QwirklePlayer player) {
        this.eventBus = eventBus;
        this.player = player;

        getEventBus().register(new GameListener());
    }

    public EventBus getEventBus() { return eventBus; }

    public QwirklePlayer getPlayer() { return player; }

    private class GameListener {
        @Subscribe public void clearWhenGameStarts(GameStarted event) {
            clear();
            fireUpdate();
        }

        @Subscribe public void getHandWhenDealt(DrawPieces event) {
            // TODO shift old pieces up/left, make room for new ones
            if (event.getPlayer() == getPlayer()) {
                pieces = event.getHand();
                fireUpdate();
            }
        }

        // hide & show pieces in hand as we drag them to the board
        @Subscribe public void hideAndShowPieces(PlayPiece event) {
            // TODO allow moving to new locations
            if (event.getPlayer() == getPlayer() && event.isPhaseAccept()) {
                // dragged out of our hand?
                if (event.getPickup().isGridHand())
                    draggedOut.add(event.getPiece());
                // dragged back into hand?
                if (event.getDrop().isGridHand())
                    draggedOut.remove(event.getPiece());
                fireUpdate();
            }
        }

        @Subscribe public void resetAtEndOfTurn(TurnCompleted turn) {
            if (turn.getPlayer() == getPlayer()) {
                // if our turn ended, forget about the play we were contemplating
                clearTentative();
                fireUpdate();
            }
        }

        @Subscribe public void resetWhenGameStarts(GameStarted started) {
            clear();
            fireUpdate();
        }
    }

    /** A map representing this player's hand, for display. Empty spots (where a location has a null placement )
     *  should be displayed because they are where a piece has been removed
     *  and played tentatively on the board. */
    public UpdateHand.HandPlacements getHandPlacements(boolean vertical) {
        if (pieces == null)
            return UpdateHand.HandPlacements.createEmpty();

        else {
            synchronized (handPlaces) {
                List<QwirkleLocation> remainingLocations = new ArrayList<>();
                for (int i = 0; i < pieces.size(); ++i)
                    remainingLocations.add(new QwirkleLocation(vertical ? 0 : i, vertical ? i : 0));
                Map<QwirkleLocation, QwirklePlacement> byLocation = new IdentityHashMap<>();

                // 1. assign a location to each piece
                List<QwirklePiece> remainingPieces = new ArrayList<>();
                remainingPieces.addAll(pieces);
                // a. Do the easy ones -- we already know where they go, skipping conflicts
                for (QwirklePiece piece : pieces) {
                    QwirklePlacement placement = handPlaces.get(piece);
                    // if it matches one of our allowable placements, use it
                    if (placement != null && remainingLocations.contains(placement.getLocation())
                            && !byLocation.containsKey(placement.getLocation())) {
                        byLocation.put(placement.getLocation(), placement);
                        remainingPieces.remove(piece);
                        remainingLocations.remove(placement.getLocation());
                    }
                }
                // b. fill in the rest
                if (remainingPieces.size() != remainingLocations.size())
                    throw new IllegalStateException("mismatch: " + remainingPieces.size()
                            + "pieces, " + remainingLocations.size() + " locations");
                while (!remainingPieces.isEmpty()) {
                    QwirkleLocation loc = remainingLocations.remove(remainingLocations.size() - 1);
                    QwirklePiece piece = remainingPieces.remove(remainingPieces.size() - 1);
                    byLocation.put(loc, new QwirklePlacement(piece, loc));
                }

                // 2. remember these placements for spatial persistence
                handPlaces.clear();
                for (QwirklePlacement p : byLocation.values())
                    handPlaces.put(p.getPiece(), p);

                // 3. hide the ones that have been played
                Map<QwirklePiece, QwirklePlacement> visible = new IdentityHashMap<>(handPlaces);
                List<QwirkleLocation> empties = new ArrayList<>();
                for (QwirklePiece piece : draggedOut) {
                    QwirklePlacement empty = visible.remove(piece);
                    if (empty != null)
                        empties.add(empty.getLocation());
                }

                return new UpdateHand.HandPlacements(visible, empties, handPlaces);
            }
        }
    }

    private void fireUpdate() {
        getEventBus().post(new UpdateHand(this));
    }

    private void clear() {
        clearTentative();
        pieces = null;
        synchronized (handPlaces) {
            handPlaces.clear();
        }
    }

    /** Forget about the play we were contemplating. */
    private void clearTentative() {
        draggedOut.clear();
    }
}
