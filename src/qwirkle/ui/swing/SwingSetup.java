package qwirkle.ui.swing;

import qwirkle.ui.attic.BoardMonolithicPanel;
import qwirkle.ui.attic.QwirkleGridLayoutPanel;
import qwirkle.ui.board.QwirkleGridPanel;
import qwirkle.ui.attic.SquareLayout;
import qwirkle.control.GameManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;

public class SwingSetup {
    /** Automatically save & restore size of window from prefs. */
    public static void addWindowSizer(final JFrame frame, Class classForPrefs) {
        // dismiss move operations until we've established our position from prefs
        final boolean[] windowOpened = { false };
        final Preferences prefs = getPrefs(classForPrefs);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                frame.setBounds(getStartingBounds(prefs));
                windowOpened[0] = true;
            }
            @Override
            public void windowClosing(WindowEvent e) {
                saveWindowBounds(frame, prefs);
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (windowOpened[0]) // we'll get this event before windowOpened
                    saveWindowBounds(frame, prefs);
            }
            @Override
            public void componentMoved(ComponentEvent e) {
                if (windowOpened[0]) // we'll get this event before windowOpened
                    saveWindowBounds(frame, prefs);
            }
        });
    }

    // obsoleted by Frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
/*
    public static void exitOnClose(JFrame frame) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
*/

    public static void saveWindowBounds(JFrame frame, Preferences prefs) {
        if (frame.isVisible()) {
            Rectangle r = frame.getBounds();
            prefs.putInt(SetupKit.PREFS_WINDOW_LEFT, r.x);
            prefs.putInt(SetupKit.PREFS_WINDOW_TOP, r.y);
            prefs.putInt(SetupKit.PREFS_WINDOW_WIDTH, r.width);
            prefs.putInt(SetupKit.PREFS_WINDOW_HEIGHT, r.height);
        }
    }

    public static Rectangle getStartingBounds(Preferences prefs) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int left = prefs.getInt(SetupKit.PREFS_WINDOW_LEFT, screenSize.width / 4);
        int top = prefs.getInt(SetupKit.PREFS_WINDOW_TOP, screenSize.height / 4);
        int width = prefs.getInt(SetupKit.PREFS_WINDOW_WIDTH, screenSize.width / 2);
        int height = prefs.getInt(SetupKit.PREFS_WINDOW_HEIGHT, screenSize.height / 2);

        return new Rectangle(left, top, width, height);
    }

    // compare the three game board panels
    public static JComponent createThreePanelTest(GameManager game) {
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(new BoardMonolithicPanel(game.getEventBus()));
        box.add(new QwirkleGridPanel(game.getEventBus()));
        box.add(new SquareLayout.SquarePanel(new QwirkleGridLayoutPanel(game.getEventBus())));
        return box;
    }

    public static JPanel createUI(GameManager game) {
        JPanel ui = new JPanel();
        ui.setLayout(new BorderLayout());
        ui.add(new QwirkleGridPanel(game.getEventBus()));
//        ui.add(new PlayerPanel("Random", new RandomPlayer())(engine), BorderLayout.SOUTH);
        return ui;
    }

    private static Preferences getPrefs(Class cls) {
        return Preferences.userNodeForPackage(cls);
    }
}
