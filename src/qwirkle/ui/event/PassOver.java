package qwirkle.ui.event;

import qwirkle.ui.view.QwirklePieceDisplay;

/** The mouse passed into or out of a {@link QwirklePieceDisplay}. */
public class PassOver {
    private QwirklePieceDisplay display;
    private boolean enter;

    public PassOver(QwirklePieceDisplay display, boolean enter) {
        this.display = display;
        this.enter = enter;
    }

    /** The display that was passed over. */
    public QwirklePieceDisplay getDisplay() { return display; }

    /** Enter (true) or exit (false)? */
    public boolean isEnter() { return enter; }

    @Override
    public String toString() {
        return (enter ? "Entering " : "Exiting ")
                + (display.getPiece() == null ? "empty spot" : display.getPiece())
                + " at " + display.getQwirkleLocation();
    }
}
