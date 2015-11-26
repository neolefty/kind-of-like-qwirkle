package qwirkle.ui.swing.main;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleColor;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.QwirkleSettings;
import qwirkle.game.base.QwirkleShape;
import qwirkle.game.control.impl.NewThreadEachTime;
import qwirkle.game.control.players.RainbowAI;
import qwirkle.game.event.*;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.event.HighlightTurn;
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
    // TODO add slide-out sidebar with game meta-controls & history

    private static QwirklePlayer createRainbowPlayer(String s, Collection<QwirkleColor> colors) {
        RainbowAI result = new RainbowAI(s, colors);
        result.setBias(5);
        result.getRainbow().setDislikeMonochrome(0); // single color strips are totally okay
        return new QwirklePlayer(result);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // settings
                List<QwirkleColor> colors = QwirkleColor.DEFAULT_COLORS;
//                List<QwirkleShape> shapes = QwirkleShape.DEFAULT_SHAPES;
//                int decks = 3;
//                List<QwirkleColor> colors = QwirkleColor.FIVE_COLORS;
//                List<QwirkleShape> shapes = QwirkleShape.FIVE_SHAPES;
                List<QwirkleShape> shapes = QwirkleShape.FOUR_SHAPES;
                int decks = 1;

                // players
                List<QwirklePlayer> players = new ArrayList<>();
                players.add(createRainbowPlayer("Rainbow", colors));
                players.add(createRainbowPlayer("Color Wheel", colors));
//                players.add(new QwirklePlayer(new MaxPlayer("Sam")));
//                players.add(new QwirklePlayer(new MaxPlayer("Gilly")));
//                players.add(new QwirklePlayer(new StupidPlayer("1")));

                QwirkleSettings settings = new QwirkleSettings(decks, shapes, colors, players);

//                QwirkleSettings settings = new QwirkleSettings(1, QwirkleShape.FIVE_SHAPES, QwirkleColor.FIVE_COLORS, players);
//                QwirkleSettings settings = new QwirkleSettings(2, QwirkleShape.EIGHT_SHAPES, QwirkleColor.EIGHT_COLORS, players);
//                QwirkleSettings settings = new QwirkleSettings(3, QwirkleShape.DEFAULT_SHAPES, QwirkleColor.SIX_GREYS, players);
//                QwirkleSettings settings = new QwirkleSettings(2, QwirkleShape.EIGHT_SHAPES, QwirkleColor.EIGHT_GREYS, players);
//                QwirkleSettings settings = new QwirkleSettings(3, QwirkleShape.EIGHT_SHAPES, QwirkleColor.FIVE_COLORS, players);

                // TODO move settings to a setup screen and dynamically update them
                QwirkleUIController control = new QwirkleUIController(settings, new NewThreadEachTime());
                control.getThreads().setStepMillis(650);

                // make a window frame
                final QwirkleFrame frame = new QwirkleFrame();
                frame.setSize(900, 600); // default size for first time
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

//                System.setProperty("awt.useSystemAAFontSettings","on");
//                System.setProperty("swing.aatext", "true");

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
                wakeOnGameEvents(ssp, control.getEventBus());
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

    private static void wakeOnGameEvents(final ScreenSaverPane ssp, EventBus bus) {
        bus.register(new Object() {
            @Subscribe public void turnPlayed(TurnCompleted event) { ssp.activityDetected(); }
            @Subscribe public void turnStart(TurnStarting event) { ssp.activityDetected(); }
            @Subscribe public void gameOver(GameOver event) { ssp.activityDetected(); }
            @Subscribe public void gameStart(GameStarted event) { ssp.activityDetected(); }
            @Subscribe public void draw(DrawPieces event) { ssp.activityDetected(); }
            @Subscribe public void drag(DragPiece event) { ssp.activityDetected(); }
            @Subscribe public void highlight(HighlightTurn event) { ssp.activityDetected(); }
        });
    }
}
