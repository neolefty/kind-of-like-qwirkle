package qwirkle.ui;

import qwirkle.game.base.QwirkleGrid;

/** A UI element that displays a {@link QwirkleGrid}. */
public interface QwirkleGridDisplay {
    /** The grid that this displays. */
    QwirkleGrid getGrid();

    /** The width of a piece, in pixels. */
    int getPieceWidth();

    /** The height of a piece, in pixels. */
    int getPieceHeight();

    /** The {@link QwirklePieceDisplay} at the given coordinates (local to this).
     *  Null if there is no piece display at this location. */
    QwirklePieceDisplay getPieceDisplay(int x, int y);
}
