package qwirkle.event;

import qwirkle.game.QwirklePlacement;

// TODO when all the plays have been chosen for a turn, bundle them up into a single event
/** Someone plays a piece interactively. */
public class PiecePlay {
    private QwirklePlacement placement;
    private Phase phase;
    private PiecePlay proposal;

    /** A player {@link Phase#propose}s a play by dragging a piece.
     *  The game {@link Phase#accept}s or {@link Phase#reject}s it.
     *  Later, it may be either {@link Phase#cancel}ed or {@link Phase#confirm}ed. */
    public enum Phase {
        propose, // a player suggests a move by dragging a piece
        unpropose, // never mind, I don't want to do this

        accept, // the game says okay you can do that; anything else?
        reject, // the game says no, you can't do that

        cancel, // unproposal was successful
        confirm // your moves are confirmed; probably time for the next player's turn
    }

    private PiecePlay(QwirklePlacement placement) {
        this.placement = placement;
        this.phase = Phase.propose;
    }

    private PiecePlay(PiecePlay previous, Phase requiredPhase, Phase newPhase) {
        if (previous.getPhase() != requiredPhase)
            throw new IllegalStateException
                    ("Can only " + newPhase + " a " + requiredPhase + " -- not " + previous);

        this.placement = previous.getPlacement();
        this.phase = newPhase;

        if (previous.getPhase() == Phase.propose)
            this.proposal = previous;
        else
            this.proposal = previous.getProposal();
    }

    // TODO do we need to add a QwirkleGrid to this -- the grid it was played on?
    /** A player proposes playing a piece, probably by dragging it to the board. */
    public static PiecePlay propose(QwirklePlacement placement) {
        return new PiecePlay(placement);
    }

    /** Accept a single placement. */
    public PiecePlay accept() { return new PiecePlay(this, Phase.propose, Phase.accept); }

    /** Reject a single placement. */
    public PiecePlay reject() { return new PiecePlay(this, Phase.propose, Phase.reject); }

    /** Player changes their mind about a proposed placement.
     *  May trigger a cascade of cancellations. */
    public PiecePlay unpropose() { return new PiecePlay(this, Phase.propose, Phase.unpropose); }

    /** Confirm an already-accepted play. */
    public PiecePlay confirm() { return new PiecePlay(getProposal(), Phase.accept, Phase.confirm); }

    /** Cancel an already-accepted play, usually a response to an un-proposal,
     *  but may be part of an unpropose cascade. */
    public PiecePlay cancel() { return new PiecePlay(getProposal(), Phase.accept, Phase.cancel); }

    public QwirklePlacement getPlacement() { return placement; }

    /** What phase of a play is this? Proposal, acceptance, etc. */
    public Phase getPhase() { return phase; }

    public PiecePlay getProposal() {
        return proposal;
    }

    /** This play is proposed by a player. */
    public boolean isPropose() { return phase == Phase.propose; }
    /** A player has changed their mind about a play. */
    public boolean isUnpropose() { return phase == Phase.unpropose; }
    /** This play has been accepted by the board as legal. */
    public boolean isAccept() { return phase == Phase.accept; }
    /** This play has been rejected by the board, probably because it was illegal. */
    public boolean isReject() { return phase == Phase.reject; }
    /** This play has been confirmed and committed to the board. */
    public boolean isConfirm() { return phase == Phase.confirm; }
    /** This play has been cancelled, probably after an unproposal. */
    public boolean isCancel() { return phase == Phase.cancel; }

    public String toString() {
        return phase + " " + placement;
    }
}
