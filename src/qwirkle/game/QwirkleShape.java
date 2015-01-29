package qwirkle.game;

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
    anotherStar("n");

    private String abbrev;
    private QwirkleShape(String abbrev) {
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
}
