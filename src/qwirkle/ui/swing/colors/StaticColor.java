package qwirkle.ui.swing.colors;

import java.awt.*;

/** A simple static color. */
public class StaticColor implements ColorSource {
    private Color color;

    public StaticColor(Color color) { this.color = color; }

    @Override public Color getColor() { return color; }
}
