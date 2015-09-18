package qwirkle.attic;

import com.google.common.eventbus.EventBus;
import qwirkle.control.GameStatus;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Lock the hands and deck from being changed outside of this manager. */
class PiecesListWriterImpl extends WriterImpl<List<QwirklePiece>> implements PiecesListWriter {
    // To change, modify this raw list and then call change()
    private List<QwirklePiece> raw = new ArrayList<>();
    private List<QwirklePiece> unmod;
    public PiecesListWriterImpl(GameStatus status, EventBus bus) {
        super(status, bus);
        set(Collections.unmodifiableList(raw));
    }

    /** an unmodifiable list. */
    @Override
    public List<QwirklePiece> get() {
        if (unmod == null)
            unmod = Collections.unmodifiableList(raw);
        return unmod;
    }

    @Override public void removePlacements(Collection<QwirklePlacement> placements) {
        for (QwirklePlacement placement : placements)
            raw.remove(placement.getPiece());
        changed();
    }
    @Override public boolean add(QwirklePiece piece) {
        boolean result = raw.add(piece);
        changed();
        return result;
    }
    @Override public void addAll(Collection<QwirklePiece> pieces) {
        raw.addAll(pieces);
        changed();
    }
    @Override public void removePieces(Collection<QwirklePiece> pieces) {
        raw.removeAll(pieces);
        changed();
    }
    @Override public void clear() {
        raw.clear();
        changed();
    }
    @Override public QwirklePiece remove(int index) {
        QwirklePiece result = raw.remove(index);
        changed();
        return result;
    }

    @Override
    public String toString() {
        return QwirklePiece.abbrev(get());
    }
}
