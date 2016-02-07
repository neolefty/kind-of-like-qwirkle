package qwirkle.ui.swing.game.meta;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.control.GameHistory;
import qwirkle.ui.event.HistoryUpdate;

import javax.swing.*;
import java.awt.*;

/** UI for multi-game history and new games.
 *  (Suggest that it slide out from the side of the main UI). */
public class SwingMetaGame extends JPanel {
    private SwingWinLoss winLossPanel;

    public SwingMetaGame(EventBus eventBus) {
        winLossPanel = new SwingWinLoss(eventBus);
        eventBus.register(new Object() {
            @Subscribe public void updated(HistoryUpdate event) {
                update(event.getHistory());
            }
        });
        setLayout(new BorderLayout());
        add(new JScrollPane(winLossPanel), BorderLayout.CENTER);
    }

    /** A game has ended, so update our display. */
    private void update(GameHistory history) {
//        int n = history.getAllGames().size();
//        System.out.println("Update: " + n + " games so far. Winner: "
//                + history.getAllGames().get(n - 1).getAnnotated().getLeader());
//        winLossPanel.update(history);
    }
}
