package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.ui.event.PassOver;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.event.PlayPiece;
import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.ui.QwirklePieceDisplay;

/** Watches PieceDrag and PassOver events to catch when someone
 *  is trying to play a piece by dragging and dropping.
 *  Note: doesn't guarantee that the attempt to play is legal. */
public class DragToPlayPromoter {
    private QwirklePieceDisplay lastDisplay;

    public DragToPlayPromoter(final EventBus bus) {
        bus.register(new Object() {
            @Subscribe
            public void passOver(PassOver event) {
                // moved in --> currently over a display
                if (event.isEnter())
                    lastDisplay = event.getDisplay();
                // moved out --> currently not over a display
                else
                    lastDisplay = null;
            }

            @Subscribe
            public void drag(DragPiece event) {
                // if it's a drop, and it's over a QwirklePieceDisplay,
                if (event.isDrop()) {
                    QwirklePieceDisplay pieceDisplay = lastDisplay; // avoid concurrency problems by grabbing a temp copy
                    if (pieceDisplay != null /* && pieceDisplay.getPiece() == null */) {
                        QwirkleLocation location = pieceDisplay.getQwirkleLocation();
                        QwirklePlacement placement = new QwirklePlacement(event.getPiece(), location);
                        PlayPiece proposal = PlayPiece.propose(event.getPlayer(), placement, pieceDisplay.getDisplay());
                        bus.post(proposal);
                    }
                }
            }
        });
    }
}
