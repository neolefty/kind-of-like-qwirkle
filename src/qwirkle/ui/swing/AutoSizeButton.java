package qwirkle.ui.swing;

import javax.swing.*;
import java.awt.*;

/** A JButton that automatically adjusts its font size based on its parent's width or height. */
public class AutoSizeButton extends JButton {
    FontAutosizer sizer;

    public AutoSizeButton(Component parent, String text, double fraction) {
        super(text);
        sizer = new FontAutosizer(this, parent, fraction);
    }
}
