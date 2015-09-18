package qwirkle.ui.swing.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import qwirkle.control.GameManager;
import qwirkle.event.*;
import qwirkle.game.*;
import qwirkle.game.impl.QwirkleGridImpl;

import java.util.ArrayList;
import java.util.List;

// TODO allow dragging to reorder pieces

/** Show a player's status. Uses a {@link QwirkleGridPanel} with
 *  hacked event updates to show the player's current hand, highlighting
 *  the most recently drawn pieces. */
public class PlayerHandPanel extends QwirkleGridPanel {
    private GameManager mgr;
    private AsyncPlayer player;
    // what pieces did the player draw last?
    private List<QwirklePiece> lastDraw;

    private boolean vertical;

    public PlayerHandPanel(final GameManager mgr, AsyncPlayer player) {
        super(new EventBus(new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                System.out.println("Player hand panel event bus: " + context);
                exception.printStackTrace(System.out);
            }
        }));
        setBlankIncluded(false);

        this.mgr = mgr;
        this.player = player;
        mgr.getEventBus().register(new GameListener());
        // forward mouse events from the local bus to the parent bus
        getEventBus().register(new Object() {
            @Subscribe public void dragPosted(PieceDrag event) { mgr.getEventBus().post(event); }
            @Subscribe public void passedOver(PassOver event) { mgr.getEventBus().post(event); }
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
        // someone took a turn
        @Subscribe public void turned(QwirkleTurn turn) {
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
        @Subscribe public void dealt(QwirkleDraw draw) {
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

    private void update(QwirkleDraw draw) {
        if (draw.getPlayer() == player)
            lastDraw = draw.getDrawn();
        // only highlight the most recent player's draw at a time
        else
            lastDraw = null;
        update();
    }

    private void update(QwirkleTurn turn) {
        if (turn.getPlayer() == player) {
            // we just took a turn, so forget the previous pieces we drew -- we'll be getting new ones soon
            lastDraw = null;
            // force a UI update
            update();
        }
    }

    /** Update the UI. */
    private void update() {
        QwirkleTurn fakeTurn = createFakeTurn();
        getEventBus().post(fakeTurn);
    }

    /** Create a fake {@link QwirkleTurn} to tell the {@link QwirkleGridPanel} what to draw. */
    private QwirkleTurn createFakeTurn() {
        List<QwirklePlacement> handPlaces = new ArrayList<>();
        List<QwirklePiece> drawScratch = new ArrayList<>();
        if (lastDraw != null) drawScratch.addAll(lastDraw);
        List<QwirklePlacement> drawPlacements = new ArrayList<>();

        // make a board from our hand, along with the new pieces
        List<QwirklePiece> hand = mgr.getHand(player);
        if (hand != null) {
            // build a board that represents the hand
            for (int i = 0; i < hand.size(); ++i) {
                QwirklePiece piece = hand.get(i);
                QwirklePlacement place = new QwirklePlacement(piece, vertical ? 0 : i, vertical ? i : 0);
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
        return QwirkleTurn.drawToHand(handGrid, drawPlacements, player);
    }
}
