package qwirkle.ui.main;

import qwirkle.control.GameManager;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleSettings;
import qwirkle.players.MaxPlayer;
import qwirkle.players.StupidPlayer;
import qwirkle.ui.swing.ScreenSaverPane;
import qwirkle.ui.swing.SwingKitty;
import qwirkle.ui.swing.SwingSetup;
import qwirkle.ui.swing.TransparencyFader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SwingMain {

    // TODO add human player
    // TODO add player panels
    // TODO add taunts
    // TODO choose shapes & colors to use
    // TODO enable designing your own shape / color
    // TODO test different numbers of shapes & colors
    // TODO start new game automatically if continuing
    // TODO debug board not resetting when new game starts

//    public static final long SCREENSAVER_TIMEOUT = 10 * 60 * 1000; // 10 minutes
    public static final long SCREENSAVER_TIMEOUT = 3 * 1000; // 3 seconds

    public static class Colors {
        public static final Color FG = Color.WHITE;
        public static final Color BG = Color.BLACK;
        public static final Color MOUSE = Color.GRAY.darker();
        public static final Color CLICK = Color.GRAY;
        public static final Color MOUSE_HL = Color.GRAY;
        public static final Color BG_HL = Color.DARK_GRAY;
        public static final Color CLICK_HL = Color.GRAY.brighter();
        public static final Color DEBUG = Color.GREEN;
        // Color.CYAN.darker();
        // Color.YELLOW.darker();
        // Color.BLUE.darker(),
        // Color.GREEN.darker(),
    }

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
                screensaver.setEdgeTransparency(4);
                screensaver.setSecondsToCross(8);
                screensaver.setSecondsToRotate(4);

                // manage the game & screensaver panels with a ScreenSaverPane
                ScreenSaverPane.Fader fader = new TransparencyFader(screensaver, screensaver.getStepMillis());
                ScreenSaverPane ssp = new ScreenSaverPane
                        (gamePanel, screensaver, fader, SCREENSAVER_TIMEOUT);
//                ssp.setFadeMillis(5000);
                frame.setContentPane(ssp);

                // set colors (only need it once, after everything is added)
                SwingKitty.setColors(frame, Colors.FG, Colors.BG);

                // show the window
                frame.setVisible(true);
            }
        });
    }
}
