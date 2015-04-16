package qwirkle.ui.util;

import java.awt.*;

/** Swing utilities. */
public class SwingKitty {
    // Recursively set the colors & font of comp to match our scheme
    public static void setColors(Component comp) {
        comp.setForeground(Color.WHITE);
        comp.setBackground(Color.BLACK);
        if (comp.getFont() != null)
            comp.setFont(comp.getFont().deriveFont(30f));
        if (comp instanceof Container) {
            Container panel = (Container) comp;
            synchronized (panel.getTreeLock()) {
                for (Component child : panel.getComponents())
                    setColors(child);
            }
        }
    }

    /** Combine two transparencies. */
    public static double combineTransparency(double t1, double t2) {
        return 1 - (1 - t1) * (1 - t2);
    }
}
