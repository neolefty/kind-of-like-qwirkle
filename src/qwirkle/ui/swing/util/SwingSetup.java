package qwirkle.ui.swing.util;

import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

    public static JPanel createUI(QwirkleUIController control) {
        JPanel ui = new JPanel();
        ui.setLayout(new BorderLayout());
        ui.add(new QwirkleGridPanel(control.getEventBus()));
//        ui.add(new PlayerPanel("Random", new RandomPlayer())(engine), BorderLayout.SOUTH);
        return ui;
    }

    private static Preferences getPrefs(Class cls) {
        return Preferences.userNodeForPackage(cls);
    }
}
