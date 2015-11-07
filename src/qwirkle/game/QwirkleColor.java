package qwirkle.game;

import java.util.ArrayList;
import java.util.Arrays;
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
    white (255, 255, 255, "h"),

    grey1 (225, 225, 225, "1"),
    grey2 (195, 195, 195, "2"),
    grey3 (165, 165, 165, "3"),
    grey4 (135, 135, 135, "4"),
    grey5 (105, 105, 105, "5"),
    grey6 (75, 75, 75, "6"),
    grey7 (45, 45, 45, "7");

    static public final List<QwirkleColor> EIGHT_COLORS
            = Collections.unmodifiableList(Arrays.asList(
            blue, green, yellow, orange, red, purple, yorangow, grey));

    static public final List<QwirkleColor> EIGHT_GREYS
            = Collections.unmodifiableList(Arrays.asList(
            white, grey1, grey2, grey3, grey4, grey5, grey6, grey7));

    static public final List<QwirkleColor> SIX_GREYS
            = Collections.unmodifiableList(Arrays.asList(
            white, grey1, grey2, grey3, grey4, grey5));

    static public final List<QwirkleColor> DEFAULT_COLORS
            = Collections.unmodifiableList(Arrays.asList(
            blue, green, yellow, orange, red, purple));

    static public final List<QwirkleColor> FIVE_COLORS
            = Collections.unmodifiableList(Arrays.asList(
            /*blue, */green, yellow, orange, red, purple));

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
