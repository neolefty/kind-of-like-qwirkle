package qwirkle.ui.main;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameManager;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleSettings;
import qwirkle.players.MaxPlayer;
import qwirkle.players.StupidPlayer;
import qwirkle.ui.util.SwingKitty;
import qwirkle.ui.util.SwingSetup;
import qwirkle.ui.util.UserActivityTimeout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SwingMainWithScreenSaver {

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
                frame.setSize(900, 600);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                // listen for movement & save it to prefs
                SwingSetup.addWindowSizer(frame, SwingMainWithScreenSaver.class);

                QwirkleGamePanel gamePanel = new QwirkleGamePanel(game);

                // after a few minutes of inactivity, show the screen saver
                final ShapeBouncer shapeBouncer = new ShapeBouncer(game);
                shapeBouncer.setTransparency(4);
                shapeBouncer.setResetOnResume(true);
                shapeBouncer.setSecondsToCross(8);
                shapeBouncer.setSecondsToRotate(4);
                UserActivityTimeout timeout = new UserActivityTimeout(gamePanel, game.getEventBus(), 2000, 500);
                timeout.addWatched(shapeBouncer); // if the screensaver panel sees activity, wake up
                shapeBouncer.setBounds(0, 0, 1000, 1000);
                gamePanel.setBounds(0, 0, 1000, 1000);
                final JLayeredPane layers = new JLayeredPane();
                layers.add(gamePanel, JLayeredPane.DEFAULT_LAYER);
                layers.add(shapeBouncer, JLayeredPane.MODAL_LAYER);
                game.getEventBus().register(new Object() {
                    @Subscribe public void timeout(UserActivityTimeout.TimeoutEvent event) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                shapeBouncer.setVisible(true);
                            }
                        });
                    }
                    @Subscribe public void resume(UserActivityTimeout.ResumeEvent event) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                shapeBouncer.setVisible(false);
                            }
                        });
                    }
                });

                // put a view of the game into the window
//                final JPanel outer = new QwirkleGamePanel(game);
                frame.setContentPane(layers);

                // set colors (only need it once, after everything is added)
                SwingKitty.setColors(layers);

                // show the window
                frame.setVisible(true);
            }
        });
    }
}
