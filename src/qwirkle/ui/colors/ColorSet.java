package qwirkle.ui.colors;

import qwirkle.game.base.QwirkleColor;

/** A set of colors. */
public interface ColorSet {
    /** Normal color. */
    QwirkleColor getNormal();

    /** highlighted */
    QwirkleColor getHighlight();

    /** Activated color. */
    QwirkleColor getActivated();
}
