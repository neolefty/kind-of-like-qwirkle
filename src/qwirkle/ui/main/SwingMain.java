package qwirkle.ui.main;

import qwirkle.control.GameManager;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleSettings;
import qwirkle.players.MaxPlayer;
import qwirkle.players.StupidPlayer;
import qwirkle.ui.util.ScreenSaverPane;
import qwirkle.ui.util.SwingKitty;
import qwirkle.ui.util.SwingSetup;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SwingMain {

    // TODO add human player
    // TODO add player panels
    // TODO add taunts
    // TODO choose shapes & colors to use
    // TODO enable designing your own shape / color
    // TODO test different numbers of shapes & colors

    public static final long SCREENSAVER_TIMEOUT = 3 * 60 * 1000; // 3 minutes
//    public static final long SCREENSAVER_TIMEOUT = 3 * 1000; // 3 seconds

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
                SwingSetup.addWindowSizer(frame, SwingMain.class);

                // add a view of the game
                QwirkleGamePanel gamePanel = new QwirkleGamePanel(game);

                // with a screensaver
                ShapeBouncer screensaver = new ShapeBouncer(game);
                screensaver.setResetOnResume(true);
                screensaver.setStepMillis(16);
                screensaver.setTransparency(4);
                screensaver.setSecondsToCross(8);
                screensaver.setSecondsToRotate(4);

                // manage the game & screensaver panels with a ScreenSaverPane
                ScreenSaverPane ssp = new ScreenSaverPane(gamePanel, screensaver, SCREENSAVER_TIMEOUT);
                frame.setContentPane(ssp);

                // set colors (only need it once, after everything is added)
                SwingKitty.setColors(frame);

                // show the window
                frame.setVisible(true);
            }
        });
    }
}
