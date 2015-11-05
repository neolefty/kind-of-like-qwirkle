package qwirkle.ui.swing.game.player;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import qwirkle.control.GameController;
import qwirkle.event.*;
import qwirkle.game.*;
import qwirkle.game.impl.QwirkleGridImpl;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// TODO allow dragging to reorder pieces
// TODO allow cancelling by dragging off of the board
// TODO highlight playable pieces, on your turn, with colored bg?
// TODO highlight destinations on mouseover?

/** Show a player's status. Uses a {@link QwirkleGridPanel} with
 *  hacked event updates to show the player's current hand, highlighting
 *  the most recently drawn pieces.
 *
 *  <p>Note that there are two event buses involved: A local one for this panel, with internal events,
 *  and a global one that receives events from the game at large.</p>*/
public class PlayerHandPanel extends QwirkleGridPanel {
    private GameController control;
    private AsyncPlayer player;
    // what pieces did the player draw last?
    private List<QwirklePiece> lastDraw;
    // what pieces has the player placed on the board but not finalized as a play?

    // The pieces that have been dragged out of our hand and placed on the board
    // Note: track placements to avoid ambiguity due to duplicate pieces.
    private List<QwirklePlacement> draggedOut = new ArrayList<>();

    // Where each of our pieces belongs -- constructed when we refresh this panel
    // (even if they are currently not in our hand because they are on the board)
    // Note: Follow concurrency discipline when modifying it
    private final List<QwirklePlacement> handPlaces = new ArrayList<>();

    private boolean vertical;

