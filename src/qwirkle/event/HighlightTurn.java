package qwirkle.event;

/** An event that indicates a turn needs to be highlighted or unhighlighted. */
public class HighlightTurn {
    private QwirkleTurn turn;
    private boolean highlighted;

    public HighlightTurn(QwirkleTurn turn, boolean highlighted) {
        if (turn == null)
            throw new NullPointerException("Turn is null.");
        this.turn = turn;
        this.highlighted = highlighted;
    }

    public QwirkleTurn getTurn() { return turn; }

    public boolean isHighlighted() { return highlighted; }
}
