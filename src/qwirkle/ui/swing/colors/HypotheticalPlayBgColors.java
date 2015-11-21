package qwirkle.ui.swing.colors;

import qwirkle.game.base.QwirkleColor;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;

import java.awt.*;

/** Background for a hypothetical play. */
public class HypotheticalPlayBgColors implements ColorSet {
    private QwirkleColor pieceColor;

    public HypotheticalPlayBgColors(QwirkleColor pieceColor) { this.pieceColor = pieceColor; }
    public HypotheticalPlayBgColors(QwirklePiece piece) { this(piece.getColor()); }
    public HypotheticalPlayBgColors(QwirklePlacement placement) {
        this(placement.getColor());
    }

    @Override
    public Color getNormal() {
        return new Color(pieceColor.getColorInt()).darker().darker();
    }

    @Override
    public Color getHighlight() {
        return new Color(pieceColor.getColorInt()).darker();
    }

    @Override
    public Color getActivated() {
        return new Color(pieceColor.getColorInt()).brighter();
    }
}
