package qwirkle.game;

import java.util.Collection;
import java.util.List;

// TODO: write a cooperative player that tries to maximize harmonious plays (to make 6x6 blocks) -- alt win = locked 6x6 game :)
public interface QwirklePlayer {
    /** Choose where to play.
     *  @param board The current state of the game
     *  @param hand The pieces you have
     *  @return a list of placements (must be legal).
     *  If the player returns an empty list or null, the player will be given a chance to discard. */
    Collection<QwirklePlacement>
    play(QwirkleBoard board, List<QwirklePiece> hand);

    /** Discard some tiles.
     *  This method will be called if you returned an empty list or null from play().
     *  @return the list of tiles you wish to discard. */
    Collection<QwirklePiece>
    discard(QwirkleBoard board, List<QwirklePiece> hand);

    String getName();
}
