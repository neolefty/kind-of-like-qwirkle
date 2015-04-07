package qwirkle.ui.main;

import qwirkle.control.GameManager;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleSettings;
import qwirkle.players.MaxPlayer;
import qwirkle.ui.board.GameControlPanel;
import qwirkle.ui.board.GameStatusPanel;
import qwirkle.ui.board.QwirkleGamePanel;
import qwirkle.ui.util.SwingKitty;
import qwirkle.ui.util.SwingSetup;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SwingMain {

    // TODO add human player
    // TODO add player panels
    // TODO add taunts
    // TODO choose shapes & colors to use
    // TODO enable designing your own shape / color
    // TODO test different numbers of shapes & colors

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // make a window
                QwirkleFrame frame = new QwirkleFrame();
                frame.setSize(900, 600);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                // listen for movement & save it to prefs
                SwingSetup.addWindowSizer(frame);

                // set up a game
                java.util.List<QwirklePlayer> players
                        = Arrays.asList((QwirklePlayer) new MaxPlayer("Sam"), new MaxPlayer("Gilly"));
//        final QwirklePlayer player3 = new StupidPlayer("1");
                QwirkleSettings settings = new QwirkleSettings(players);
                final GameManager game = new GameManager(settings);

                // outer panel: controls on bottom & game as main (center) component
                final JPanel outer = new JPanel();
                outer.setLayout(new BorderLayout());
                frame.setContentPane(outer);

                // center: game & players
                QwirkleGamePanel gamePanel = new QwirkleGamePanel(game);
                outer.add(gamePanel, BorderLayout.CENTER);

                // bottom: game controls
                GameControlPanel controls = new GameControlPanel(game);
                outer.add(controls, BorderLayout.SOUTH);

                // top: game status messages
                GameStatusPanel status = new GameStatusPanel(game);
                outer.add(status, BorderLayout.NORTH);

                // set colors at the end (only need it once, after everything is added)
                SwingKitty.setColors(outer);

                // show the window
                frame.setVisible(true);
                game.start();
            }
        });
    }
}
