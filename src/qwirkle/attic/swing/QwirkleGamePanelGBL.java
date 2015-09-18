package qwirkle.attic.swing;

import qwirkle.control.GameManager;
import qwirkle.game.AsyncPlayer;
import qwirkle.ui.swing.game.player.PlayerPanel;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;

import javax.swing.*;
import java.awt.*;

/** A JPanel that shows the state of a game, including players and status.
 *  Uses GridBagLayout; replaced with our own layout, QwirkleGameLayout. */
public class QwirkleGamePanelGBL extends JPanel {
    public QwirkleGamePanelGBL(GameManager mgr) {
        setLayout(new GridBagLayout());
        Insets zeroInsets = new Insets(0, 0, 0, 0);
        GridBagConstraints constraints = new GridBagConstraints
                (0,0, // (x,y) = (0,0)
                 1,1, // grid height & width
                 1,1, // weight x,y
                 GridBagConstraints.CENTER, // maybe want NORTHWEST?
                 GridBagConstraints.BOTH, // fill
                 zeroInsets,
                 0, 0); // padding x,y

        // player panels
        constraints.gridy = 0;
        constraints.weightx = .25;
        for (AsyncPlayer player : mgr.getPlayers()) {
            add(new PlayerPanel(mgr, player), constraints);
            constraints.gridx++;
        }
        constraints.weightx = 1; // put it back the way it was

        // main panel: show the current game
        final QwirkleGridPanel grid = new QwirkleGridPanel(mgr.getEventBus());
        grid.setBlankIncluded(false);

        constraints.gridx++; constraints.gridy = 0;
        add(grid, constraints);
    }
}
