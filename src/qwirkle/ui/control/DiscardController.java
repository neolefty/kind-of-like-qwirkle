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
import qwirkle.ui.event.DiscardUpdate;
import qwirkle.ui.event.PlayPiece;

import java.util.*;

/** The logic for tracking discards. Doesn't accept or reject proposals -- that's handled
 *  by {@link HypotheticalPlay}. Instead, it just watches for acceptances and cancellations,
 *  keeps track of them, and sends {@link DiscardUpdate} events. */
public class DiscardController {
    private final EventBus localBus = new EventBus("discards");
    private final EventBus externalBus;
    private boolean vertical;

    private List<QwirklePlacement> placements = new ArrayList<>();
    private int numSpots = 0;
    private QwirklePlayer curPlayer;

    public DiscardController(EventBus eventBus) {
        this.externalBus = eventBus;
        externalBus.register(new OutsideListener());

        // forward drag events -- don't worry about undisposing because this controller is immortal
        new DragForwarder(getLocalBus(), null, getMainBus());
    }

    /** The bus that is local to the discard panel. */
    public EventBus getLocalBus() { return localBus; }
    /** The main bus for the gameboard. */
    public EventBus getMainBus() { return externalBus; }

    public boolean isVertical() { return vertical; }
    public void setVertical(boolean vertical) {
        if (this.vertical != vertical) {
            this.vertical = vertical;
            update();
        }
    }

    /** The player whose turn it is. */
    public QwirklePlayer getCurPlayer() { return curPlayer; }

    /** The number of places in this discard panel. Equal to the number of places in a player's hand. */
    public int getNumSpots() { return numSpots; }

    // listen to the main game event bus
    private class OutsideListener {
        // keep track of discards via accepts & cancels
        @Subscribe
        public void play(PlayPiece event) {
            if (event.isTypeDiscard()) {
                if (event.isPhaseAccept())
                    add(event.getPlacement());
                else if (event.isPhaseCancel())
                    // TODO postpone, like GameboardPanel
                    remove(event.getPlacement());
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

    private void add(QwirklePlacement placement) {
        if (isLocationFilled(placement.getLocation()))
            throw new IllegalStateException("Can't add " + placement + " because it's already taken: " + placements);
        else
            placements.add(placement);
        update();
    }

    private void remove(QwirklePlacement placement) {
        if (placements.remove(placement))
            update();
    }

    private void clear() {
        placements.clear();
        curPlayer = null;
        update();
    }

    private boolean isLocationFilled(QwirkleLocation loc) {
        for (QwirklePlacement p : placements)
            if (p.getLocation().equals(loc))
                return true;
        return false;
    }

    // rebuild horizontally or vertically, whichever is appropriate, from either orientation
    private void update() {
        // map of existing locations to pieces
        Map<QwirkleLocation, QwirklePiece> notPlaced = new HashMap<>();
        for (QwirklePlacement p : placements)
            notPlaced.put(p.getLocation(), p.getPiece());

        // map the ones that work neatly
        List<QwirklePlacement> newPlacements = new ArrayList<>();
        Set<QwirkleLocation> emptySpots = new HashSet<>();
        for (int i = 0; i < numSpots; ++i) {
            QwirkleLocation hLoc = new QwirkleLocation(i, 0), vLoc = new QwirkleLocation(0, i);
            QwirkleLocation actualLoc = vertical ? vLoc : hLoc;
            QwirklePiece piece = notPlaced.remove(hLoc); // if it was horizontal before, this is where we'd find it
            if (piece == null) piece = notPlaced.remove(vLoc); // likewise if it was vertical
            if (piece == null) emptySpots.add(actualLoc);
            else newPlacements.add(new QwirklePlacement(piece, actualLoc));
        }

        // any missing? Put 'em in the empty spots
        while (!notPlaced.isEmpty()) {
            QwirklePiece piece = notPlaced.remove(notPlaced.keySet().iterator().next());
            QwirkleLocation place = emptySpots.iterator().next();
            emptySpots.remove(place);
            newPlacements.add(new QwirklePlacement(piece, place));
        }

        // remember the new placements, in case anything had to be moved
        placements = newPlacements;

        DiscardUpdate event = new DiscardUpdate(newPlacements, emptySpots);
        getMainBus().post(event);
    }
}
