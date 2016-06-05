package qwirkle.ui.event;

import qwirkle.game.base.QwirklePlacement;
import qwirkle.ui.view.QwirklePieceDisplay;

/** A piece was clicked on. Currently only used to close hamburger menu.
 *  If we need to, we can add the piece that was clicked on. */
public class PieceClicked {
    private final QwirklePieceDisplay display;

    public PieceClicked(QwirklePieceDisplay display) {
        this.display = display;
    }

    public QwirklePieceDisplay getDisplay() {
        return display;
    }

    public QwirklePlacement getPlacement() {
        return display.getPlacement();
    }
}
