package qwirkle.ui.paint.colors;

import java.awt.*;

/** A set of colors. */
public interface ColorSet {
    /** Normal color. */
    Color getNormal();

    /** highlighted */
    Color getHighlight();

    /** Activated color. */
    Color getActivated();
}
