package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.TurnCompleted;
import qwirkle.game.event.TurnStarting;
import qwirkle.ui.event.UpdateDiscards;
import qwirkle.ui.event.PlayPiece;

import java.util.*;

/** The logic for tracking discards. Doesn't accept or reject proposals -- that's handled
 *  by {@link HypotheticalPlayController}. Instead, it just watches for acceptances and cancellations,
 *  keeps track of them, and sends {@link UpdateDiscards} events. */
public class DiscardTracker {
    private final EventBus eventBus;
    private boolean vertical;

    /** Discards that have been accepted. */
    private Map<QwirklePiece, PlayPiece> acceptedDiscards = new IdentityHashMap<>();
    private int numSpots = 0;
    private QwirklePlayer curPlayer;

    public DiscardTracker(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(new OutsideListener());
    }

    /** The number of discards currently. */
    public int size() { return acceptedDiscards.size(); }

    public boolean isVertical() { return vertical; }
    public void setVertical(boolean vertical) {
        if (this.vertical != vertical) {
            this.vertical = vertical;
            update();
        }
    }

    public EventBus getEventBus() { return eventBus; }

    /** The player whose turn it is. */
    public QwirklePlayer getCurPlayer() { return curPlayer; }

    /** The number of places in this discard panel. Equal to the number of places in a player's hand. */
    public int getNumSpots() { return numSpots; }

    // listen to the main game event bus
    private class OutsideListener {
        // keep track of discards via accepts & cancels
        @Subscribe
        public void play(PlayPiece event) {
            if (event.isPhaseAccept()) {
                if (event.isPickupDiscard())
                    remove(event);
                if (event.isDropDiscard())
                    add(event);
            }
        }

        // when a game starts, update hand size based on the new game's settings
        @Subscribe public void start(GameStarted event) {
            numSpots = event.getSettings().getHandSize();
            clear();
        }

        @Subscribe public void turnStart(TurnStarting event) {
            curPlayer = event.getCurPlayer();
            update();
        }

        // when a turn ends, forget our state
        @Subscribe public void turnEnd(TurnCompleted event) { clear(); }
    }

    private void add(PlayPiece play) {
        acceptedDiscards.put(play.getPiece(), play);
        update();
    }

    private void remove(PlayPiece play) {
        PlayPiece removed = acceptedDiscards.remove(play.getPiece());
        if (removed != null)
            update();
    }

    private void clear() {
        acceptedDiscards.clear();
        curPlayer = null;
        update();
    }

    /** Place all the discarded pieces, horizontally or vertically, whichever is appropriate,
     *  using a heuristic to prevent them moving around unexpectedly,
     *  and post a {@link UpdateDiscards} event. */
    private void update() {
        // map of existing locations to pieces
        Map<QwirkleLocation, PlayPiece> notPlaced = new HashMap<>();
        for (PlayPiece p : acceptedDiscards.values())
            notPlaced.put(p.getDropLocation(), p);

        // map the ones that work neatly
        List<QwirklePlacement> newPlacements = new ArrayList<>();
        Set<QwirkleLocation> emptySpots = new HashSet<>();
        for (int i = 0; i < numSpots; ++i) {
            QwirkleLocation hLoc = new QwirkleLocation(i, 0), vLoc = new QwirkleLocation(0, i);
            QwirkleLocation actualLoc = vertical ? vLoc : hLoc;
            PlayPiece discard = notPlaced.remove(hLoc); // if it was horizontal before, this is where we'd find it
            if (discard == null)
                discard = notPlaced.remove(vLoc); // likewise if it was vertical
            if (discard == null) emptySpots.add(actualLoc);
            else newPlacements.add(new QwirklePlacement(discard.getPiece(), actualLoc));
        }

        // any missing? Put 'em in the empty spots
        while (!notPlaced.isEmpty()) {
            PlayPiece discard = notPlaced.remove(notPlaced.keySet().iterator().next());
            QwirkleLocation loc = emptySpots.iterator().next();
            emptySpots.remove(loc);
            newPlacements.add(new QwirklePlacement(discard.getPiece(), loc));
        }

        // post the placements in an event
        eventBus.post(new UpdateDiscards(Collections.unmodifiableList(newPlacements), emptySpots));
    }
}
