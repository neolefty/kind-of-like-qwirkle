package qwirkle.ui.colors;

import qwirkle.game.base.QwirkleColor;

/** A set of fixed, static colors. */
public class StaticColorSet implements ColorSet {
    private QwirkleColor normal, highlight, activated;

    public StaticColorSet(QwirkleColor normal, QwirkleColor highlight, QwirkleColor activated) {
        this.normal = normal;
        this.highlight = highlight;
        this.activated = activated;
    }

    @Override public QwirkleColor getHighlight() { return highlight; }
    @Override public QwirkleColor getNormal() { return normal; }
    @Override public QwirkleColor getActivated() { return activated; }
}
