package qwirkle.ui.event;

import qwirkle.game.base.*;
import qwirkle.ui.QwirkleGridDisplay;
import qwirkle.ui.QwirklePieceDisplay;
import qwirkle.ui.control.DragToPlayPromoter;

/** A piece is being dragged -- picked up or put down or cancelled.
 *  Note that we don't track the destination in this event.
 *  Instead, destination is determined by {@link DragToPlayPromoter}
 *  and can be subscribed to via {@link PlayPiece} events. */
public class DragPiece {
    public enum Action {
        PICKUP, SUSTAIN, DROP, CANCEL
    }

    private DragPiece pickup;
    private QwirklePlayer player;
    private QwirklePieceDisplay display;
    private QwirkleGrid grid;
    private QwirklePlacement placement;
    private Action action;

    /** Initiate a drag action. Player and display must be non-null. */
    public static DragPiece createPickup(QwirklePlayer player, QwirklePieceDisplay display) {
        if (display == null)
            throw new NullPointerException("display is null");
        if (player == null)
            throw new NullPointerException("player is null");
        return new DragPiece(player, display, Action.PICKUP);
    }

    /** Drop a piece.
     *  @param display the display this piece is being dropped on. Must not be null. */
    public DragPiece drop(QwirklePieceDisplay display) {
        if (display == null)
            throw new NullPointerException("display is null");
        checkStartOrSustain(Action.DROP);
        DragPiece result = new DragPiece(this, display, Action.DROP);
        // often the drop placement will be null, since that display
        // doesn't have a piece yet and therefore can't have a placement
        if (result.placement == null)
            result.placement = new QwirklePlacement(getPiece(), display.getQwirkleLocation());
        return result;
    }

    /** Keep dragging.
     *  @param display the display we're passing over now. May be null. */
    public DragPiece sustain(QwirklePieceDisplay display) {
        checkStartOrSustain(Action.SUSTAIN);
        return new DragPiece(this, display, Action.SUSTAIN);
    }

    /** Cancel dragging. */
    public DragPiece cancel() {
        checkStartOrSustain(Action.CANCEL);
        return new DragPiece(this, this.display, Action.CANCEL);
    }

    public DragPiece getPickup() {
        DragPiece result = (action == Action.PICKUP) ? this : pickup;
        if (result == null)
            throw new IllegalStateException("null pickup: " + this);
        return result;
    }

    private DragPiece(QwirklePlayer player, QwirklePieceDisplay sourceDisplay, Action action) {
        this.player = player;
        this.display = sourceDisplay;
        if (display != null) {
            this.grid = sourceDisplay.getDisplay().getGrid();
            this.placement = sourceDisplay.getPlacement();
        }
        this.action = action;
    }

    /** Create a drag event that continues a drag action onto another display. */
    private DragPiece(DragPiece precedent, QwirklePieceDisplay display, Action action) {
        this(precedent.player, display, action);
        this.pickup = precedent.getPickup();
    }

    private void checkStartOrSustain(Action action) {
        if (getAction() != Action.PICKUP && getAction() != Action.SUSTAIN)
            throw new IllegalStateException
                    ("Can't " + action.toString().toLowerCase() + " a " + getAction());
    }

    public boolean isActionPickup() { return action == Action.PICKUP; }
    public boolean isActionSustain() { return action == Action.SUSTAIN; }
    public boolean isActionDrop() { return action == Action.DROP; }
    public boolean isActionCancel() { return action == Action.CANCEL; }

    public boolean isGridDiscard() { return getDisplayType() == QwirkleGridDisplay.DisplayType.discard; }
    public boolean isGridHand() { return getDisplayType() == QwirkleGridDisplay.DisplayType.hand; }
    public boolean isGridGameboard() { return getDisplayType() == QwirkleGridDisplay.DisplayType.gameboard; }

    /** The player who is dragging. */
    public QwirklePlayer getPlayer() { return player; }

    /** Is this a pickup, drop, sustain, cancel? */
    public Action getAction() { return action; }

    public QwirkleGridDisplay.DisplayType getDisplayType() {
        return display.getDisplay().getDisplayType();
    }

    public QwirklePieceDisplay getPieceDisplay() { return display; }
    public QwirkleGridDisplay getGridDisplay() { return display.getDisplay(); }

    /** The grid this piece is being picked up from or dropped onto. */
    public QwirkleGrid getGrid() { return grid; }

    public QwirkleLocation getLocation() { return placement.getLocation(); }

    public QwirklePlacement getPlacement() { return placement; }

    public QwirklePiece getPiece() { return getPickup().getPlacement().getPiece(); }

    @Override
    public String toString() {
        return action + " " + placement + " on " + getDisplayType()
                + (pickup == null ? "" : " from " + pickup.placement + " on " + pickup.getDisplayType());
    }
}
