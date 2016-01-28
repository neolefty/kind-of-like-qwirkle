package qwirkle.game.event;

import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlayer;

import java.util.List;

/** Event: A player drew some pieces at the end of their turn. */
public class DrawPieces {
    private List<QwirklePiece> drawn;
    private List<QwirklePiece> curHand;
    private QwirklePlayer player;

    public DrawPieces(QwirklePlayer player, List<QwirklePiece> drawn, List<QwirklePiece> hand) {
        this.player = player;
        this.drawn = drawn;
        this.curHand = hand;
    }

    /** This player's current hand. */
    public List<QwirklePiece> getHand() { return curHand; }
    /** The pieces that were drawn. */
    public List<QwirklePiece> getDrawn() { return drawn; }
    /** The player who drew some pieces. */
    public QwirklePlayer getPlayer() { return player; }

    @Override
    public String toString() { return player + " drew " + drawn; }
}
