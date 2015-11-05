package qwirkle.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum QwirkleColor {
    red (225, 20, 20, "r"),
    orange(240, 130, 0, "o"),
    yorangow (255, 214, 0, "w"),
    yellow (215, 255, 0, "y"),
    green (0, 180, 70, "g"),
    blue (0, 60, 255, "b"),
    purple (140, 0, 255, "p"),
    grey (140, 140, 140, "e"),
    white (255, 255, 255, "h");

    private final int color;
    private final int r, g, b;
    private String abbrev;

    QwirkleColor(int r, int g, int b, String abbrev) {
        this.r = r; this.g = g; this.b = b;
        this.color = b + (g << 8) + (r << 16);
        this.abbrev = abbrev;
    }

    /** The RGB color 0xRRGGBB. Avoid dependency on java.awt.Color. */
    public int getColorInt() { return color; }

    public String getAbbrev() { return abbrev; }

    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }

    /** Find a shape by abbreviation. */
    public static QwirkleColor pick(String abbrev) {
        for (QwirkleColor c : values())
            if (c.getAbbrev().equals(abbrev))
                return c;
        throw new IllegalArgumentException("no match for " + abbrev);
    }

    /** Parse a sequence of single-character color abbreviations. */
    public static List<QwirkleColor> parseColors(String colors) {
        List<QwirkleColor> result = new ArrayList<>();
        for (int i = 0; i < colors.length(); ++i)
            result.add(pick(colors.substring(i, i+1)));
        return Collections.unmodifiableList(result);
    }
}
