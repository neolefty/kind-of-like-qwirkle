package qwirkle.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameManager;
import qwirkle.control.GameStarted;
import qwirkle.game.*;
import qwirkle.game.impl.QwirkleGridImpl;
import qwirkle.ui.board.QwirkleGridPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** Show a player's status. Uses a {@link QwirkleGridPanel} with
 *  hacked event updates to show the player's current hand, highlighting
 *  the most recently drawn pieces. */
public class PlayerPanel extends JPanel {
    private GameManager mgr;
    private QwirklePlayer player;
    // what pieces did the player draw last?
    private List<QwirklePiece> lastDraw;
    // what was our most recent turn?
    private QwirkleTurn lastTurn;
    // send events to the hand panel through this bus
    private EventBus handBus;

    private Boolean vertical = null;

    private QwirkleGridPanel handPanel;

    public PlayerPanel(GameManager mgr, QwirklePlayer player) {
        this.mgr = mgr;
        this.player = player;
        mgr.getEventBus().register(new GameListener());
        handPanel = new QwirkleGridPanel(mgr.getEventBus());
        handBus = new EventBus("Fake board events for " + player.getName());

        handPanel = new QwirkleGridPanel(handBus);
        handPanel.setBlankIncluded(false);
//        handPanel.setMinimumSize(new Dimension(100, 100));
        setVertical();
    }

    private void setOrientation(boolean vertical) {
        if (this.vertical == null || !this.vertical.equals(vertical)) {
            this.vertical = vertical;
            if (handPanel.getParent() != null)
                remove(handPanel);
            setLayout(new BoxLayout(this, vertical ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
            add(handPanel);
        }
    }

    public void setHorizontal() { setOrientation(false); }
    public void setVertical() { setOrientation(true); }

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
        lastTurn = null;
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
            lastTurn = turn;
            // we just took a turn, so forget the previous pieces we drew -- we'll be getting new ones soon
            lastDraw = null;
            // force a UI update
            update();
        }
    }

    /** Update the UI. */
    private void update() {
        QwirkleTurn fakeTurn = createFakeTurn();
        handBus.post(fakeTurn);
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
