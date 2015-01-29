package qwirkle.game;

import java.util.Collection;

/** A grid of Qwirkle pieces. */
public interface QwirkleGrid {
    /** The locations of all pieces on the board. */
    Collection<QwirklePlacement> getPlacements();

    /** How many pieces are on the board? */
    int size();

    /** Get the piece at a certain location.
     *  @return null if there is no piece there. */
    QwirklePiece get(QwirkleLocation location);

    /** Get the QwirklePlacement at a certain location.
     *  @return null if there is no piece there. */
    QwirklePlacement getPlacement(QwirkleLocation location);
    QwirklePlacement getPlacement(int x, int y);

    public interface GridWalker extends Iterable<LineWalker> {}
    public interface LineWalker extends Iterable<QwirkleLocation> {}
    
    /** An iterator of that traverses the grid along horizontal lines -- y then x.
     *  @param padding empty squares to include around the edge. */
    GridWalker getHorizontalWalker(int padding);

    /** An iterator that traverses the grid along vertical lines -- x then y.
     *  @param padding empty squares to include around the edge. */
    GridWalker getVerticalWalker(int padding);

    /** The current left edge of the board. */
    int getXMin();

    /** The current right edge of the board. */
    int getXMax();

    /** The current bottom edge of the board. */
    int getYMin();

    /** The current top edge of the board. */
    int getYMax();

    /** The width of the board, in tiles. */
    int getWidth();

    /** The height of the board, in tiles. */
    int getHeight();

    /** Render this as a String, with each line prefixed by <tt>linePrefix</tt>. */
    String toString(String linePrefix);
}
