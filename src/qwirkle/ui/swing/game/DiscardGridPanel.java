package qwirkle.ui.swing.game;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.impl.QwirkleGridImpl;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.TurnCompleted;
import qwirkle.game.event.TurnStarting;
import qwirkle.ui.control.DragForwarder;
import qwirkle.ui.control.HypotheticalPlay;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.event.PlayPiece;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;

import java.util.*;

// TODO disallow regular playing if discarding and vice-versa
// TODO highlight when droppable?
/** A panel you can discard pieces into. */
public class DiscardGridPanel extends QwirkleGridPanel {
    private boolean vertical;
    private List<QwirklePlacement> placements = new ArrayList<>();
    private int numSpots = 0;
    private QwirkleUIController uiController;
    private HypotheticalPlay hypoPlay;
    private QwirklePlayer curPlayer;

    public DiscardGridPanel(QwirkleUIController controller) {
        super(new EventBus("discards"), DisplayType.discard);
        this.uiController = controller;
        this.hypoPlay = uiController.getHypothetical();

        // the bus for managing events in the discard grid
        EventBus localBus = super.getEventBus();
        // forward drag events
        new DragForwarder(localBus, this, controller.getEventBus());

        controller.register(new OutsideListener());

        super.setBlankIncluded(false);
    }

    // listen to the main game event bus
    private class OutsideListener {
        // keep track of discards via accepts & cancels
        @Subscribe public void play(PlayPiece event) {
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
            makeDraggable(curPlayer);
        }

        // when a turn ends, forget our state
        @Subscribe public void turnEnd(TurnCompleted event) { clear(); }
    }

    public void setVertical(boolean vertical) {
        if (this.vertical != vertical) {
            this.vertical = vertical;
            update();
        }
    }

    /** The number of places in this discard panel. Equal to the number of places in a player's hand. */
    public int getNumSpots() {
        return numSpots;
    }

    private void clear() {
        placements.clear();
        curPlayer = null;
        update();
    }

    private void remove(QwirklePlacement placement) {
        if (placements.remove(placement))
            update();
    }

    private void add(QwirklePlacement placement) {
        if (isLocationFilled(placement.getLocation()))
            throw new IllegalStateException("Can't add " + placement + " because it's already taken: " + placements);
        else
            placements.add(placement);
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

        // fill in with empties -- kind of ugly that this is a side effect
        setAlwayShown(emptySpots, false);

        // remember the new placements, in case anything had to be moved
        placements = newPlacements;

        // display the updated board
        setGrid(new QwirkleGridImpl(newPlacements));
        if (curPlayer != null)
            makeDraggable(curPlayer);
    }
}
