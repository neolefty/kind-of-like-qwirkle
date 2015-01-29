package qwirkle.game;

import qwirkle.control.GameStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** A single turn in Qwirkle. Pieces placed on a grid by a player.
 *  Immutable except for getStatus(), which points to a long-lived object that tracks changes to the game. */
public class QwirkleTurn {
    private QwirklePlayer player;
    private GameStatus status;
    private int score;
    private List<QwirklePlacement> placements;
    // the board *after* the placements have been made
    private QwirkleGrid grid;
    // bonus points at the end for finishing first, equal to number of opponents remaining tiles
    private int bonus;
    private int discardCount;

    /** <tt>player</tt> played <tt>placements</tt> for <tt>score</tt> points. */
    public static QwirkleTurn play
            (GameStatus status, Collection<QwirklePlacement> placements,
             QwirklePlayer player, int score)
    {
        if (placements == null)
            throw new NullPointerException("placements collection is null");
        if (score <= 0)
            throw new IllegalArgumentException("score is " + score);
        if (placements.isEmpty())
            throw new IllegalArgumentException("placements collection is empty");
        List<QwirklePlacement> placeList = Collections.unmodifiableList(new ArrayList<>(placements));
        return new QwirkleTurn(status, null, placeList, player, score, 0); // 0 discards
    }

    /** <tt>player</tt> discarded <tt>discardCount</tt> pieces. */
    public static QwirkleTurn discard(GameStatus status, QwirklePlayer player, int discardCount) {
        if (discardCount < 0)
            throw new IllegalArgumentException("Discard count is " + discardCount);
        return new QwirkleTurn(status, null, null, player, 0, discardCount); // 0 score, null placements
    }

    /** A player is dealt new tiles to fill their hand back up after a discard or play.
     *  In this case, though, the board represents their hand, including the new pieces,
     *  and the placements are the new pieces (to highlight).
     *  The orientation of the board etc. is left up to the caller. */
    public static QwirkleTurn drawToHand
            (QwirkleGrid hand, Collection<QwirklePlacement> draw, QwirklePlayer player)
    {
        List<QwirklePlacement> drawList = Collections.unmodifiableList(new ArrayList<>(draw));
        return new QwirkleTurn(null, hand, drawList, player, 0, 0);
    }

    /** Give a bonus at the end of the game, to the player who finishes first,
     *  equal to the sum of the pieces still held by the other players. */
    public QwirkleTurn bonus(int bonus) {
        if (isDiscard())
            throw new IllegalStateException("Can't get a bonus (" + bonus + ") after passing.");
        else if (bonus < 0)
            throw new IllegalStateException("Bonus is negative: " + bonus);
        QwirkleTurn result = new QwirkleTurn(status, null, placements, player, score, discardCount);
        result.bonus = bonus;
        return result;
    }

    /** Construct a turn. Exactly one of status or grid should be non-null.
     *  If status is non-null, the board state will be gotten from status.getBoard().
     *  @param status the state of the game. Only null if board is not null.
     *  @param board the board, in its state after the play. Only null if status is not null.
     *  @param placements what pieces were played. May not be null or zero-length.
     *  @param player the player who made the play. Not null.
     *  @param score the score for the play. */
    private QwirkleTurn
            (GameStatus status, QwirkleGrid board, List<QwirklePlacement> placements,
             QwirklePlayer player, int score, int discardCount)
    {
        if (board == null && status == null)
            throw new NullPointerException("Status and board are both null.");
        else if (board != null && status != null)
            throw new IllegalArgumentException("Status and board are both non-null.");
        if (player == null)
            throw new NullPointerException("Player is null.");
        this.status = status;
        this.grid = (board == null ? status.getBoard() : board);
        this.placements = placements;
        this.player = player;
        this.score = score;
        this.discardCount = discardCount;
    }

    /** What pieces were played. Null iff a discard or pass. */
    public List<QwirklePlacement> getPlacements() { return placements; }

    /** The board, in its state after the play. Never null. */
    public QwirkleGrid getGrid() { return grid; }

    /** The game status. Only valid until the next turn is played, because it is mutable. */
    public GameStatus getStatus() { return status; }

    /** The player who made the play. Never null. */
    public QwirklePlayer getPlayer() { return player; }

    /** The total score for this play. Zero if a discard or pass. Greater than zero otherwise. */
    public int getScore() { return score + bonus; }

    /** The bonus score for finishing first, equal to the number of opponents' remaining pieces. */
    public int getBonus() { return bonus; }

    /** How many pieces did the player discard? */
    public int getDiscardCount() { return discardCount; }

    /** Was this turn a discard? Includes passes (0-card discards). */
    public boolean isDiscard() { return placements == null; }

    public boolean isDrawToHand() { return score == 0 && !isDiscard(); }

    @Override
    public String toString() {
        if (isDiscard())
            return player.getName() + (getDiscardCount() == 0 ? " passes." : " discards " + getDiscardCount() + " pieces.");
        else if (isDrawToHand())
            return player.getName() + " draws " + placements + ".";
        else
            return player.getName() + " plays " + placements + " for " + score + " points"
                    + (bonus == 0 ? "" : " plus " + bonus + " for finishing first, for a total of " + getScore())
                    + ". New board state is: \n" + grid;
    }

    public String getSummary() {
        if (isDiscard())
            return player.getName() + (getDiscardCount() == 0 ? " passes." : " discards " + getDiscardCount() + " pieces.");
        else if (isDrawToHand())
            return player.getName() + " draws " + placements.size() + " pieces.";
        else
            return player.getName() + " plays " + placements.size() + " pieces for " + getScore() + " points"
                    + (bonus == 0 ? "" : " (bonus + " + bonus + ")") + ".";
    }

    /** Does this turn include a placement at <tt>location</tt>? */
    public boolean containsLocation(QwirkleLocation location) {
        if (placements != null)
            for (QwirklePlacement p : placements)
                if (p.getLocation().equals(location))
                    return true;
        return false;
    }

    /** Does this turn include a placement at (x, y)? */
    public boolean containsLocation(int x, int y) {
        return containsLocation(new QwirkleLocation(x, y));
    }
}
