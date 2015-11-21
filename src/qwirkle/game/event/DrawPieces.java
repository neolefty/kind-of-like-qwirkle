package qwirkle.game.event;

import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Event: A player drew some pieces at the end of their turn. */
public class DrawPieces {
    private List<QwirklePiece> pieces;
    private QwirklePlayer player;
    public DrawPieces(QwirklePlayer player, Collection<QwirklePiece> pieces) {
        this.player = player;
        this.pieces = Collections.unmodifiableList(new ArrayList<>(pieces));
    }

    public List<QwirklePiece> getDrawn() { return pieces; }
    public QwirklePlayer getPlayer() { return player; }

    @Override
    public String toString() {
        return player + " drew " + pieces;
    }
}
