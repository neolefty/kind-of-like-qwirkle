package qwirkle.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.event.*;
import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirkleLocation;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;

import java.util.*;

/** A play that a player is contemplating. */
public class HypotheticalPlay {
    // The board not including the hypothetical play
    private QwirkleBoard board;

    private EventBus bus;

    // The board including the hypothetical play
    private QwirkleBoard hypoBoard;

    private List<PlayPiece> plays = new ArrayList<>();

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

    /** Respond player interactions -- proposing a piece to play or unplay. */
    @Subscribe
    public synchronized void proposePlay(PlayPiece event) {
        // A. playing a new piece is proposed
        if (event.isPropose()) {
            // if it's legal, accept it
            if (isLegalMove(event.getPlacement())) {
                PlayPiece accept = event.accept();
                plays.add(accept);
                hypoBoard = getBoard().play(getPlacements());
                bus.post(accept);
            }
            // if it's not legal, reject it
            else
                bus.post(event.reject());
        }
        // B. a player has changed their mind and wants to retract a play
        else if (event.isUnpropose()) {
            // if they actually played it, then figure out how many pieces must be cancelled
            if (containsPlacement(event.getPlacement().getLocation())) {
                List<PlayPiece> cancellations = cascadeCancel(event);
                for (PlayPiece cancel : cancellations)
                    bus.post(cancel);
            }
        }
    }


    /** What plays need to be cancelled if <tt>unpropose</tt> is cancelled?
     *  Create cancel events & remove their proposed events from <tt>this.plays</tt>. */
    private synchronized List<PlayPiece> cascadeCancel(PlayPiece unpropose) {
        throw new IllegalStateException("NYI");
    }

    /** Where can this piece be placed legally? */
    public Collection<QwirklePlacement> getLegalMoves(QwirklePiece piece) {
//        if (getBoard() == null)
//            return Collections.singletonList(new QwirklePlacement(piece, 0, 0));
//        else
            return getBoard().getLegalPlacements(getPlacements(), piece);
    }

    /** The placements so far accepted from the player. */
    public synchronized List<QwirklePlacement> getPlacements() {
        List<QwirklePlacement> result = new ArrayList<>();
        for (PlayPiece play : plays)
            result.add(play.getPlacement());
        return Collections.unmodifiableList(result);
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

    /** Is this location one of the placements that has already been accepted? */
    public synchronized boolean containsPlacement(QwirkleLocation location) {
        for (PlayPiece play : plays)
            if (play.getPlacement().getLocation().equals(location))
                return true;
        return false;
    }

    /** Clear hypothetical plays because the board has updated. */
    private synchronized void clearHypothetical(QwirkleBoard board) {
        hypoBoard = null;
        plays.clear();
        this.board = board;
    }

    /** Does this have no hypothetical placements? */
    public synchronized boolean isEmpty() {
        return plays.isEmpty();
    }

    /** Confirm this hypothetical play as a real play. */
    public void confirm() {
        bus.post(new PlayTurn(getPlacements()));
    }
}
