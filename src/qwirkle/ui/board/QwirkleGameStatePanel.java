package qwirkle.ui.board;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameManager;
import qwirkle.control.event.GameStarted;
import qwirkle.control.event.TurnStarting;
import qwirkle.game.QwirklePlayer;
import qwirkle.ui.main.QwirkleGameLayout;
import qwirkle.ui.main.SwingMain;
import qwirkle.ui.swing.SwingKitty;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A JPanel that shows the state of a game: players and status. */
public class QwirkleGameStatePanel extends JPanel {
    // synchronize on this before making any changes
    private final Map<QwirklePlayer, PlayerPanel> playerPanelMap = new LinkedHashMap<>();
    private GameManager mgr;

    public QwirkleGameStatePanel(GameManager mgr) {
        super(new QwirkleGameLayout());

        this.mgr = mgr;
        mgr.getEventBus().register(new Object() {
            @Subscribe public void started(GameStarted started) {
                updatePlayers(started.getSettings().getPlayers());
            }
            @Subscribe public void turn(TurnStarting turn) {
                updateHighlight(turn.getCurPlayer());
            }
        });

        // board
        QwirkleGridPanel grid = new QwirkleGridPanel(mgr.getEventBus());
        grid.setBlankIncluded(false);
        add(grid);
    }

    private void updateHighlight(QwirklePlayer curPlayer) {
        for (QwirklePlayer player : playerPanelMap.keySet()) {
            Color bgColor = (curPlayer == player) ? SwingMain.Colors.BG_HL : SwingMain.Colors.BG;
            playerPanelMap.get(player).setBackground(bgColor);
            Color borderColor = (curPlayer == player) ? SwingMain.Colors.FG : SwingMain.Colors.BG;
            playerPanelMap.get(player).setBorder(BorderFactory.createLineBorder(borderColor));
        }
    }

    private void updatePlayers(List<QwirklePlayer> players) {
        removePlayerPanels();
        addPlayerPanels(players);
    }

    private void addPlayerPanels(List<QwirklePlayer> players) {
        synchronized (playerPanelMap) {
            for (QwirklePlayer player : players) {
                PlayerPanel pp = new PlayerPanel(mgr, player);
                playerPanelMap.put(player, pp);
                // set an invisible border now to take up the space so the size doesn't change later
                pp.setBorder(BorderFactory.createLineBorder(SwingMain.Colors.BG));
                SwingKitty.setColors(pp, SwingMain.Colors.FG, SwingMain.Colors.BG);
                add(pp);
            }
        }
    }

    private void removePlayerPanels() {
        // better not do this while also synchronizing on getTreeLock() because it will deadlock
        synchronized (playerPanelMap) {
            for (PlayerPanel pp : playerPanelMap.values())
                remove(pp);
            playerPanelMap.clear();
        }
    }
}
