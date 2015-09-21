package qwirkle.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.event.PassOver;
import qwirkle.event.PieceDrag;
import qwirkle.event.PiecePlay;
import qwirkle.game.QwirkleLocation;
import qwirkle.game.QwirklePlacement;
import qwirkle.ui.QwirklePieceDisplay;

/** Watches PieceDrag and PassOver events to catch when someone
 *  is trying to play a piece by dragging and dropping.
 *  Note: doesn't guarantee that the attempt to play is legal. */
public class PieceDropWatcher {
    private QwirklePieceDisplay lastDisplay;

    public PieceDropWatcher(final EventBus bus) {
        bus.register(new Object() {
            @Subscribe
            public void passOver(PassOver event) {
                if (event.isEnter())
                    lastDisplay = event.getDisplay();
                else
                    lastDisplay = null;
            }

            @Subscribe
            public void drag(PieceDrag event) {
                if (event.isDrop()) {
                    QwirklePieceDisplay display = lastDisplay; // avoid concurrency problems by grabbing a temp copy
                    if (display != null && display.getPiece() == null) {
                        QwirkleLocation location = display.getQwirkleLocation();
                        QwirklePlacement placement = new QwirklePlacement(event.getPiece(), location);
                        bus.post(PiecePlay.propose(placement));
                    }
                }
            }
        });
    }
}
