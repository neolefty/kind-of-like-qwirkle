package qwirkle.event;

import qwirkle.game.AsyncPlayer;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;

// TODO when all the plays have been chosen for a turn, bundle them up into a single event
/** Someone plays a piece interactively. */
public class PlayPiece {
    private QwirklePlacement placement;
    private AsyncPlayer player;
    private Phase phase;
    private PlayPiece previous;

    /** A player {@link Phase#propose}s a play by dragging a piece.
     *  The game {@link Phase#accept}s or {@link Phase#reject}s it.
     *  Later, it may be either {@link Phase#cancel}ed or {@link Phase#confirm}ed. */
    public enum Phase {
        // propose --> accept --> confirm
        //         \                \--> unpropose --> cancel
        //           \--> reject
        propose, // a player suggests a move by dragging a piece
        unpropose, // never mind, I don't want to do this previously accepted play

        accept, // the game says okay you can do that; anything else?
        reject, // the game says no, you can't do that

        cancel, // unproposal was successful
        // TODO remove this phase? Is it implied when a new QwirkleTurn event arrives?
        confirm // your moves are confirmed; probably time for the next player's turn soon
    }

    private PlayPiece(QwirklePlacement placement, AsyncPlayer player) {
        this.player = player;
        this.placement = placement;
        this.phase = Phase.propose;
    }

    private PlayPiece(PlayPiece previous, Phase requiredPhase, Phase newPhase) {
        if (previous.getPhase() != requiredPhase)
            throw new IllegalStateException
                    ("Can only " + newPhase + " a " + requiredPhase + " -- not " + previous);

        this.placement = previous.getPlacement();
        this.player = previous.getPlayer();
        this.phase = newPhase;
        this.previous = previous;
    }

    /** Which player is playing? */
    public AsyncPlayer getPlayer() { return player; }

    /** Convenience. */
    public QwirklePiece getPiece() { return placement.getPiece(); }

    // TODO do we need to add a QwirkleGrid to this -- the grid it was played on?
    /** A player proposes playing a piece, probably by dragging it to the board. */
    public static PlayPiece propose(AsyncPlayer player, QwirklePlacement placement) {
        return new PlayPiece(placement, player);
    }

    /** Accept a single placement. */
    public PlayPiece accept() { return new PlayPiece(this, Phase.propose, Phase.accept); }

    /** Reject a single placement. */
    public PlayPiece reject() { return new PlayPiece(this, Phase.propose, Phase.reject); }

    /** Player changes their mind about a proposed placement.
     *  May trigger a cascade of cancellations. */
    public PlayPiece unpropose() { return new PlayPiece(this, Phase.accept, Phase.unpropose); }

    /** Confirm an already-accepted play. */
    public PlayPiece confirm() { return new PlayPiece(this, Phase.accept, Phase.confirm); }

    /** Cancel an already-accepted play, usually a response to an un-proposal,
     *  but may be part of an unpropose cascade. */
     public PlayPiece cancel() { return new PlayPiece(this, Phase.unpropose, Phase.cancel); }

    public QwirklePlacement getPlacement() { return placement; }

    /** What phase of a play is this? Proposal, acceptance, etc. */
    public Phase getPhase() { return phase; }

    /** What preceded this phase? For example, a cancel is preceded by an unproposal.
     *  For a proposal, returns null. */
    public PlayPiece getPrevious() { return previous; }

    /** This play is proposed by a player. */
    public boolean isPropose() { return phase == Phase.propose; }
    /** A player has changed their mind about a play. */
    public boolean isUnpropose() { return phase == Phase.unpropose; }
    /** This play has been accepted by the board as legal. */
    public boolean isAccept() { return phase == Phase.accept; }
    /** This play has been rejected by the board, probably because it was illegal. */
    public boolean isReject() { return phase == Phase.reject; }
    /** This play has been confirmed and will be committed to the board. */
    public boolean isConfirm() { return phase == Phase.confirm; }
    /** This play has been cancelled, probably after an unproposal. */
    public boolean isCancel() { return phase == Phase.cancel; }

    public String toString() {
        return phase + " " + placement;
    }
}
