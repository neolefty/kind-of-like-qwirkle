package qwirkle.ui.swing.game;

import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.swing.game.meta.SwingHamburger;
import qwirkle.ui.swing.game.meta.SwingMetaGame;
import qwirkle.ui.swing.util.SlideOutPanel;

import javax.swing.*;
import java.awt.*;

/** A JPanel that shows a complete Qwirkle game, with board, players, controls, and status. */
public class SwingGamePanel extends SlideOutPanel<JPanel, SwingMetaGame> {
    public SwingGamePanel(QwirkleUIController control) {
        // main panel: game state
        // slide-out panel: meta-game
        super(createMain(control), new SwingMetaGame(control.getEventBus()));
    }

    private static abstract class HasHamburger extends JPanel {
        abstract SwingHamburger getHamburger();
    }

    public static HasHamburger createMain(QwirkleUIController control) {
        HasHamburger result = new HasHamburger() {
            @Override
            SwingHamburger getHamburger() {
                return null;
            }
        };
        result.setLayout(new BorderLayout());

        // center: board & players
        result.add(new SwingGameState(control), BorderLayout.CENTER);
        // bottom: controls
        result.add(new SwingGameControl(control), BorderLayout.SOUTH);
        // TODO move controls to the bottom of the board, just above the controls, so they aren't next to the first player?
        // TODO consider shortening visible "Best" message to "Gilly, for 16 points" instead of "Gilly plays 2 for 16"
        // TODO add visible barrier or space between board and last player (especially in horizontal layout)
        // top: game status messages
        result.add(new SwingGameStatus(control), BorderLayout.NORTH);

//        result.add(new JButton("East"), BorderLayout.EAST);

        return result;
    }
}
