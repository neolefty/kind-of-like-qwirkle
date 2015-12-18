package qwirkle.ui.view;

import qwirkle.game.base.HasQwirkleLocation;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.ui.swing.util.HasBackgroundMgr;

/** A UI component that displays a {@link QwirklePiece}. */
public interface QwirklePieceDisplay extends HasQwirkleLocation, HasBackgroundMgr {
    /** The piece that this displays. Null if empty. */
    QwirklePiece getPiece();

    /** The placement that is here. Null if {@link #getPiece()} is empty. */
    QwirklePlacement getPlacement();

    /** The size of the piece displayed, in screen pixels. */
    int getPieceHeight();

    /** The size of the piece displayed, in screen pixels. */
    int getPieceWidth();

    /** The grid display that this is part of. Not null (so far ...). */
    QwirkleGridDisplay getDisplay();
}
