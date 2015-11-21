package qwirkle.game.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum QwirkleShape {
    square("s"),
    circle("c"),
    star4("4"),
    star8("8"),
    flower("f"),
    diamond("d"),
    triangle("3"),
    smiley("m"),
    gaga("g"),
    ay("a"),
    heart("h"),
    butterfly("b"),
    star5("5"),
    pentagon("p"),
    anotherStar("n");

    static public final List<QwirkleShape> EIGHT_SHAPES
            = Collections.unmodifiableList(Arrays.asList(
            square, circle, diamond, star4, star8, heart, flower, triangle));

    static public final List<QwirkleShape> DEFAULT_SHAPES
            = Collections.unmodifiableList(Arrays.asList(
            square, circle, diamond, star4, star8, heart));

    static public final List<QwirkleShape> FIVE_SHAPES
            = Collections.unmodifiableList(Arrays.asList(
            square, circle, diamond, star4, heart));

    static public final List<QwirkleShape> FOUR_SHAPES
            = Collections.unmodifiableList(Arrays.asList(
            square, star5, triangle, circle));

    private String abbrev;
    QwirkleShape(String abbrev) {
        this.abbrev = abbrev;
    }

    public String getAbbrev() { return abbrev; }

    /** Find a shape by abbreviation. */
    public static QwirkleShape pick(String abbrev) {
        for (QwirkleShape s : values())
            if (s.getAbbrev().equals(abbrev))
                return s;
        throw new IllegalArgumentException
                ("no match for " + abbrev);
    }

    /** Parse a sequence of single-character shape abbreviations. */
    public static List<QwirkleShape> parseShapes(String shapes) {
        List<QwirkleShape> result = new ArrayList<>();
        for (int i = 0; i < shapes.length(); ++i)
            result.add(pick(shapes.substring(i, i+1)));
        return Collections.unmodifiableList(result);
    }
}
