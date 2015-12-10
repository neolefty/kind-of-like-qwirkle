package qwirkle.ui.event;

import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlayer;

/** Someone plays a piece interactively. */
public class PlayPiece {
    private DragPiece pickup;
    private DragPiece drop;
    private Phase phase;

    /** A player {@link Phase#propose}s a play by dragging a piece from one panel & position to another.
     *  The interaction engine {@link Phase#accept}s or {@link Phase#reject}s it.
     *  The turn is finalized by a {@link PlayTurn} event, at which point you can discard all
     *  {@link PlayPiece} events. */
    public enum Phase {
        // propose --> accept --> [TurnCompleted event]
        //           \--> reject
        propose, // a player suggests a move by dragging a piece
        accept, // the game says okay you can do that; anything else?
        reject, // the game says no, you can't do that
    }

    /** A player proposes playing a piece from one board to another. */
    public static PlayPiece propose(DragPiece pickup, DragPiece drop) {
        return new PlayPiece(Phase.propose, pickup, drop);
    }

    public PlayPiece(Phase phase, DragPiece pickup, DragPiece drop) {
        this.phase = phase;
        this.pickup = pickup;
        this.drop = drop;
    }

    /** Accept a single placement. */
    public PlayPiece accept() {
        return new PlayPiece(this, Phase.propose, Phase.accept);
    }

    /** Reject a single placement. */
    public PlayPiece reject() {
        return new PlayPiece(this, Phase.propose, Phase.reject);
    }

    /** Which player is playing? */
    public QwirklePlayer getPlayer() { return pickup.getPlayer(); }

    /** Convenience. */
    public QwirklePiece getPiece() { return pickup.getPiece(); }

    public DragPiece getPickup() { return pickup; }
    public DragPiece getDrop() { return drop; }

    /** What phase of a play is this? Proposal, acceptance, etc. */
    public Phase getPhase() { return phase; }

    public QwirkleLocation getDropLocation() { return drop.getPlacement().getLocation(); }

    /** This play is proposed by a player. */
    public boolean isPhasePropose() { return phase == Phase.propose; }
    /** This play has been accepted by the board as legal. */
    public boolean isPhaseAccept() { return phase == Phase.accept; }
    /** This play has been rejected by the board, probably because it was illegal. */
//    public boolean isPhaseReject() { return phase == Phase.reject; }

    public boolean isPickupHand() { return pickup.isGridHand(); }
    public boolean isPickupDiscard() { return pickup.isGridDiscard(); }
    public boolean isPickupGameboard() { return pickup.isGridGameboard(); }

    public boolean isDropHand() { return drop.isGridHand(); }
    public boolean isDropDiscard() { return drop.isGridDiscard(); }
    public boolean isDropGameboard() { return drop.isGridGameboard(); }

    public String toString() {
        // propose move red square at 3, 2 from hand to 1, -1 on gameboard
        return phase
                + " move " + pickup.getPiece()
                + " from " + pickup.getLocation()
                + " on " + pickup.getDisplayType()
                + " to " + drop.getLocation()
                + " on " + drop.getDisplayType();
    }

    private PlayPiece(PlayPiece previous, Phase requiredPhase, Phase newPhase) {
        if (previous.getPhase() != requiredPhase)
            throw new IllegalStateException
                    ("Can only " + newPhase + " a " + requiredPhase + " -- not " + previous);

        this.pickup = previous.pickup;
        this.drop = previous.drop;
        this.phase = newPhase;
    }
}
