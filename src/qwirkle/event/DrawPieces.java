package qwirkle.event;

import qwirkle.game.AsyncPlayer;
import qwirkle.game.QwirklePiece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Event: A player drew some pieces at the end of their turn. */
public class DrawPieces {
    private List<QwirklePiece> pieces;
    private AsyncPlayer player;
    public DrawPieces(AsyncPlayer player, Collection<QwirklePiece> pieces) {
        this.player = player;
        this.pieces = Collections.unmodifiableList(new ArrayList<>(pieces));
    }

    public List<QwirklePiece> getDrawn() { return pieces; }
    public AsyncPlayer getPlayer() { return player; }

    @Override
    public String toString() {
        return player + " drew " + pieces;
    }
}
