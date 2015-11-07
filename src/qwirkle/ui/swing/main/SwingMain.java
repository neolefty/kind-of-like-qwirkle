package qwirkle.ui.swing.main;

import qwirkle.control.GameController;
import qwirkle.control.impl.NewThreadEachTime;
import qwirkle.game.AsyncPlayer;
import qwirkle.game.QwirkleColor;
import qwirkle.game.QwirkleSettings;
import qwirkle.game.QwirkleShape;
import qwirkle.game.impl.AsyncPlayerWrapper;
import qwirkle.players.RainbowPlayer;
import qwirkle.ui.swing.colors.Colors;
import qwirkle.ui.swing.game.QwirkleDragPane;
import qwirkle.ui.swing.game.QwirkleGamePanel;
import qwirkle.ui.swing.util.SwingKitty;
import qwirkle.ui.swing.util.SwingSetup;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SwingMain {
    // TODO add human player
    // TODO add taunts
    // TODO add designing your own shape / color
    // TODO choose shapes & colors to use
    // TODO start new game automatically if playing continuously
    // TODO disable screensaver if playing continuously

    private static AsyncPlayer createRainbowPlayer(String s, Collection<QwirkleColor> colors) {
        RainbowPlayer result = new RainbowPlayer(s, colors);
        result.setBias(5);
        result.getRainbow().setDislikeMonochrome(0);
        return new AsyncPlayerWrapper(result);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // settings
                List<QwirkleColor> colors = QwirkleColor.DEFAULT_COLORS;
                List<QwirkleShape> shapes = QwirkleShape.DEFAULT_SHAPES;
                int decks = 3;

                // players
                List<AsyncPlayer> players = new ArrayList<>();
                players.add(createRainbowPlayer("Rainbow", colors));
                players.add(createRainbowPlayer("Color Wheel", colors));
//                players.add(new AsyncPlayerWrapper(new MaxPlayer("Sam")));
//                players.add(new AsyncPlayerWrapper(new MaxPlayer("Gilly")));
//                players.add(new AsyncPlayerWrapper(new StupidPlayer("1")));

                QwirkleSettings settings = new QwirkleSettings(decks, shapes, colors, players);

//                QwirkleSettings settings = new QwirkleSettings(1, QwirkleShape.FIVE_SHAPES, QwirkleColor.FIVE_COLORS, players);
//                QwirkleSettings settings = new QwirkleSettings(2, QwirkleShape.EIGHT_SHAPES, QwirkleColor.EIGHT_COLORS, players);
//                QwirkleSettings settings = new QwirkleSettings(3, QwirkleShape.DEFAULT_SHAPES, QwirkleColor.SIX_GREYS, players);
//                QwirkleSettings settings = new QwirkleSettings(2, QwirkleShape.EIGHT_SHAPES, QwirkleColor.EIGHT_GREYS, players);
//                QwirkleSettings settings = new QwirkleSettings(3, QwirkleShape.EIGHT_SHAPES, QwirkleColor.FIVE_COLORS, players);

                // TODO move settings to a setup screen and dynamically update them
                GameController control = new GameController(settings, new NewThreadEachTime());

                // make a window frame
                final QwirkleFrame frame = new QwirkleFrame();
                frame.setSize(900, 600); // default size for first time
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                // listen for movement & save it to prefs
                SwingSetup.addWindowSizer(frame, SwingMain.class);

                // add a view of the game
                QwirkleGamePanel gamePanel = new QwirkleGamePanel(control);

                // add an overlay for dragging pieces
                frame.setGlassPane(new QwirkleDragPane(control.getEventBus()));

                // with a screensaver
                ShapeBouncer screensaver = new ShapeBouncer(control.getGame());
                screensaver.setResetOnResume(true);
                screensaver.setStepMillis(16);
                screensaver.setEdgeTransparency(4);
                screensaver.setSecondsToCross(8);
                screensaver.setSecondsToRotate(4);

                // manage the game & screensaver panels with a ScreenSaverPane
                ScreenSaverPane.Fader fader = new TransparencyFader(screensaver, screensaver.getStepMillis());
                ScreenSaverPane ssp = new ScreenSaverPane
                        (gamePanel, screensaver, fader, UIConstants.SCREENSAVER_TIMEOUT);
//                ssp.setFadeMillis(5000);
                frame.setContentPane(ssp);

                // set colors (only need it once, after everything is added)
                SwingKitty.setColors(frame, Colors.FG, Colors.BG);

                // show the window
                control.getGame().start();
                frame.setVisible(true);
            }
        });
    }
}
