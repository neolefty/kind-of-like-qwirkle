package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleBoard;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.PreEvent;
import qwirkle.game.event.TurnCompleted;
import qwirkle.game.event.TurnStarting;
import qwirkle.ui.QwirkleGridDisplay;
import qwirkle.ui.event.PlayPiece;
import qwirkle.ui.event.PlayTurn;

import java.util.*;

// TODO test this
/** A play that a player is contemplating. */
public class HypotheticalPlayController {
    // The board not including the hypothetical play
    private QwirkleBoard board;
    private DiscardTracker discardTracker;
    private QwirklePlayer curPlayer;

    private EventBus bus;

    // The board including the hypothetical play
    private QwirkleBoard hypoBoard;

    // The accepted plays (may be plays on the gameboard or discards)
    // note: use identity rather than hashcode because we may have multiple identical pieces played (discarded)
    private Map<QwirklePiece, PlayPiece> acceptedPlays = new IdentityHashMap<>();

    public HypotheticalPlayController(final EventBus bus) {
        this.bus = bus;
        bus.register(new EventSubscriber());
        this.discardTracker = new DiscardTracker(bus);
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

    /** The board not including hypothetical plays. */
    public synchronized QwirkleBoard getBoard() { return board; }

    /** The board including hypothetical plays. */
    public synchronized QwirkleBoard getHypotheticalBoard() { return hypoBoard; }

    /** Discards are managed here, for the sake of modularity. */
    public DiscardTracker getDiscardTracker() { return discardTracker; }

    /** Is this one of the placements on the main board that has already been accepted? */
    public synchronized boolean containsPlacement(QwirklePlacement placement) {
        PlayPiece play = acceptedPlays.get(placement.getPiece());
        return play != null && play.getDrop().getPlacement().equals(placement);
    }

    /** How many pieces are currently in this hypothetical play? */
    public int size() { return acceptedPlays.size(); }

    /** Does this have no hypothetical placements? */
    public synchronized boolean isEmpty() {
        return acceptedPlays.isEmpty();
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

    /** Where can this piece land legally on the gameboard? */
    public synchronized Collection<QwirklePlacement> getLegalMoves(QwirklePiece piece) {
        Collection<QwirklePlacement> result;
        Collection<PlayPiece> allExcept = getAllExcept(piece);
        // if we've discarded, then we can't play on the board
        if (!allExcept.isEmpty() && allExcept.iterator().next().isDropDiscard())
            result = Collections.emptyList();
        // a play is allowed
        else
            result = getBoard().getLegalPlacements(getPlacements(piece), piece);

//        System.out.println("Legal moves: " + result);
        return result;
    }

    /** All the plays that have been accepted except for the one involving <tt>play</tt>'s piece. */
    private Collection<PlayPiece> getAllExcept(QwirklePiece piece) {
        Map<QwirklePiece, PlayPiece> result = new IdentityHashMap<>(acceptedPlays);
        result.remove(piece);
        return result.values();
    }

    /** The placements so far accepted from the player. */
    public synchronized List<QwirklePlacement> getPlacements() { return getPlacements(null); }

    /** The placements so far accepted from the player, except for any involving <tt>exceptFor</tt>. */
    public synchronized List<QwirklePlacement> getPlacements(QwirklePiece exceptFor) {
        List<QwirklePlacement> result = new ArrayList<>();
        for (PlayPiece play : acceptedPlays.values())
            if (play.getPiece() != exceptFor)
                result.add(play.getDrop().getPlacement());
        return result;
    }

    /** Does this play consist only of 0 or more discards? */
    public boolean isAllDiscards() {
        return acceptedPlays.isEmpty()
                || acceptedPlays.values().iterator().next().isDropDiscard();
    }

    /** Is <tt>placement</tt> legal, considering the other accepted moves so far? */
    private synchronized boolean isLegalMove(PlayPiece play) {
        // note: we don't initialize these on purpose, to force a compiler check
        // that we've covered all possibilities (that's why we have all the UOE's)
        boolean pickupLegal, dropLegal;

        // 1. Pickup -- is it legal?
        // coming from the gameboard: can pick it up if it's not locked by other pieces
        if (play.isPickupGameboard())
            pickupLegal = isRemovable(play.getPickup().getPlacement());
            // can pick up pretty much anything from discard or hand
        else if (play.isPickupDiscard() || play.isPickupHand())
            pickupLegal = true;
        else
            throw new UnsupportedOperationException("Unknown origin type: " + play);

        // 2. Drop -- is it legal?
        // on gameboard: yes if it's a legal play & discard is otherwise empty
        if (play.isDropGameboard()) {

            // a. figure out whether the placement would be legal
            List<QwirklePlacement> placements = getPlacements();
            // if it's a relocation on the gameboard, remove the old spot and add the new spot
            if (play.isPickupGameboard()) {
                boolean removed = placements.remove(play.getPickup().getPlacement());
                if (!removed)
                    throw new IllegalStateException("Picking up nonexistent piece: " + play.getPickup());
                placements.add(play.getDrop().getPlacement());
            }
            if (!getBoard().isLegal(placements))
                dropLegal = false;

            // b. if the placement would be legal, see if the pickup is okay
            else {
                // from hand: can play if you haven't already discarded
                if (play.isPickupHand())
                    dropLegal = (discardTracker.size() == 0);
                    // from discard: can play if it's the only discarded piece
                else if (play.isPickupDiscard())
                    dropLegal = discardTracker.size() == 1;
                    // if it was picked up from the gameboard, then it's fine
                else if (play.isPickupGameboard())
                    dropLegal = true;
                else
                    throw new UnsupportedOperationException("Unknown destination type: " + play);
            }
        }

        // drop in hand: legal if the piece can be removed from the board
        else if (play.isDropHand()) {
            if (play.isPickupDiscard() || play.isPickupHand())
                dropLegal = true;
            else if (play.isPickupGameboard())
                dropLegal = isRemovable(play.getPickup().getPlacement());
            else throw new UnsupportedOperationException("Unknown origin type: " + play);
        }

        // drop in discard: only if it wouldn't split the play between the gameboard and discard pile
        else if (play.isDropDiscard()) {
            if (play.isPickupHand())
                dropLegal = isAllDiscards();
            else if (play.isPickupDiscard())
                dropLegal = true;
            // TODO treat drop on discard as drop on hand when can't discard
            else if (play.isPickupGameboard())
                dropLegal = acceptedPlays.size() == 1; // it's removable by definition
            else throw new UnsupportedOperationException("Unknown origin type: " + play);
        }

        else throw new UnsupportedOperationException("Unknown destination type: " + play);

        return pickupLegal && dropLegal;
    }

    public QwirklePlayer getCurrentPlayer() { return curPlayer; }

    /** Handle events from the EventBus. */
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

        @Subscribe public void proposePlay(PlayPiece event) { handlePlay(event); }

        @Subscribe public void newTurn(TurnStarting event) { curPlayer = event.getCurPlayer(); }
    }

    /** Respond to player interactions -- proposing a piece to play
     *  or unproposing a piece that was contemplated. */
    private synchronized void handlePlay(PlayPiece event) {
        if (event.isPhasePropose()) {
            if (isLegalMove(event))
                accept(event);
            else
                reject(event);
        }
    }

    private void reject(PlayPiece event) {
        bus.post(event.reject());
    }

    private synchronized void accept(PlayPiece event) {
        event = event.accept();
        // clear previous state about this piece
        acceptedPlays.remove(event.getPiece());
        // note the new state (putting it back into your hand isn't really a play)
        if (event.getDrop().getDisplayType() != QwirkleGridDisplay.DisplayType.hand)
            // note: will overwrite a previous play of the same piece
            acceptedPlays.put(event.getPiece(), event);
        updateBoard();
        bus.post(event);
    }

    /** Clear hypothetical plays because the board has updated. */
    private synchronized void clearHypothetical(QwirkleBoard board) {
        hypoBoard = null;
        acceptedPlays.clear();
        this.board = board;
    }

    /** Update the board based on the current hypothetical plays, but don't post any events. */
    private void updateBoard() {
        if (isAllDiscards())
            hypoBoard = getBoard();
        else
            hypoBoard = getBoard().play(getPlacements());
    }
}
