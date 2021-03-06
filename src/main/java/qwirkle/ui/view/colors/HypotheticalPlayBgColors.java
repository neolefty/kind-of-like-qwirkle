package qwirkle.ui.view.colors;

import qwirkle.game.base.QwirkleColor;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;

/** Background for a hypothetical play. */
public class HypotheticalPlayBgColors implements ColorSet {
    private QwirkleColor pieceColor;

    public HypotheticalPlayBgColors(QwirkleColor pieceColor) { this.pieceColor = pieceColor; }
    public HypotheticalPlayBgColors(QwirklePiece piece) { this(piece.getColor()); }
    public HypotheticalPlayBgColors(QwirklePlacement placement) { this(placement.getColor()); }

    /** Legal destination. */
    @Override public QwirkleColor getNormal() { return pieceColor.darker().darker().darker(); }

    /** Hover over a legal destination. */
    @Override public QwirkleColor getHighlight() { return pieceColor.darker().darker(); }

    /** Where the piece came from (because the mouse is pressed, and this is focused, during a drag) */
    @Override public QwirkleColor getActivated() { return pieceColor.darker(); }

    @Override
    public String toString() {
        return "hypothetical highlight based on " + pieceColor + ": [" + getNormal() + ", " + getHighlight() + ", " + getActivated() + "]";
    }
}
