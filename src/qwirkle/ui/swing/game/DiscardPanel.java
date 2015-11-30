package qwirkle.ui.swing.game;

import com.google.common.eventbus.EventBus;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;

/** A panel you can discard pieces into. */
public class DiscardPanel extends QwirkleGridPanel {
    public DiscardPanel(EventBus bus) {
        super(bus);
    }
}
