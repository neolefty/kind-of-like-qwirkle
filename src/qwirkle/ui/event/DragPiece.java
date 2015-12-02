package qwirkle.ui.event;

import qwirkle.game.base.*;
import qwirkle.ui.control.DragToPlayPromoter;

/** A piece is being dragged -- picked up or put down or cancelled.
 *  Note that we don't track the destination in this event.
 *  Instead, destination is determined by {@link DragToPlayPromoter}
 *  and can be subscribed to via {@link PlayPiece} events. */
public class DragPiece {
    public enum Action {
        PICKUP, SUSTAIN, DROP, CANCEL
    }

    private QwirklePlayer player;
    private QwirkleGrid sourceGrid;
    private QwirkleLocation sourceLocation;
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
        this.sourceLocation = precedent.sourceLocation;
        this.action = action;
    }

    private void checkStartOrSustain(Action action) {
        if (getAction() != Action.PICKUP && getAction() != Action.SUSTAIN)
            throw new IllegalStateException
                    ("Can't " + action.toString().toLowerCase() + " a " + getAction());
    }

    /** Drop a piece. */
    public DragPiece drop() {
        checkStartOrSustain(Action.DROP);
        return new DragPiece(this, Action.DROP);
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

    /** The location being picked up from. */
    public QwirkleLocation getSourceLocation() { return sourceLocation; }

    /** The placement being picked up. */
    public QwirklePlacement getSourcePlacement() { return sourceGrid.getPlacement(sourceLocation); }

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
        return action + " " + getPiece() + " from " + getSourceLocation();
    }
}
