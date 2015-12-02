package qwirkle.ui.swing.main;

import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.QwirkleSettings;
import qwirkle.game.control.impl.NewThreadEachTime;
import qwirkle.game.control.players.MaxAI;
import qwirkle.game.control.players.StupidAI;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.swing.colors.Colors;
import qwirkle.ui.swing.game.QwirkleGamePanel;
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
                List<QwirklePlayer> players = new ArrayList<>();
                players.add(new QwirklePlayer(new MaxAI("Sam")));
                players.add(new QwirklePlayer(new MaxAI("Gilly")));
                players.add(new QwirklePlayer(new StupidAI("1")));
                QwirkleSettings settings = new QwirkleSettings(players);
                QwirkleUIController control = new QwirkleUIController(settings, new NewThreadEachTime());
                control.getGame().start();

                // make a window frame
                QwirkleFrame frame = new QwirkleFrame();
                frame.setSize(900, 600); // default size for first time
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                // listen for movement & save it to prefs
                SwingSetup.addWindowSizer(frame, SwingMainSimple.class);

                // add a view of the game
                QwirkleGamePanel gamePanel = new QwirkleGamePanel(control);
                frame.setContentPane(gamePanel);

                // set colors (only need it once, after everything is added)
                SwingKitty.setColors(gamePanel, Colors.FG, Colors.BG);

                // show the window
                frame.setVisible(true);
            }
        });
    }
}
