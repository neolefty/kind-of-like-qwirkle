package qwirkle.ui.event;

import qwirkle.game.base.*;

/** A piece is being dragged -- picked up or put down or cancelled. */
public class DragPiece {
    public enum Action {
        PICKUP, SUSTAIN, DROP, CANCEL
    }

    private QwirklePlayer player;
    private QwirkleGrid grid;
    private QwirkleLocation location;
    private Action action;

    /** Create a drag event. */
    private DragPiece(QwirklePlayer player, QwirkleGrid grid, QwirkleLocation location, Action pickup) {
        this.player = player;
        this.grid = grid;
        this.location = location;
        this.action = pickup;
    }

    private void checkStartOrSustain(Action action) {
        if (getAction() != Action.PICKUP && getAction() != Action.SUSTAIN)
            throw new IllegalStateException
                    ("Can't " + action.toString().toLowerCase() + " a " + getAction());
    }

    /** Drop a piece. */
    public DragPiece drop() {
        checkStartOrSustain(Action.DROP);
        return new DragPiece(getPlayer(), getGrid(), location, Action.DROP);
    }

    /** Keep dragging. */
    public DragPiece sustain() {
        checkStartOrSustain(Action.SUSTAIN);
        return new DragPiece(getPlayer(), grid, location, Action.SUSTAIN);
    }

    /** Keep dragging. */
    public DragPiece cancel() {
        checkStartOrSustain(Action.CANCEL);
        return new DragPiece(getPlayer(), grid, location, Action.CANCEL);
    }

    /** The player who is dragging. */
    public QwirklePlayer getPlayer() { return player; }

    /** The grid this piece is being picked up from. */
    public QwirkleGrid getGrid() { return grid; }

    /** The location being picked up or dropped. */
    public QwirkleLocation getLocation() { return location; }

    /** The placement being picked up or dropped. */
    public QwirklePlacement getPlacement() { return grid.getPlacement(location); }

    /** The piece being picked up or dropped. */
    public QwirklePiece getPiece() { return grid.get(location); }

    /** Is this a pickup, drop, sustain, cancel? */
    public Action getAction() { return action; }

    public boolean isPickup() { return action == Action.PICKUP; }
    public boolean isSustain() { return action == Action.SUSTAIN; }
    public boolean isDrop() { return action == Action.DROP; }
    public boolean isCancel() { return action == Action.CANCEL; }

    /** Convenience method. */
    public static DragPiece createPickup(QwirklePlayer player, QwirkleGrid grid, QwirkleLocation location) {
        return new DragPiece(player, grid, location, Action.PICKUP);
    }

    @Override
    public String toString() {
        return action + " " + getPlacement();
    }
}
