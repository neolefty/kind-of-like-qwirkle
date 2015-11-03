package qwirkle.ui.swing.game;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameController;
import qwirkle.event.GameStarted;
import qwirkle.game.AsyncPlayer;
import qwirkle.ui.swing.colors.Colors;
import qwirkle.ui.swing.game.board.PlayableHighlighter;
import qwirkle.ui.swing.game.board.QwirklePlayableGridPanel;
import qwirkle.ui.swing.game.player.PlayerPanel;
import qwirkle.ui.swing.util.SwingKitty;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A JPanel that shows the state of a game: players and board. */
public class QwirkleGameStatePanel extends JPanel {
    // synchronize on this before making any changes
    private final Map<AsyncPlayer, PlayerPanel> playerPanelMap = new LinkedHashMap<>();
    private GameController control;

    public QwirkleGameStatePanel(GameController control) {
        super(new QwirkleGameLayout());
        this.control = control;

        // board
        QwirklePlayableGridPanel grid = new QwirklePlayableGridPanel(control);
        grid.setBlankIncluded(true);
        add(grid);

        // ui controllers
        new PlayableHighlighter(control, grid);
        control.register(new Object() {
            @Subscribe
            public void started(GameStarted started) {
                updatePlayers(started.getSettings().getPlayers());
            }
        });
    }

    private void updatePlayers(List<AsyncPlayer> players) {
        removePlayerPanels();
        addPlayerPanels(players);
    }

    private void addPlayerPanels(List<AsyncPlayer> players) {
        synchronized (playerPanelMap) {
            for (AsyncPlayer player : players) {
                PlayerPanel pp = new PlayerPanel(control, player);
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
