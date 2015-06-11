package qwirkle.ui.board;

import qwirkle.control.GameManager;
import qwirkle.game.QwirklePlayer;
import qwirkle.ui.main.QwirkleGameLayout;
import qwirkle.ui.main.SwingMain;

import javax.swing.*;
import java.awt.*;

/** A JPanel that shows the state of a game: players and status. */
public class QwirkleGameStatePanel extends JPanel {
    public QwirkleGameStatePanel(GameManager mgr) {
        super(new QwirkleGameLayout());

        // player panels
        // TODO update when players are added / removed
        for (QwirklePlayer player : mgr.getPlayers()) {
            PlayerPanel pp = new PlayerPanel(mgr, player);
            pp.setBorder(BorderFactory.createLineBorder(SwingMain.Colors.DEBUG));
            add(pp);
        }

        // board
        QwirkleGridPanel grid = new QwirkleGridPanel(mgr.getEventBus());
        grid.setBlankIncluded(false);
        add(grid);
    }
}
