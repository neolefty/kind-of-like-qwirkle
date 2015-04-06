package qwirkle.ui.board;

import qwirkle.control.GameManager;
import qwirkle.game.QwirklePlayer;
import qwirkle.ui.main.QwirkleGameLayout;

import javax.swing.*;
import java.awt.*;

/** A JPanel that shows the state of a game, including players and status. */
public class QwirkleGamePanel extends JPanel {
    public QwirkleGamePanel(GameManager mgr) {
        super(new QwirkleGameLayout());

        // player panels
        // TODO update when players are added / removed
        for (QwirklePlayer player : mgr.getPlayers()) {
            PlayerPanel pp = new PlayerPanel(mgr, player);
            pp.setBorder(BorderFactory.createLineBorder(Color.GREEN));
            add(pp);
        }

        // board
        QwirkleGridPanel grid = new QwirkleGridPanel(mgr.getEventBus());
        grid.setBlankIncluded(false);
        add(grid);
    }
}
