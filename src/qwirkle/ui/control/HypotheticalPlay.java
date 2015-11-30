package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.*;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.PreEvent;
import qwirkle.game.event.TurnCompleted;
import qwirkle.game.event.TurnStarting;
import qwirkle.ui.event.PlayPiece;
import qwirkle.ui.event.PlayTurn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** A play that a player is contemplating. */
public class HypotheticalPlay {
    // The board not including the hypothetical play
    private QwirkleBoard board;

    private EventBus bus;

    // The board including the hypothetical play
    private QwirkleBoard hypoBoard;
    private QwirklePlayer currentPlayer;

    // The accepted plays, in the order they were placed
    private List<PlayPiece> acceptedPlays = new ArrayList<>();

    // What was the last play that was canceled? Null if none.
    private PlayPiece lastCancel;

    public HypotheticalPlay(final EventBus bus) {
        this.bus = bus;
        bus.register(this);
    }

    @Subscribe
    public void startGame(GameStarted started) {
        clearHypothetical(started.getStatus().getBoard());
    }

    /** When a turn is about to post, clear our hypothetical play.
     *  Note: this uses a pre-event to avoid concurrency issues with highlighting. */
    @Subscribe
    public void preTurnTaken(PreEvent pre) {
        if (pre.getEvent() instanceof TurnCompleted)
            clearHypothetical(((TurnCompleted) pre.getEvent()).getStatus().getBoard());
    }

    /** When a new turn starts, update our notion of the current player. */
    @Subscribe
    public void turnStarting(TurnStarting event) { currentPlayer = event.getCurPlayer(); }

    /** Respond player interactions -- proposing a piece to play or unplay. */
    @Subscribe
    public synchronized void proposePlay(PlayPiece event) {
        // A. playing a new piece is proposed
        if (event.isPropose()) {
            // if it's legal, accept it
            if (isLegalMove(event.getPlacement())) {
                PlayPiece accept = event.accept(this);
                acceptedPlays.add(accept);
                hypoBoard = getBoard().play(getPlacements());
                bus.post(accept);
            }
            // if it's not legal, reject it
            else
                bus.post(event.reject());
        }
        // B. a player has changed their mind and wants to retract a play
        else if (event.isUnpropose()) {
            // is this one of ours?
            if (containsPlacement(event.getPlacement())) {
                // can we remove it without breaking things?
                if (isRemovable(event.getPlacement())) {
                    boolean removed = acceptedPlays.remove(event.getPrevious());
                    hypoBoard = getBoard().play(getPlacements());
                    if (!removed) throw new IllegalStateException
                            ("Couldn't remove " + event.getPrevious() + " from accepted plays.");
                    lastCancel = event.cancel();
                    bus.post(lastCancel);
                }
                // sorry, we would need to remove other pieces first and cascade to this one
                else
                    throw new IllegalStateException("Removing a locked piece is not supported: " + event);
            }
        }
    }

    /** How many pieces are currently in this hypothetical play? */
    public int size() { return acceptedPlays.size(); }

    /** Where can this piece be placed legally? */
    public Collection<QwirklePlacement> getLegalMoves(QwirklePiece piece) {
//        if (getBoard() == null)
//            return Collections.singletonList(new QwirklePlacement(piece, 0, 0));
//        else
        // special case: moving the only piece on the board
        if (lastCancel != null && getBoard().size() == 0 && piece == lastCancel.getPiece()) {
            List<QwirklePlacement> result = new ArrayList<>();
            QwirkleLocation center = lastCancel.getPlacement().getLocation();
            result.add(new QwirklePlacement(piece, center.getAbove()));
            result.add(new QwirklePlacement(piece, center.getBelow()));
            result.add(new QwirklePlacement(piece, center.getLeft()));
            result.add(new QwirklePlacement(piece, center.getRight()));
            return result;
        }
        else
            return getBoard().getLegalPlacements(getPlacements(), piece);
    }

    /** The placements so far accepted from the player. */
    public synchronized List<QwirklePlacement> getPlacements() {
        List<QwirklePlacement> result = new ArrayList<>();
        for (PlayPiece play : acceptedPlays)
            result.add(play.getPlacement());
        return Collections.unmodifiableList(result);
    }

    /** Not synchronized; neither is {@link #turnStarting}, where player is set. */
    public QwirklePlayer getCurrentPlayer() {
        return currentPlayer;
    }

    /** Is <tt>placement</tt> legal, considering the other accepted moves so far? */
    public synchronized boolean isLegalMove(QwirklePlacement placement) {
        List<QwirklePlacement> placements = new ArrayList<>();
        placements.add(placement);
        placements.addAll(getPlacements());
        return board.isLegal(placements);
    }

    /** The board not including hypothetical plays. */
    public synchronized QwirkleBoard getBoard() { return board; }

    /** The board including hypothetical plays. */
    public synchronized QwirkleBoard getHypotheticalBoard() { return hypoBoard; }

    /** Is this one of the placements that has already been accepted? */
    public synchronized boolean containsPlacement(QwirklePlacement placement) {
        for (PlayPiece play : acceptedPlays)
            if (play.getPlacement().equals(placement))
                return true;
        return false;
    }

    /** Clear hypothetical plays because the board has updated. */
    private synchronized void clearHypothetical(QwirkleBoard board) {
        hypoBoard = null;
        currentPlayer = null;
        acceptedPlays.clear();
        this.board = board;
    }

    /** Does this have no hypothetical placements? */
    public synchronized boolean isEmpty() {
        return acceptedPlays.isEmpty();
    }

    /** Confirm this hypothetical play as a real play. */
    public void confirm() {
        bus.post(new PlayTurn(getPlacements()));
    }

    /** Is this placement removable? */
    public synchronized boolean isRemovable(QwirklePlacement placement) {
        // if it's not one of ours, duh, no
        if (!containsPlacement(placement))
            return false;
        // if it's the only move, then it's fine to remove it
        else if (size() == 1)
            return true;
        // if the move is still legal without it
        else {
            List<QwirklePlacement> without = new ArrayList<>(getPlacements());
            without.remove(placement);
            return getBoard().isLegal(without);
        }
    }

    /** Get the event that records a particular placement being accepted.
     *  Useful if you want to unpropose that placement. */
    public PlayPiece getAcceptedPlay(QwirklePlacement placement) {
        for (PlayPiece accept : acceptedPlays)
            if (accept.getPlacement().equals(placement))
                return accept;
        throw new IllegalStateException("No acceptance found for " + placement);
    }
}
