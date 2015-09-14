package qwirkle.ui.board;

import qwirkle.game.QwirkleGrid;

import java.awt.*;

/** A UI element that displays a {@link QwirkleGrid}. */
public interface QwirkleGridDisplay {
    /** The grid that this displays. */
    QwirkleGrid getGrid();

    /** The size of a piece, in pixels. */
    Dimension getPieceSize();

    /** The {@link QwirklePieceDisplay} at the given coordinates (local to this).
     *  Null if there is no piece display at this location. */
    QwirklePieceDisplay getPieceDisplay(int x, int y);
}
