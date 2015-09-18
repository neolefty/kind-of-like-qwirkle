package qwirkle.attic.swing;

import qwirkle.control.GameManager;
import qwirkle.control.impl.NewThreadEachTime;
import qwirkle.game.AsyncPlayer;
import qwirkle.game.QwirkleSettings;
import qwirkle.game.impl.AsyncPlayerWrapper;
import qwirkle.players.MaxPlayer;
import qwirkle.players.StupidPlayer;
import qwirkle.ui.swing.main.QwirkleFrame;
import qwirkle.ui.swing.game.QwirkleGamePanel;
import qwirkle.ui.swing.colors.Colors;
import qwirkle.ui.swing.util.SwingKitty;
import qwirkle.ui.swing.util.SwingSetup;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SwingMainSimple {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // set up a game
                List<AsyncPlayer> players = new ArrayList<>();
                players.add(new AsyncPlayerWrapper(new MaxPlayer("Sam")));
                players.add(new AsyncPlayerWrapper(new MaxPlayer("Gilly")));
                players.add(new AsyncPlayerWrapper(new StupidPlayer("1")));
                QwirkleSettings settings = new QwirkleSettings(players);
                final GameManager game = new GameManager(settings, new NewThreadEachTime());
                game.start();

                // make a window frame
                QwirkleFrame frame = new QwirkleFrame();
                frame.setSize(900, 600); // default size for first time
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                // listen for movement & save it to prefs
                SwingSetup.addWindowSizer(frame, SwingMainSimple.class);

                // add a view of the game
                QwirkleGamePanel gamePanel = new QwirkleGamePanel(game);
                frame.setContentPane(gamePanel);

                // set colors (only need it once, after everything is added)
                SwingKitty.setColors(gamePanel, Colors.FG, Colors.BG);

                // show the window
                frame.setVisible(true);
            }
        });
    }
}
