package qwirkle.ui.swing.game;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleGrid;
import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.base.impl.QwirkleGridImpl;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.TurnCompleted;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;

import java.util.*;

/** A panel you can discard pieces into. */
public class DiscardGridPanel extends QwirkleGridPanel {
    private QwirkleUIController controller;
    private boolean vertical;
    private List<QwirklePlacement> placements = new ArrayList<>();
    // the bus for managing events in the discard grid
    private EventBus discardBus;
    private int numSpots = 0;

    public DiscardGridPanel(QwirkleUIController controller) {
        super(new EventBus("discards"));
        discardBus = super.getEventBus();
        this.controller = controller;

        discardBus.register(new LocalListener());
        controller.register(new OutsideListener());
    }

    public void setVertical(boolean vertical) {
        update();
    }

    // listen to the local event bus (for our own grid)
    private class LocalListener {
        // on drag, remove from panel
        @Subscribe public void drag(DragPiece event) {
            if (event.isDrop()) {
                // if it is being dragged out of us, remove it
                if (event.getSourceGrid() == getGrid())
                    remove(event.getSourcePlacement());
                // if it is being dragged into one of our empty spots, add it
                if (event.getDestGrid() == getGrid() && getGrid().get(event.getDestLocation()) == null)
                    add(event.getDestPlacement());
            }
        }
        @Subscribe public void start(GameStarted event) {
            clear();
            numSpots = event.getSettings().getHandSize();
        }
        @Subscribe public void turnEnd(TurnCompleted event) { clear(); }
    }

    private void clear() {
        placements.clear();
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

    private void update() {
        setGrid(generateGrid());
    }

    // rebuild horizontally or vertically, whichever is appropriate, from either orientation
    private QwirkleGrid generateGrid() {
        // map of existing locations to pieces
        Map<QwirkleLocation, QwirklePiece> notPlaced = new HashMap<>();
        for (QwirklePlacement p : placements)
            notPlaced.put(p.getLocation(), p.getPiece());

        // map the ones that work neatly
        List<QwirklePlacement> newPlacements = new ArrayList<>();
        Set<QwirkleLocation> emptySpots = new HashSet<>();
        for (int i = 0; i < numSpots; ++i) {
            QwirkleLocation hLoc = new QwirkleLocation(0, i), vLoc = new QwirkleLocation(i, 0);
            QwirkleLocation actualLoc = vertical ? vLoc : hLoc;
            QwirklePiece piece = notPlaced.get(hLoc); // if it was horizontal before, this is where we'd find it
            if (piece == null) piece = notPlaced.get(vLoc); // likewise if it was vertical
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

        placements = newPlacements;
        return new QwirkleGridImpl(newPlacements);
    }

    // listen to the main game event bus
    private class OutsideListener {
    }
}
