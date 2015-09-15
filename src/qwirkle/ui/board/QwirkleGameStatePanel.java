package qwirkle.ui.board;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameManager;
import qwirkle.control.event.GameStarted;
import qwirkle.control.event.TurnStarting;
import qwirkle.game.AsyncPlayer;
import qwirkle.ui.main.QwirkleGameLayout;
import qwirkle.ui.paint.colors.Colors;
import qwirkle.ui.swing.SwingKitty;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A JPanel that shows the state of a game: players and status. */
public class QwirkleGameStatePanel extends JPanel {
    // synchronize on this before making any changes
    private final Map<AsyncPlayer, PlayerPanel> playerPanelMap = new LinkedHashMap<>();
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
        grid.setBlankIncluded(true);
        add(grid);
    }

    // TODO move this to PlayerPanel -- they can listen for events too
    // TODO abstract it a bit -- "setCurrent(curPlayer == player)" and leave the styling up to the PlayerPanel
    private void updateHighlight(AsyncPlayer curPlayer) {
        for (AsyncPlayer player : playerPanelMap.keySet()) {
            boolean cur = curPlayer == player;

            Color bg = (cur) ? Colors.BG_HL : Colors.BG;
//            SwingKitty.setColors(playerPanelMap.get(player), SwingMain.Colors.FG, bg);
            PlayerPanel pp = playerPanelMap.get(player);
            pp.setBackground(bg);
            Color borderColor = (cur) ? Colors.FG : Colors.BG;
            pp.setBorder(BorderFactory.createLineBorder(borderColor));
            pp.setDraggable(cur);
        }
    }

    private void updatePlayers(List<AsyncPlayer> players) {
        removePlayerPanels();
        addPlayerPanels(players);
    }

    private void addPlayerPanels(List<AsyncPlayer> players) {
        synchronized (playerPanelMap) {
            for (AsyncPlayer player : players) {
                PlayerPanel pp = new PlayerPanel(mgr, player);
                playerPanelMap.put(player, pp);
                // set an invisible border now to take up the space so the size doesn't change later
                pp.setBorder(BorderFactory.createLineBorder(Colors.BG));
                SwingKitty.setColors(pp, Colors.FG, Colors.BG);
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
