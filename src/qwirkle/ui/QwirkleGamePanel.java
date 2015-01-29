package qwirkle.ui;

import qwirkle.control.GameManager;
import qwirkle.game.QwirklePlayer;
import qwirkle.ui.board.QwirkleGridPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** A JPanel that shows the state of a game, including players and status. */
public class QwirkleGamePanel extends JPanel {
    private GameManager mgr;

    public QwirkleGamePanel(GameManager mgr) {
        this.mgr = mgr;
//        setLayout(new GridBagLayout());
        setLayout(new BorderLayout());

        // main panel: show the current game
        final QwirkleGridPanel grid = new QwirkleGridPanel(mgr.getEventBus());
        grid.setBlankIncluded(false);
        add(grid, BorderLayout.CENTER);

        // player panels
        List<PlayerPanel> playerPanels = new ArrayList<>();
        for (QwirklePlayer player : mgr.getPlayers())
            playerPanels.add(new PlayerPanel(mgr, player));
        String[] sides = { BorderLayout.WEST, BorderLayout.EAST };
        // a terrible idea: alternate players left-right-left-right
        for (int i = 0; i < playerPanels.size(); ++i)
            add(playerPanels.get(i), sides[i % sides.length]);
    }
}
