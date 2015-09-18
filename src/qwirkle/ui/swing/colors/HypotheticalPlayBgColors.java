package qwirkle.ui.swing.colors;

import qwirkle.game.QwirkleColor;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;

import java.awt.*;

/** Background for a hypothetical play. */
public class HypotheticalPlayBgColors implements ColorSet {
    private QwirkleColor pieceColor;

    public HypotheticalPlayBgColors(QwirkleColor pieceColor) { this.pieceColor = pieceColor; }
    public HypotheticalPlayBgColors(QwirklePiece piece) { this(piece.getColor()); }
    public HypotheticalPlayBgColors(QwirklePlacement placement) { this(placement.getColor()); }

    @Override
    public Color getNormal() {
        return pieceColor.getColor().darker().darker();
    }

    @Override
    public Color getHighlight() {
        return pieceColor.getColor().darker();
    }

    @Override
    public Color getActivated() {
        return pieceColor.getColor().brighter();
    }
}
