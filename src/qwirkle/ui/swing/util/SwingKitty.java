package qwirkle.ui.swing.util;

import java.awt.*;

/** Swing utilities. */
public class SwingKitty {
    // Recursively set the colors & font of comp to match our scheme
    public static void setColors(Component comp, Color fg, Color bg) {
        comp.setForeground(fg);
        comp.setBackground(bg);
        if (comp.getFont() != null)
            comp.setFont(comp.getFont().deriveFont(30f));
        if (comp instanceof Container) {
            Container panel = (Container) comp;
            synchronized (panel.getTreeLock()) {
                for (Component child : panel.getComponents())
                    setColors(child, fg, bg);
            }
        }
    }

    /** Combine two transparencies. */
    public static double combineTransparency(double t1, double t2) {
        return 1 - (1 - t1) * (1 - t2);
    }
}
