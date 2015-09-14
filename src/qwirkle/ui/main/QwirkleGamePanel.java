package qwirkle.ui.main;

import qwirkle.control.GameManager;
import qwirkle.ui.board.GameControlPanel;
import qwirkle.ui.board.GameStatusPanel;
import qwirkle.ui.board.QwirkleGameStatePanel;

import javax.swing.*;
import java.awt.*;

/** A JPanel that shows a complete Qwirkle game, with board, players, controls, and status. */
public class QwirkleGamePanel extends JPanel {
    public QwirkleGamePanel(GameManager mgr) {
        super(new BorderLayout());

        // center: game & players
        add(new QwirkleGameStatePanel(mgr), BorderLayout.CENTER);
        // bottom: controls
        add(new GameControlPanel(mgr), BorderLayout.SOUTH);
        // TODO move these to the bottom of the board, just above the controls, so they aren't next to the first player?
        // TODO consider shortening visible "Best" message to "Gilly, for 16 points" instead of "Gilly plays 2 for 16"
        // TODO add visible barrier or space between board and last player (especially in horizontal layout)
        // top: game status messages
        add(new GameStatusPanel(mgr), BorderLayout.NORTH);
        // below bottom: scores
//        add(new QwirkleScorePanel(mgr, 0.04), BorderLayout.SOUTH);
    }
}
