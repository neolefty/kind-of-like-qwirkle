package qwirkle.ui.swing.game.player;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleKit;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.impl.QwirkleGridImpl;
import qwirkle.game.event.DrawPieces;
import qwirkle.game.event.TurnStarting;
import qwirkle.ui.control.PlayerHandTracker;
import qwirkle.ui.control.SelfDisposingEventSubscriber;
import qwirkle.ui.event.UpdateHand;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;
import qwirkle.ui.swing.impl.SwingPlatformAttacher;

import javax.swing.*;
import java.util.List;

// TODO move logic into a controller
// TODO allow dragging to reorder pieces
// TODO highlight playable pieces, on your turn, with colored bg?
// TODO highlight destinations on mouseover?

/** Show a player's status. Uses a {@link QwirkleGridPanel} with
 *  hacked event updates to show the player's current hand, highlighting
 *  the most recently drawn pieces.
 *
 *  <p>Note that there are two event buses involved: A local one for this panel, with internal events,
 *  and a global one that receives events from the game at large.</p>*/
public class PlayerHandPanel extends QwirkleGridPanel {
    private PlayerHandTracker tracker;

    /** The set of pieces that we drew most recently. */
    private List<QwirklePiece> lastDraw;

    // is this panel displayed vertically (true) or horizontally (false)?
    private boolean vertical;

    public PlayerHandPanel(PlayerHandTracker tracker) {
        super(tracker.getEventBus(), DisplayType.hand);
        this.tracker = tracker;
        setBlankIncluded(false);
        new GameListener(tracker.getEventBus(), this);
        setVertical(true);
    }

    /** Vertical or horizontal? */
    public void setVertical(boolean vertical) {
        if (this.vertical != vertical) {
            this.vertical = vertical;
            update();
        }
    }

    public QwirklePlayer getPlayer() { return tracker.getPlayer(); }

    private class GameListener extends SelfDisposingEventSubscriber {
        public GameListener(EventBus bus, JComponent home) {
            super(bus, new SwingPlatformAttacher(home));
        }

        @Subscribe public void updateUI(UpdateHand event) {
            if (event.getPlayer() == getPlayer())
                update();
        }

//        // listen for dragging pieces in and out
//        @Subscribe public void drag(DragPiece event) {
//            if (event.getPlayer() == player) {
//                // if picking up from somewhere else, drag it out
//                // (if picking up from here, don't need to because PiecePanel will hide it)
//                if (event.isActionPickup() && event.getGrid() != getGrid())
//                    dragOut(event.getPlacement());
//                // if cancelling or dropping, undo the drag out
//                if (event.isActionCancel() || event.isActionDrop())
//                    undragOut(event.getPickup().getPlacement());
//            }
//        }

        // highlight the new pieces we draw
        @Subscribe public void dealt(DrawPieces draw) {
            if (draw.getPlayer() == getPlayer()) {
                lastDraw = draw.getDrawn();
                update();
            }
        }

        // when our turn starts, stop highlighting the last pieces we drew
        @Subscribe public void turnStart(TurnStarting event) {
            if (event.getCurPlayer() == getPlayer()) {
                lastDraw = null;
                update();
            }
        }
    }

    /** Update the UI. */
    private void update() {
        UpdateHand.HandPlacements hand = tracker.getHandPlacements(vertical);
        setHighlight(QwirkleKit.placementsToLocations(hand.getPlacements(lastDraw, false)));
        setAlwayShown(hand.getEmptySpots(), false);
        setGrid(new QwirkleGridImpl(hand.getVisiblePieces().values()));

//        synchronized(handPlaces) {
//            handPlaces.clear();
//            List<QwirklePlacement> handDisplay = new ArrayList<>();
//            List<QwirklePiece> drawScratch = new ArrayList<>();
//            if (lastDraw != null) drawScratch.addAll(lastDraw);
//
//            // make a grid from our hand, along with the new pieces
//            List<QwirklePiece> hand = control.getGame().getHand(player);
//            List<QwirkleLocation> drawLocations = new ArrayList<>();
//            if (hand != null) {
//                // build a grid that represents the hand
//                for (int i = 0; i < hand.size(); ++i) {
//                    QwirkleLocation location = new QwirkleLocation(vertical ? 0 : i, vertical ? i : 0);
//                    QwirklePiece piece = hand.get(i);
//                    QwirklePlacement place = new QwirklePlacement(piece, location);
//                    handPlaces.add(place);
//                    if (!draggedOut.contains(place))
//                        handDisplay.add(place);
//                }
//                // highlight the new pieces, starting at the bottom
//                for (int i = handDisplay.size() - 1; i >= 0; --i) {
//                    QwirklePlacement place = handDisplay.get(i);
//                    // make sure each draw is highlighted only once, but allow duplicate
//                    if (drawScratch.remove(place.getPiece()))
//                        drawLocations.add(place.getLocation());
//                }
//            }
//            QwirkleGrid handGrid = new QwirkleGridImpl(handDisplay);
//            setGrid(handGrid);
//            setHighlight(drawLocations);
//        }
    }
}
