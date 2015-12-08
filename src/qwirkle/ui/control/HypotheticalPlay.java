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
    private DiscardController discardController;

    private EventBus bus;

    // The board including the hypothetical play
    private QwirkleBoard hypoBoard;
    private QwirklePlayer currentPlayer;

    // The accepted plays, in the order they were placed
    // (note: may be plays on the gameboard or discards)
    private List<PlayPiece> acceptedPlays = new ArrayList<>();

    // Cached copy: the last set of legal moves reported
    private Collection<QwirklePlacement> lastLegal;

    public HypotheticalPlay(final EventBus bus) {
        this.bus = bus;
        bus.register(new EventSubscriber());
        this.discardController = new DiscardController(bus);
    }

    /** The board not including hypothetical plays. */
    public synchronized QwirkleBoard getBoard() { return board; }

    /** The board including hypothetical plays. */
    public synchronized QwirkleBoard getHypotheticalBoard() { return hypoBoard; }

    /** Discards are managed here, for the sake of modularity. */
    public DiscardController getDiscardController() { return discardController; }

    /** Is this one of the placements that has already been accepted? */
    public synchronized boolean containsPlacement(QwirklePlacement placement) {
        for (PlayPiece play : acceptedPlays)
            if (play.getPlacement().equals(placement))
                return true;
        return false;
    }

    /** Does this play consist only of discards? */
    public boolean isAllDiscards() {
        for (PlayPiece accepted : acceptedPlays)
            if (!accepted.isTypeDiscard())
                return false;
        return true; // no non-discards found
    }

    /** How many pieces are currently in this hypothetical play? */
    public int size() { return acceptedPlays.size(); }

    /** Does this have no hypothetical placements? */
    public synchronized boolean isEmpty() {
        return acceptedPlays.isEmpty();
    }

    /** Confirm this hypothetical play as a real play. */
    public void confirm() {
        if (isAllDiscards()) {
            List<QwirklePiece> discards = new ArrayList<>();
            for (QwirklePlacement place : getPlacements())
                discards.add(place.getPiece());
            bus.post(PlayTurn.discard(discards));
        }
        else
            bus.post(PlayTurn.play(getPlacements()));
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

    // TODO allow putting it back where you found it
    /** Where can this piece be placed legally? */
    public synchronized Collection<QwirklePlacement> getLegalMoves(QwirklePiece piece) {
        Collection<QwirklePlacement> result;
        // you can't discard & play at the same time
        if (size() > 0 && isAllDiscards())
            result = Collections.emptyList();
            // what are the legal moves?
        else
            result = getBoard().getLegalPlacements(getPlacements(), piece);

        // remember the result
        this.lastLegal = result;
//        System.out.println("Legal moves: " + result);
        return result;
    }

    /** The placements so far accepted from the player. */
    public synchronized List<QwirklePlacement> getPlacements() {
        List<QwirklePlacement> result = new ArrayList<>();
        for (PlayPiece play : acceptedPlays)
            result.add(play.getPlacement());
        return Collections.unmodifiableList(result);
    }

    /** Who is the current player? (Not synchronized because it wouldn't help.) */
    public QwirklePlayer getCurrentPlayer() {
        return currentPlayer;
    }

    /** Is <tt>placement</tt> legal, considering the other accepted moves so far? */
    // TODO only accept highlighted drops? Remember the last legal moves?
    public synchronized boolean isLegalMove(PlayPiece play) {
        // you can discard as long as you've only discarded so far
        if (play.isTypeDiscard()) {
            return isAllDiscards()
                    // and nothing is already in that discard spot (purely cosmetic)
                    && play.getDisplay().getGrid().get(play.getLocation()) == null;
        }
        // you can play if it fits the rules
        else if (play.isTypeGameboard()) {
            if (lastLegal == null) {
                List<QwirklePlacement> tmp = new ArrayList<>(getPlacements());
                tmp.add(play.getPlacement());
                return board.isLegal(tmp);
            } else {
                return lastLegal.contains(play.getPlacement());
            }
        }
        else if (play.isTypeHand()) {
            PlayPiece origin = findPlay(play.getPiece());
            //noinspection SimplifiableIfStatement
            if (origin == null) return true; // already removed or never actually played
            else return isRemovable(origin.getPlacement());
        }
        // um, don't know about other types of plays. Cancellations are handled elsewhere.
        else
            throw new UnsupportedOperationException("Unknown type: " + play);
    }

    private class EventSubscriber {
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
        public void turnStarting(TurnStarting event) {
            setCurrentPlayer(event.getCurPlayer());
        }

        @Subscribe
        public void proposePlay(PlayPiece event) {
            respondToProposal(event);
        }
    }

    private synchronized void setCurrentPlayer(QwirklePlayer currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /** Respond to player interactions -- proposing a piece to play
     *  or unproposing a piece that was contemplated. */
    private synchronized void respondToProposal(PlayPiece event) {
        // A. playing a new piece is proposed -- either on the gameboard or a discard
        if (event.isPhasePropose() && (event.isTypeGameboard() || event.isTypeDiscard())) {
            // if it's legal, accept it
            if (isLegalMove(event)) {
                PlayPiece accept = event.accept(HypotheticalPlay.this);
                acceptedPlays.add(accept);
                updateBoard();
                bus.post(accept);
            }
            // if it's not legal, reject it
            else
                bus.post(event.reject());
        }
        // B. a player has changed their mind and wants to retract a play
        else if (event.isPhaseUnpropose()) {
            // is this one of ours?
            if (containsPlacement(event.getPlacement())) {
                // can we remove it without breaking things?
                if (isRemovable(event.getPlacement())) {
                    boolean removed = acceptedPlays.remove(event.getPrevious());
                    updateBoard();
                    if (!removed) throw new IllegalStateException
                            ("Couldn't remove " + event.getPrevious() + " from accepted plays.");
                    bus.post(event.cancel());
                }
                // sorry, we would need to remove other pieces first and cascade to this one
                else
                    throw new IllegalStateException("Removing a locked piece is not supported: " + event);
            }
        }
    }

    private void updateBoard() {
        if (isAllDiscards())
            hypoBoard = getBoard();
        else
            hypoBoard = getBoard().play(getPlacements());
    }

    /** Find the placement of this piece in the hypothetical play. Null if not present. */
    private PlayPiece findPlay(QwirklePiece piece) {
        for (PlayPiece play : acceptedPlays)
            if (piece == play.getPiece())
                return play;
        return null;
    }

    /** Clear hypothetical plays because the board has updated. */
    private synchronized void clearHypothetical(QwirkleBoard board) {
        hypoBoard = null;
        currentPlayer = null;
        acceptedPlays.clear();
        lastLegal = null;
        this.board = board;
    }
}
