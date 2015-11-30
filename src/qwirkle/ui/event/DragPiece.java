package qwirkle.ui.event;

import qwirkle.game.base.*;

/** A piece is being dragged -- picked up or put down or cancelled. */
public class DragPiece {
    public enum Action {
        PICKUP, SUSTAIN, DROP, CANCEL
    }

    private QwirklePlayer player;
    private QwirkleGrid sourceGrid, destGrid;
    private QwirkleLocation sourceLocation, destLocation;
    private Action action;

    private DragPiece(QwirklePlayer player, QwirkleGrid sourceGrid, QwirkleLocation location, Action action) {
        this.player = player;
        this.sourceGrid = sourceGrid;
        this.sourceLocation = location;
        this.action = action;
        if (action != Action.PICKUP)
            throw new IllegalArgumentException("Can't create " + action + " event from scratch: " + this);
    }

    /** Create a drag event that continues a drag action. */
    private DragPiece(DragPiece precedent, Action action) {
        this.player = precedent.player;
        this.sourceGrid = precedent.sourceGrid;
        this.destGrid = precedent.destGrid;
        this.sourceLocation = precedent.sourceLocation;
        this.destLocation = precedent.destLocation;
        this.action = action;
    }

    private void checkStartOrSustain(Action action) {
        if (getAction() != Action.PICKUP && getAction() != Action.SUSTAIN)
            throw new IllegalStateException
                    ("Can't " + action.toString().toLowerCase() + " a " + getAction());
    }

    /** Drop a piece. */
    public DragPiece drop(QwirkleGrid destGrid, QwirkleLocation location) {
        checkStartOrSustain(Action.DROP);
        DragPiece result = new DragPiece(this, Action.DROP);
        result.destGrid = destGrid;
        result.destLocation = location;
        return result;
    }

    /** Keep dragging. */
    public DragPiece sustain() {
        checkStartOrSustain(Action.SUSTAIN);
        return new DragPiece(this, Action.SUSTAIN);
    }

    /** Keep dragging. */
    public DragPiece cancel() {
        checkStartOrSustain(Action.CANCEL);
        return new DragPiece(this, Action.CANCEL);
    }

    /** The player who is dragging. */
    public QwirklePlayer getPlayer() { return player; }

    /** The grid this piece is being picked up from. */
    public QwirkleGrid getSourceGrid() { return sourceGrid; }

    /** The grid this is piece is being dropped on. */
    public QwirkleGrid getDestGrid() { return destGrid; }

    /** The location being picked up from. */
    public QwirkleLocation getSourceLocation() { return sourceLocation; }

    /** The location being dropped on (null if none yet). */
    public QwirkleLocation getDestLocation() { return destLocation; }

    /** The placement being picked up. */
    public QwirklePlacement getSourcePlacement() { return sourceGrid.getPlacement(sourceLocation); }

    /** The placement being dropped (null if none yet). */
    public QwirklePlacement getDestPlacement() {
        return destLocation != null && destGrid != null ? destGrid.getPlacement(destLocation) : null;
    }

    /** The piece being picked up or dropped. */
    public QwirklePiece getPiece() { return sourceGrid.get(sourceLocation); }

    /** Is this a pickup, drop, sustain, cancel? */
    public Action getAction() { return action; }

    public boolean isPickup() { return action == Action.PICKUP; }
    public boolean isSustain() { return action == Action.SUSTAIN; }
    public boolean isDrop() { return action == Action.DROP; }
    public boolean isCancel() { return action == Action.CANCEL; }

    /** Convenience method. */
    public static DragPiece createPickup(QwirklePlayer player, QwirkleGrid sourceGrid, QwirkleLocation location) {
        return new DragPiece(player, sourceGrid, location, Action.PICKUP);
    }

    @Override
    public String toString() {
        return action + " " + getSourcePlacement()
                + (getDestPlacement() == null ? "" : " to " + getDestPlacement());
    }
}
