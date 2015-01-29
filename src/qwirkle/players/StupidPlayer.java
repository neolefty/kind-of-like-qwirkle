package qwirkle.players;

import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;
import qwirkle.game.QwirklePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StupidPlayer implements QwirklePlayer{
    private String name;

    public StupidPlayer(String name) { this.name = name; }

    @Override
    public List<QwirklePlacement> play(QwirkleBoard board, List<QwirklePiece> hand) {
        List<QwirklePlacement> result = new ArrayList<>();
        for (QwirklePiece piece : hand) {
            Collection<QwirklePlacement> legal
                    = board.getLegalPlacements(piece);
            if (!legal.isEmpty()) {
                result.add(legal.iterator().next());
                break;
            }
        }
        // if no legal moves, result will be empty
        return result;
    }

    /** Can't play anything? Discard whole hand. */
    @Override
    public List<QwirklePiece> discard(QwirkleBoard board, List<QwirklePiece> hand) {
        return hand;
    }

    @Override
    public String getName() { return "Stupid " + name; }
}
