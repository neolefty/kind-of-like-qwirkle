package qwirkle.event;

import qwirkle.game.QwirklePlacement;

/** Someone wants to play a piece interactively. */
public class PiecePlay {
    private QwirklePlacement placement;

    public PiecePlay(QwirklePlacement placement) { this.placement = placement; }

    public QwirklePlacement getPlacement() { return placement; }

/*
    private boolean confirmed = false;
    private boolean rejected = false;

    public PiecePlay confirm() {
        PiecePlay result = new PiecePlay(placement);
        checkNotCorR();
        result.confirmed = true;
        return result;
    }

    public PiecePlay reject() {
        PiecePlay result = new PiecePlay(placement);
        checkNotCorR();
        result.rejected = true;
        return result;
    }

    public boolean isConfirmed() { return confirmed; }
    public boolean isRejected() { return rejected; }

    private void checkNotCorR() {
        if (confirmed)
            throw new IllegalStateException("Already confirmed.");
        if (rejected)
            throw new IllegalStateException("Already rejected");
    }
*/
}
