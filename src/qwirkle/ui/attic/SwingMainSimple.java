package qwirkle.ui.attic;

import qwirkle.control.GameManager;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleSettings;
import qwirkle.players.MaxPlayer;
import qwirkle.players.StupidPlayer;
import qwirkle.ui.main.QwirkleFrame;
import qwirkle.ui.main.QwirkleGamePanel;
import qwirkle.ui.util.SwingKitty;
import qwirkle.ui.util.SwingSetup;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SwingMainSimple {

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
                // set up a game
                List<QwirklePlayer> players = new ArrayList<>();
                players.add(new MaxPlayer("Sam"));
                players.add(new MaxPlayer("Gilly"));
                players.add(new StupidPlayer("1"));
                QwirkleSettings settings = new QwirkleSettings(players);
                final GameManager game = new GameManager(settings);
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
                SwingKitty.setColors(gamePanel);

                // show the window
                frame.setVisible(true);
            }
        });
    }
}
