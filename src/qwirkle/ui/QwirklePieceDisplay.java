package qwirkle.ui;

import qwirkle.game.base.HasQwirkleLocation;
import qwirkle.game.base.QwirklePiece;

/** A UI component that displays a {@link QwirklePiece}. */
public interface QwirklePieceDisplay extends HasQwirkleLocation {
    /** The piece that this displays. Null if empty. */
    QwirklePiece getPiece();

    /** The size of the piece displayed, in screen pixels. */
    int getPieceHeight();

    /** The size of the piece displayed, in screen pixels. */
    int getPieceWidth();

    /** The grid display that this is part of. Not null (so far ...). */
    QwirkleGridDisplay getDisplay();
}
