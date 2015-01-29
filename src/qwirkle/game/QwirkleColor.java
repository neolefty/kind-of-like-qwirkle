package qwirkle.game;

import java.awt.Color;

public enum QwirkleColor {
    red (new Color(225, 20, 20), "r"),
    orange(new Color(240, 130, 0), "o"),
    yorangow (new Color(255, 214, 0), "w"),
    yellow (new Color(215, 255, 0), "y"),
    green (new Color(0, 180, 70), "g"),
    blue (new Color(0, 60, 255), "b"),
    purple (new Color(140, 0, 255), "p"),
    grey (new Color(140, 140, 140), "e"),
    white (new Color(255, 255, 255), "h");

    private final Color color;
    private String abbrev;

    private QwirkleColor(Color color, String abbrev) {
        this.color = color;
        this.abbrev = abbrev;
    }
    public Color getColor() { return color; }

    public String getAbbrev() { return abbrev; }

    /** Find a shape by abbreviation. */
    public static QwirkleColor pick(String abbrev) {
        for (QwirkleColor c : values())
            if (c.getAbbrev().equals(abbrev))
                return c;
        throw new IllegalArgumentException("no match for " + abbrev);
    }
}
