package qwirkle.ui.event;

import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.ui.QwirkleGridDisplay;
import qwirkle.ui.control.HypotheticalPlayController;

/** Someone plays a piece interactively. */
public class PlayPiece {
    private QwirklePlacement placement;
    private QwirklePlayer player;
    private Phase phase;
    private PlayPiece previous;
    private QwirkleGridDisplay display;

    // The play being contemplated
    private HypotheticalPlayController play;

    /** A player {@link Phase#propose}s a play by dragging a piece.
     *  The game {@link Phase#accept}s or {@link Phase#reject}s it.
     *  Later, it may be either {@link Phase#cancel}ed or confirmed
     *  by posting a {@link PlayTurn} event. */
    public enum Phase {
        // propose --> accept --> [TurnCompleted event]
        //         \                \--> unpropose --> cancel
        //           \--> reject
        propose, // a player suggests a move by dragging a piece
        unpropose, // never mind, I don't want to do this previously accepted play

        accept, // the game says okay you can do that; anything else?
        reject, // the game says no, you can't do that

        cancel, // unproposal was successful
    }

    private PlayPiece(QwirklePlacement placement, QwirklePlayer player, QwirkleGridDisplay display) {
        this.player = player;
        this.placement = placement;
        this.phase = Phase.propose;
        this.display = display;
    }

    private PlayPiece(PlayPiece previous, Phase requiredPhase, Phase newPhase) {
        if (previous.getPhase() != requiredPhase)
            throw new IllegalStateException
                    ("Can only " + newPhase + " a " + requiredPhase + " -- not " + previous);

        this.placement = previous.getPlacement();
        this.player = previous.getPlayer();
        this.display = previous.getDisplay();
        this.phase = newPhase;
        this.previous = previous;
        this.play = previous.play;
    }

    /** Which player is playing? */
    public QwirklePlayer getPlayer() { return player; }

    /** Convenience. */
    public QwirklePiece getPiece() { return placement.getPiece(); }

    /** A player proposes playing a piece, probably by dragging it to the board.
     *  @param display the place it was dragged to */
    public static PlayPiece propose(QwirklePlayer player, QwirklePlacement placement, QwirkleGridDisplay display) {
        return new PlayPiece(placement, player, display);
    }

    /** Accept a single placement. */
    public PlayPiece accept(HypotheticalPlayController play) {
        PlayPiece result = new PlayPiece(this, Phase.propose, Phase.accept);
        result.play = play;
        return result;
    }

    /** Reject a single placement. */
    public PlayPiece reject() { return new PlayPiece(this, Phase.propose, Phase.reject); }

    /** Player changes their mind about a proposed placement.
     *  May trigger a cascade of cancellations. */
    public PlayPiece unpropose() { return new PlayPiece(this, Phase.accept, Phase.unpropose); }

    /** Cancel an already-accepted play, usually a response to an un-proposal,
     *  but may be part of an unpropose cascade. */
     public PlayPiece cancel() { return new PlayPiece(this, Phase.unpropose, Phase.cancel); }

    /** The whole play being contemplated. Null if it's the first play and hasn't been accepted yet. */
    public HypotheticalPlayController getPlay() { return play; }

    /** The piece & location being played to. */
    public QwirklePlacement getPlacement() { return placement; }

    /** The location being played to. */
    public QwirkleLocation getLocation() { return getPlacement().getLocation(); }

    /** The display this is being played on. */
    public QwirkleGridDisplay getDisplay() { return display; }

    /** What phase of a play is this? Proposal, acceptance, etc. */
    public Phase getPhase() { return phase; }

    /** What preceded this phase? For example, a cancel is preceded by an unproposal.
     *  For a proposal, returns null. */
    public PlayPiece getPrevious() { return previous; }

    /** Is this a play onto a discard pile? */
    public boolean isTypeDiscard() {
        return display.getDisplayType() == QwirkleGridDisplay.DisplayType.discard;
    }

    /** Is this a play onto a gameboard? */
    public boolean isTypeGameboard() {
        return display.getDisplayType() == QwirkleGridDisplay.DisplayType.gameboard;
    }

    /** Is this a play into a player's hand (taking it back from the board)? */
    public boolean isTypeHand() {
        return display.getDisplayType() == QwirkleGridDisplay.DisplayType.hand;
    }

    /** This play is proposed by a player. */
    public boolean isPhasePropose() { return phase == Phase.propose; }
    /** A player has changed their mind about a play. */
    public boolean isPhaseUnpropose() { return phase == Phase.unpropose; }
    /** This play has been accepted by the board as legal. */
    public boolean isPhaseAccept() { return phase == Phase.accept; }
    /** This play has been rejected by the board, probably because it was illegal. */
    public boolean isPhaseReject() { return phase == Phase.reject; }
    /** This play has been cancelled, probably after an unproposal. */
    public boolean isPhaseCancel() { return phase == Phase.cancel; }

    public String toString() {
        return phase + " " + placement + " to " + display.getDisplayType();
    }
}
