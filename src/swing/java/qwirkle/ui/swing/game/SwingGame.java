package qwirkle.ui.swing.game;

import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.swing.game.meta.SwingMetaGame;
import qwirkle.ui.swing.util.SlideOutPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** A JPanel that shows a complete Qwirkle game, with board, players, controls, and status. */
public class SwingGame extends SlideOutPanel<JPanel, SwingMetaGame> {
    public SwingGame(QwirkleUIController control) {
        // main panel: game state
        setMainComp(createMain(control));
        // slide-out panel: meta-game
        setSlideComp(new SwingMetaGame(control.getEventBus()));
    }

    private JPanel createMain(QwirkleUIController control) {
        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());

        final SwingBoardAndPlayers board = new SwingBoardAndPlayers(control);
        final boolean[] toggleSlide = { false };
        board.getHamburger().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleSlide[0] = !toggleSlide[0];
                SwingGame.this.setSlideVisible(toggleSlide[0]);
            }
        });

        // center: board & players
        result.add(board, BorderLayout.CENTER);

        // bottom: controls
        result.add(new SwingGameControl(control), BorderLayout.SOUTH);
        // TODO move controls to the bottom of the board, just above the controls, so they aren't next to the first player?
        // TODO consider shortening visible "Best" message to "Gilly, for 16 points" instead of "Gilly plays 2 for 16"
        // TODO add visible barrier or space between board and last player (especially in horizontal layout)

        // top: game status messages
        result.add(new SwingGameStatus(control), BorderLayout.NORTH);

        // close hamburger menu when we click in the main panel
        result.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSlideVisible(false);
            }
        });

//        result.add(new SwingMetaGame(control.getEventBus()), BorderLayout.WEST);
//        result.add(new SwingWinLoss(control.getEventBus()), BorderLayout.EAST);

//        result.add(new JButton("East"), BorderLayout.EAST);

        return result;
    }
}
