package qwirkle.ui.swing.util;

import qwirkle.game.base.QwirkleColor;

import javax.swing.*;
import java.awt.*;

/** Swing utilities. */
public class SwingKitty {
    // Recursively set the colors & font of comp to match our scheme
    public static void setColors(Component comp, QwirkleColor fg, QwirkleColor bg) {
        comp.setForeground(new Color(fg.getColorInt()));
        comp.setBackground(new Color(bg.getColorInt()));
        // TODO fix mac colors
        if (comp.getFont() != null)
            comp.setFont(comp.getFont().deriveFont(30f));
        if (comp instanceof Container) {
            Container panel = (Container) comp;
            synchronized (panel.getTreeLock()) {
                for (Component child : panel.getComponents())
                    setColors(child, fg, bg);
            }
        }
        // for buttons, set tooltip to be the button text (if they don't already have a tooltip)
        // helpful on a mac where buttons obscure their text colors under our half-baked scheme
        if (comp instanceof JComponent) {
            JComponent jc = (JComponent) comp;
            if (isEmpty(jc.getToolTipText()) && jc instanceof AbstractButton) {
                AbstractButton ab = (AbstractButton) jc;
                // update whenever text changes
                ab.addPropertyChangeListener("text", e -> ab.setToolTipText(ab.getText()));
                ab.setToolTipText(ab.getText());
            }
        }
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Combine two transparencies. */
    public static double combineTransparency(double t1, double t2) {
        return 1 - (1 - t1) * (1 - t2);
    }
}
