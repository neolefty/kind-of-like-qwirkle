package qwirkle.ui.swing.game;

import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.impl.QwirkleGridImpl;
import qwirkle.game.event.TurnCompleted;
import qwirkle.ui.control.DiscardTracker;
import qwirkle.ui.event.UpdateDiscards;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;

// TODO highlight when discarding is allowed
// TODO allow changing your mind -- drag out of discard pile
/** A panel you can discard pieces into. */
public class DiscardGridPanel extends QwirkleGridPanel {
    private DiscardTracker controller;

    public DiscardGridPanel(final DiscardTracker controller) {
        super(controller.getEventBus(), DisplayType.discard);
        super.setBlankIncluded(false);

        this.controller = controller;

        // listen for updates
        controller.getEventBus().register(new Object() {
            @Subscribe public void updateDiscards(UpdateDiscards event) {
                // be sure to show the empty spots
                setAlwayShown(event.getAlsoVisible(), false);
                setGrid(new QwirkleGridImpl(event.getPlacements()));
                if (controller.getCurPlayer() != null)
                    makeDraggable(controller.getCurPlayer());
            }
            @Subscribe public void clearOnTurnEnd(TurnCompleted event) {
                setGrid(null);
            }
        });
    }

    public void setVertical(boolean vertical) {
        controller.setVertical(vertical);
    }
}
