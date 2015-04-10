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
        // top: game status messages
        add(new GameStatusPanel(mgr), BorderLayout.NORTH);
    }
}
