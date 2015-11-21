package qwirkle.ui.swing.game;

import qwirkle.ui.control.QwirkleUIController;

import javax.swing.*;
import java.awt.*;

/** A JPanel that shows a complete Qwirkle game, with board, players, controls, and status. */
public class QwirkleGamePanel extends JPanel {
    public QwirkleGamePanel(QwirkleUIController control) {
        super(new BorderLayout());

//        JLayeredPane

        // center: board & players
        add(new QwirkleGameStatePanel(control), BorderLayout.CENTER);
        // bottom: controls
        add(new GameControlPanel(control), BorderLayout.SOUTH);
        // TODO move controls to the bottom of the board, just above the controls, so they aren't next to the first player?
        // TODO consider shortening visible "Best" message to "Gilly, for 16 points" instead of "Gilly plays 2 for 16"
        // TODO add visible barrier or space between board and last player (especially in horizontal layout)
        // top: game status messages
        add(new GameStatusPanel(control), BorderLayout.NORTH);
        // below bottom: scores
//        add(new QwirkleScorePanel(mgr, 0.04), BorderLayout.SOUTH);
    }
}
