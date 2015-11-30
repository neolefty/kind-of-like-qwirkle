package qwirkle.ui.swing.game;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirklePiece;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;

import java.util.ArrayList;
import java.util.List;

/** A panel you can discard pieces into. */
public class DiscardGridPanel extends QwirkleGridPanel {
    private QwirkleUIController controller;
    private boolean vertical;
    private List<QwirklePiece> pieces = new ArrayList<>();
    // the bus for managing events in the discard grid
    private EventBus discardBus;

    public DiscardGridPanel(QwirkleUIController controller) {
        super(new EventBus("discards"));
        discardBus = super.getEventBus();
        this.controller = controller;

        discardBus.register(new Object() {
            // on drag, remove from panel
            @Subscribe public void drag(DragPiece event) {
                if (event.isPickup()) {

                }
            }
        });
    }

    public void setVertical(boolean vertical) {
//        update();
    }
}
