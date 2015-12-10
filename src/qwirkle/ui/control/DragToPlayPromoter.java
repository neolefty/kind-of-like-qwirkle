package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.event.PlayPiece;

/** Watches PieceDrag events to catch when someone
 *  is trying to play a piece by dragging and dropping.
 *  Note: doesn't care whether the attempt to play is legal. */
public class DragToPlayPromoter {
    public DragToPlayPromoter(final EventBus bus) {
        bus.register(new Object() {
            @Subscribe
            public void drag(DragPiece event) {
                // if it's a drop, and it's over a QwirklePieceDisplay,
                if (event.isActionDrop()) {
                    bus.post(PlayPiece.propose(event.getPickup(), event));
                }
            }
        });
    }
}
