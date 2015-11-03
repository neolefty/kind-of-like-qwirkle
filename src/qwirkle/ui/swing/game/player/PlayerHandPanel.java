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
import java.util.List;

// TODO allow dragging to reorder pieces
// TODO while dragging, indicate the piece being dragged or else hide it in the player's hand
// TODO after dropped, remove the piece from the player's hand
// TODO on their turn, highlight the pieces in the player's hand that can be played (don't highlight the ones that can't)
// TODO once a piece has been played, highlight the remaining pieces that can be played (see QwirklePlayableGridPanel)
// TODO allow cancelling by dragging off of the board

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
    // Note: track placements to avoid ambiguity due to duplicate pieces.
    private List<QwirklePlacement> tentativePlay = new ArrayList<>();
    private List<QwirklePlacement> draggedOut = new ArrayList<>();

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
//        // hide & show pieces in hand as we drag them to the board
//        @Subscribe public void drag(DragPiece event) {
//            if (event.getPlayer() == player && event.getAction() != DragPiece.Action.SUSTAIN) {
//                System.out.println(event);
//                if (event.getAction() == DragPiece.Action.PICKUP) {
//                    draggedOut.add(event.getPlacement());
////                    update();
//                }
//                else if (event.getAction() == DragPiece.Action.CANCEL
//                        || event.getAction() == DragPiece.Action.DROP)
//                {
//                    draggedOut.remove(event.getPlacement());
////                    update();
//                }
//            }
//        }

//        // hide & show pieces in hand as we drag them to the board
//        @Subscribe public void play(PlayPiece event) {
//            if (event.getPlayer() == player) {
//                if (event.isPropose())
//                    tentativePlay.add(event.getPlacement());
//                else if (event.isCancel() || event.isReject() || event.isUnpropose())
//                    tentativePlay.remove(event.getPlacement());
//            }
//        }

        // someone took a turn
        @Subscribe public void turned(TurnCompleted turn) {
            try {
                update(turn);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        // a game started
        @Subscribe public void gameStart(GameStarted started) {
            try {
                clear();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        // maybe hand changed
        @Subscribe public void dealt(DrawPieces draw) {
            try {
                update(draw);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clear() {
        lastDraw = null;
        update();
    }

    private void update(DrawPieces draw) {
        if (draw.getPlayer() == player)
            lastDraw = draw.getDrawn();
        // only highlight the most recent player's draw at a time
        else
            lastDraw = null;
        update();
    }

    /** A turn came in from external bus. */
    private void update(TurnCompleted turn) {
        if (turn.getPlayer() == player) {
            // we just took a turn, so forget the previous pieces we drew -- we'll be getting new ones soon
            lastDraw = null;
            // likewise forget about the play we were contemplating
            tentativePlay.clear();
            draggedOut.clear();
            // force a UI update
            update();
        }
    }

    /** Update the UI. */
    private void update() {
        TurnCompleted fakeTurn = createFakeTurn();
        getEventBus().post(fakeTurn);
    }

    /** Create a fake {@link TurnCompleted} to tell the {@link QwirkleGridPanel} what to draw. */
    private TurnCompleted createFakeTurn() {
        List<QwirklePlacement> handPlaces = new ArrayList<>();
        List<QwirklePiece> drawScratch = new ArrayList<>();
        if (lastDraw != null) drawScratch.addAll(lastDraw);
        List<QwirklePlacement> drawPlacements = new ArrayList<>();

        // make a board from our hand, along with the new pieces
        List<QwirklePiece> hand = control.getGame().getHand(player);
        if (hand != null) {
            // build a board that represents the hand
            if (!draggedOut.isEmpty())
                System.out.println("hiding " + draggedOut);
            for (int i = 0; i < hand.size(); ++i) {
                QwirkleLocation location = new QwirkleLocation(vertical ? 0 : i, vertical ? i : 0);
                QwirklePiece piece = hand.get(i);
                QwirklePlacement place = new QwirklePlacement(piece, location);
//                if (!draggedOut.contains(place))
                    handPlaces.add(place);
            }
            // highlight the new pieces, starting at the bottom
            for (int i = handPlaces.size() - 1; i >= 0; --i) {
                QwirklePlacement place = handPlaces.get(i);
                // make sure each draw is highlighted only once, but allow duplicate
                if (drawScratch.remove(place.getPiece()))
                    drawPlacements.add(place);
            }
        }

        QwirkleGrid handGrid = new QwirkleGridImpl(handPlaces);
        return TurnCompleted.drawToHand(handGrid, drawPlacements, player);
    }
}
