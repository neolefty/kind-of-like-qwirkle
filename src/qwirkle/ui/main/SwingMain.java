package qwirkle.ui.main;

import qwirkle.control.GameManager;
import qwirkle.control.NewThreadEachTime;
import qwirkle.game.AsyncPlayer;
import qwirkle.game.QwirkleSettings;
import qwirkle.players.AsyncPlayerWrapper;
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
    // TODO add taunts
    // TODO add designing your own shape / color
    // TODO choose shapes & colors to use
    // TODO start new game automatically if playing continuously
    // TODO disable screensaver if playing continuously

    public static final long SCREENSAVER_TIMEOUT = 10 * 60 * 1000; // 10 minutes
//    public static final long SCREENSAVER_TIMEOUT = 3 * 1000; // 3 seconds

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
                List<AsyncPlayer> players = new ArrayList<>();
                players.add(new AsyncPlayerWrapper(new MaxPlayer("Sam")));
                players.add(new AsyncPlayerWrapper(new MaxPlayer("Gilly")));
                players.add(new AsyncPlayerWrapper(new StupidPlayer("1")));
                QwirkleSettings settings = new QwirkleSettings(players);
                final GameManager game = new GameManager(settings, new NewThreadEachTime());

                // make a window frame
                final QwirkleFrame frame = new QwirkleFrame();
                frame.setSize(900, 600); // default size for first time
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                // listen for movement & save it to prefs
                SwingSetup.addWindowSizer(frame, SwingMain.class);

                // add a view of the game
                QwirkleGamePanel gamePanel = new QwirkleGamePanel(game);

                // add an overlay for dragging pieces
                frame.setGlassPane(new QwirkleDragPane(game.getEventBus(), gamePanel));
//                frame.getGlassPane().setVisible(true);

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
                game.start();
                frame.setVisible(true);
            }
        });
    }
}
