package qwirkle.ui.board;

import qwirkle.game.HasQwirkleLocation;
import qwirkle.game.QwirklePiece;

/** A UI component that displays a {@link QwirklePiece}. */
public interface QwirklePieceDisplay extends HasQwirkleLocation {
    /** The piece that this displays. Null if empty. */
    QwirklePiece getPiece();

    /** The size of the piece displayed, in screen pixels. */
    int getPieceHeight();

    /** The size of the piece displayed, in screen pixels. */
    int getPieceWidth();
}
