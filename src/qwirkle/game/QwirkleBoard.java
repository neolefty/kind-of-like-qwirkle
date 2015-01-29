package qwirkle.game;

import java.util.Collection;

/** The current state of a Qwirkle board. */
public interface QwirkleBoard extends QwirkleGrid {
    /** The settings this game is being played under. */
    QwirkleSettings getSettings();

    /** The lines of pieces on the board. Includes lines of size 1. */
    Collection<QwirkleLine> getLines();

    /** What are the legal places to place a certain piece? */
    Collection<QwirklePlacement> getLegalPlacements(QwirklePiece piece);

    /** If the current player plans to place <tt>play</tt>,
     *  what are the legal places they can also put <tt>piece</tt>?
     *  @param play Placements that are already planned. Must be a legal move.
     *  @param piece The additional piece the player wishes to put on the board,
     *               along with the pieces in <tt>play</tt>.
     *  @return All the places <tt>piece</tt> can go. Empty if no possibilities. */
    Collection<QwirklePlacement> getLegalPlacements
    (Collection<QwirklePlacement> play, QwirklePiece piece);

    /** Play some tiles on the board and return the result.
     *  Doesn't change the existing board.
     *  @return a new board, with the pieces played on it */
    QwirkleBoard play(Collection<QwirklePlacement> play);

    /** Play a single tile on the board and return the result.
     *  Doesn't change the existing board.
     *  @return a new board, with the pieces played on it. */
    QwirkleBoard play(QwirklePlacement next);

    /** Returns the state of the board before the last play.
     *  Note: has no effect on this board.
     *  Null if there are no moves (size() == 0). */
    QwirkleBoard getUndo();

    /** How many turns have happened in this game so far? */
    int getTurnCount();

    /** The score of the last play.
     *  Note: To get the score of a potential move,
     *  use <tt>board.play(placements).getLastScore()</tt>. */
    int getLastScore();

    /** The last play that was made. Null if this board is empty.
     *  If the last play was a single piece, this
     *  collection's size() will be 1. */
    Collection<QwirklePlacement> getLastPlay();

    /** Is it legal to add this piece to the board? */
    boolean isLegal(QwirklePlacement placement);

    /** Is this a legal move that a player could make? */
    boolean isLegal(Collection<QwirklePlacement> play);
}