    public PlayerHandPanel(final GameController control, AsyncPlayer player) {
        super(new EventBus(new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                System.out.println("Player hand panel event bus: " + context);
                exception.printStackTrace(System.out);
            }
        }));
        this.control = control;
        setBlankIncluded(false);

        this.player = player;
        control.register(new GameListener());
        getEventBus().register(new Object() {
            // forward mouse events from the local bus to the parent bus
            @Subscribe public void dragPosted(DragPiece event) { control.post(event); }
            @Subscribe public void passedOver(PassOver event) { control.post(event); }
        });
        setVertical(true);
    }

    /** Vertical or horizontal? */
    public void setVertical(boolean vertical) {
        if (this.vertical != vertical) {
            this.vertical = vertical;
            update();
        }
    }

    private class GameListener {
        @Subscribe public void drag(DragPiece event) {
            if (event.getPlayer() == player) {
                if (event.isPickup() && event.getGrid() != getGrid())
                    dragOut(event.getPlacement());
                else if (event.isCancel() || event.isDrop())
                    undragOut(event.getPlacement());
            }
        }

        // hide & show pieces in hand as we drag them to the board
        @Subscribe public void play(PlayPiece event) {
            if (event.getPlayer() == player) {
                if (event.isAccept()) {
//                    System.out.println(event);
                    dragOut(event.getPlacement());
                }
                else if (event.isCancel() || event.isReject()) {
//                    System.out.println(event);
                    undragOut(event.getPlacement());
                }
            }
        }

        // if our turn finalized, update our interface
        @Subscribe public void turned(TurnCompleted turn) {
            if (turn.getPlayer() == player) {
                // forget about the play we were contemplating
                clearTentative();
                // force a UI update
                update();
            }
        }

        // a game started
        @Subscribe public void gameStart(GameStarted started) { clear(); }

        // highlight the new pieces we draw
        @Subscribe public void dealt(DrawPieces draw) {
            if (draw.getPlayer() == player) {
                lastDraw = draw.getDrawn();
                update();
            }
        }

        // when our turn starts, stop highlighting the last pieces we drew
        @Subscribe public void turnStart(TurnStarting event) {
            if (event.getCurPlayer() == player) {
                lastDraw = null;
                update();
            }
        }
    }

    /** A piece was dragged out of our hand. */
    private void dragOut(QwirklePlacement place) {
        QwirklePlacement handPlacement = getHandPlacement(place);
        draggedOut.add(handPlacement);
        update();
    }

    /** Guess which piece in our hand this refers to. Note that our hand may have identical pieces,
     *  and in order to give physicality to them, we distinguish between them. */
    private QwirklePlacement getHandPlacement(QwirklePlacement placement) {
        synchronized (handPlaces) {
            // if we haven't drawn our hand yet, we don't really know so just go with it
            if (handPlaces.isEmpty()) {
                Collection<QwirklePiece> hand = control.getGame().getHand(player);
                if (hand.contains(placement.getPiece()))
                    return placement;
                else throw new IllegalStateException
                        (placement.getPiece() + " is not in " + player + "'s hand: " + hand + ".");
            } else {
                QwirklePlacement result = null;
                // simple case: it's one of the placements in our hand
                if (handPlaces.contains(placement))
                    result = placement;
                    // less simple case: it's a piece from our hand but the location is weird
                    // for example: we put it on the board but changed our mind and got it back
                    // (note: we rely on identity of QwirklePiece objects -- kind of a hack)
                else for (QwirklePlacement p : handPlaces)
                    if (p.getPiece() == placement.getPiece())
                        result = p;
                // last ditch: we can't figure it out, so best guess base on the piece
                if (result == null) for (QwirklePlacement p : handPlaces)
                    if (p.getPiece().equals(placement.getPiece()))
                        result = p;

                // maybe this is a concurrency problem -- the hand was switched and the query is out of date
                if (result == null) throw new IllegalStateException
                        ("Can't find " + placement.getPiece() + " in hand (" + handPlaces + ")");

                return result;
            }
        }
    }

    /** Never mind about a piece that was previously dragged out of our hand. */
    private void undragOut(QwirklePlacement place) {
        QwirklePlacement handPlacement = getHandPlacement(place);
        draggedOut.remove(handPlacement);
        update();
    }

    private void clear() {
        lastDraw = null;
        update();
    }

    /** Forget about the play we were contemplating. */
    private void clearTentative() {
        draggedOut.clear();
    }

    /** Update the UI. */
    private void update() {
        TurnCompleted fakeTurn = createFakeTurn();
        // note: post to the internal EventBus -- not the master event bus
        getEventBus().post(fakeTurn);
    }

    /** Create a fake {@link TurnCompleted} to tell the {@link QwirkleGridPanel} what to draw. */
    private TurnCompleted createFakeTurn() {
        synchronized(handPlaces) {
            handPlaces.clear();
            List<QwirklePlacement> handDisplay = new ArrayList<>();
            List<QwirklePiece> drawScratch = new ArrayList<>();
            if (lastDraw != null) drawScratch.addAll(lastDraw);

            // make a board from our hand, along with the new pieces
            List<QwirklePiece> hand = control.getGame().getHand(player);
            List<QwirklePlacement> drawPlacements = new ArrayList<>();
            if (hand != null) {
                // build a board that represents the hand
                for (int i = 0; i < hand.size(); ++i) {
                    QwirkleLocation location = new QwirkleLocation(vertical ? 0 : i, vertical ? i : 0);
                    QwirklePiece piece = hand.get(i);
                    QwirklePlacement place = new QwirklePlacement(piece, location);
                    handPlaces.add(place);
                    if (!draggedOut.contains(place))
                        handDisplay.add(place);
                }
                // highlight the new pieces, starting at the bottom
                for (int i = handDisplay.size() - 1; i >= 0; --i) {
                    QwirklePlacement place = handDisplay.get(i);
                    // make sure each draw is highlighted only once, but allow duplicate
                    if (drawScratch.remove(place.getPiece()))
                        drawPlacements.add(place);
                }
            }
            QwirkleGrid handGrid = new QwirkleGridImpl(handDisplay);
            return TurnCompleted.drawToHand(handGrid, drawPlacements, player);
        }
    }
}