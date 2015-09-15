package qwirkle.ui.paint.colors;

import java.awt.*;

/** A set of fixed, static colors. */
public class StaticColorSet implements ColorSet {
    private Color normal, highlight, activated;

    public StaticColorSet(Color normal, Color highlight, Color activated) {
        this.normal = normal;
        this.highlight = highlight;
        this.activated = activated;
    }

    @Override public Color getHighlight() { return highlight; }
    @Override public Color getNormal() { return normal; }
    @Override public Color getActivated() { return activated; }
}
