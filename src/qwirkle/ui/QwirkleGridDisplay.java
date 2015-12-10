package qwirkle.ui;

import qwirkle.game.base.QwirkleGrid;
import qwirkle.game.base.QwirkleLocation;

import java.util.Collection;

/** A UI element that displays a {@link QwirkleGrid}. */
public interface QwirkleGridDisplay {
    enum DisplayType {
        discard, hand, gameboard
    }

    /** The grid that this displays. */
    QwirkleGrid getGrid();

    /** The width of a piece, in pixels. */
    int getPieceWidth();

    /** The height of a piece, in pixels. */
    int getPieceHeight();

    /** Highlight certain locations. */
    void setHighlight(Collection<QwirkleLocation> placements);

    /** The {@link QwirklePieceDisplay} at the given coordinates (local to this).
     *  Null if there is no piece display at this location. */
    QwirklePieceDisplay getPieceDisplay(int x, int y);

    /** What kind of display is this? */
    DisplayType getDisplayType();
}
