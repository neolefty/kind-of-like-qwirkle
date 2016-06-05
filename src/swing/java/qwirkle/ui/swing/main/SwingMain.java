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
import qwirkle.ui.UIConstants;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.event.HighlightTurn;
import qwirkle.ui.swing.game.SwingGame;
import qwirkle.ui.swing.util.SwingKitty;
import qwirkle.ui.swing.util.SwingSetup;
import qwirkle.ui.view.Fader;
import qwirkle.ui.view.TransparencyFader;

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

//    public static final List<QwirkleColor> COLORS = QwirkleColor.createRainbow(6);
    public static final List<QwirkleColor> COLORS = QwirkleColor.DEFAULT_COLORS;
//    public static final List<QwirkleShape> SHAPES = QwirkleShape.EIGHT_SHAPES;
    public static final List<QwirkleShape> SHAPES = QwirkleShape.DEFAULT_SHAPES;
//    public static final int DECKS = 1;
    public static final int DECKS = 3;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // settings

                // players
                List<QwirklePlayer> players = new ArrayList<>();
                players.add(createRainbowPlayer("Rainbow", COLORS));
                players.add(createRainbowPlayer("Color Wheel", COLORS));
//                players.add(new QwirklePlayer(new MaxPlayer("Sam")));
//                players.add(new QwirklePlayer(new MaxPlayer("Gilly")));
//                players.add(new QwirklePlayer(new StupidPlayer("1")));

                QwirkleSettings settings = new QwirkleSettings(DECKS, SHAPES, COLORS, players);

                // TODO move settings to a setup screen and dynamically update them
                QwirkleUIController control = new QwirkleUIController(settings, new NewThreadEachTime());
                control.getThreads().setStepMillis(UIConstants.AUTOPLAY_STEP_MILLIS);

                // make a window frame
                final SwingMainFrame frame = new SwingMainFrame();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

//                System.setProperty("awt.useSystemAAFontSettings","on");
//                System.setProperty("swing.aatext", "true");

                // listen for movement & save it to prefs
                SwingSetup.addWindowRememberer(frame, SwingMain.class);

                // add a view of the game
                SwingGame gamePanel = new SwingGame(control);

                // add an overlay for dragging pieces
                frame.setGlassPane(new SwingDragPane(control.getEventBus()));

                // with a screensaver
                SwingShapeBouncer screensaver = createScreensaver(settings);

                // manage the game & screensaver panels with a ScreenSaverPane
                Fader fader = new TransparencyFader(screensaver, screensaver.getStepMillis());
                SwingScreenSaver ssp = new SwingScreenSaver
                        (gamePanel, screensaver, fader, UIConstants.SCREENSAVER_TIMEOUT);

                wakeOnGameEvents(ssp, control.getEventBus());
//                ssp.setFadeMillis(5000);
                frame.setContentPane(ssp);

                // set colors (only need it once, after everything is added)
                SwingKitty.setColors(frame, UIConstants.FG, UIConstants.BG);

                // show the window
                control.getGame().start();
                frame.setVisible(true);
            }
        });
    }

    private static QwirklePlayer createRainbowPlayer(String s, Collection<QwirkleColor> colors) {
        RainbowAI result = new RainbowAI(s, colors);
        result.setBias(5);
        result.getRainbow().setDislikeMonochrome(0); // single color strips are totally okay
        return new QwirklePlayer(result);
    }

    private static SwingShapeBouncer createScreensaver(QwirkleSettings settings) {
        SwingShapeBouncer result = new SwingShapeBouncer(SwingShapeBouncer.generatePieces(settings));
        result.setResetOnResume(true);
        result.setStepMillis(16);
        result.setEdgeTransparency(4);
        result.setSecondsToCross(8);
        result.setSecondsToRotate(4);
        return result;
    }

    private static void wakeOnGameEvents(final SwingScreenSaver ssp, EventBus bus) {
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
