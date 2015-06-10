package qwirkle.ui.swing;

import javax.swing.*;
import java.awt.*;

/** A label that adjusts its size based on its parent's width or height. */
public class AutoSizeLabel extends JLabel {
    FontAutosizer sizer;

    public AutoSizeLabel(Component parent, String text, double fraction) {
        this(parent);
        setText(text);
        sizer.setFractionOfMetric(fraction);
    }

    public AutoSizeLabel(Component parent) {
        sizer = new FontAutosizer(this, parent);
    }

    public void setMetric(FontAutosizer.Metric metric) {
        sizer.setMetric(metric);
    }
}
