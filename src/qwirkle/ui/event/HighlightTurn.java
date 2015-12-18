package qwirkle.ui.event;

import qwirkle.game.event.TurnCompleted;
import qwirkle.ui.view.QwirkleGridDisplay;

/** An event that indicates a turn needs to be highlighted or unhighlighted. */
public class HighlightTurn {
    private final QwirkleGridDisplay.DisplayType type;
    private TurnCompleted turn;
    private boolean highlighted;

    public HighlightTurn(TurnCompleted turn, QwirkleGridDisplay.DisplayType type, boolean highlighted) {
        if (turn == null)
            throw new NullPointerException("Turn is null.");
        this.type = type;
        this.turn = turn;
        this.highlighted = highlighted;
    }

    public TurnCompleted getTurn() { return turn; }
    public QwirkleGridDisplay.DisplayType getType() { return type; }
    public boolean isHighlighted() { return highlighted; }
}
