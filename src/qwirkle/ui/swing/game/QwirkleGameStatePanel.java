package qwirkle.ui.swing.game;

import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.event.GameStarted;
import qwirkle.ui.view.colors.Colors;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.swing.game.board.GameboardPanel;
import qwirkle.ui.swing.game.board.PlayableHighlighter;
import qwirkle.ui.swing.game.player.PlayerPanel;
import qwirkle.ui.swing.util.SwingKitty;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A JPanel that shows the state of a qwirkle game: the players and board. */
public class QwirkleGameStatePanel extends JPanel {
    // synchronize on this before making any changes
    private final Map<QwirklePlayer, PlayerPanel> playerPanelMap = new LinkedHashMap<>();
    private final DiscardPanel discardPanel;
    private QwirkleUIController control;

    public QwirkleGameStatePanel(QwirkleUIController control) {
        super(new QwirkleGameLayout());
        this.control = control;

        // board
        GameboardPanel grid = new GameboardPanel(control);
        grid.setBlankIncluded(true);
        add(grid);

        // discard panel
        discardPanel = new DiscardPanel(control.getDiscardController());
        add(discardPanel);

        // ui controllers
        new PlayableHighlighter(control, grid);
        control.register(new Object() {
            @Subscribe
            public void started(GameStarted started) {
                updatePlayers(started.getSettings().getPlayers());
            }
        });
    }

    private void updatePlayers(final List<QwirklePlayer> players) {
        removePlayerPanels();
        addPlayerPanels(players);
    }

    private void addPlayerPanels(List<QwirklePlayer> players) {
        synchronized (playerPanelMap) {
            for (QwirklePlayer player : players) {
                PlayerPanel pp = new PlayerPanel(control, player);
                playerPanelMap.put(player, pp);
                // set an invisible border now to take up the space so the size doesn't change later
                pp.setBorder(BorderFactory.createLineBorder(new Color(Colors.BG.getColorInt())));
                SwingKitty.setColors(pp, Colors.FG, Colors.BG);
                add(pp);
            }
        }
        validate();
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
