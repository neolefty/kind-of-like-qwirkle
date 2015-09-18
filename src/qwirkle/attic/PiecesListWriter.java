package qwirkle.attic;

import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;

import java.util.Collection;
import java.util.List;

/** For accessors, use get()... */
public interface PiecesListWriter extends Writer<List<QwirklePiece>> {
    void removePlacements(Collection<QwirklePlacement> placements);
    boolean add(QwirklePiece piece);
    void addAll(Collection<QwirklePiece> pieces);
    void removePieces(Collection<QwirklePiece> pieces);
    void clear();
    QwirklePiece remove(int index);
}
