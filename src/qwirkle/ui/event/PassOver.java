package qwirkle.ui.event;

import qwirkle.ui.QwirklePieceDisplay;

/** The mouse passed over a {@link QwirklePieceDisplay}. */
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
        return (enter ? "Entering " : "Exiting ") + display.getPiece()
                + " (" + display.getPieceWidth() + "x" + display.getPieceHeight();
    }
}
