package qwirkle.game.base;

import qwirkle.game.control.players.MaxAI;

import java.util.*;

/** The settings to start a game of Qwirkle. Immutable. */
public class QwirkleSettings {
    static public final int DEFAULT_DECK_COUNT = 3;

    static public final List<QwirklePlayer> DEFAULT_PLAYERS
            = Collections.unmodifiableList(Arrays.asList(
                    new QwirklePlayer(new MaxAI()),
                    new QwirklePlayer(new MaxAI())));

    private final int nDecks;
    private final List<QwirkleShape> shapes;
    private final List<QwirkleColor> colors;
    private final List<QwirklePlayer> players;

    public QwirkleSettings
            (int nDecks, String shapes, String colors, Collection<QwirklePlayer> players)
    {
        this(nDecks, QwirkleShape.parseShapes(shapes), QwirkleColor.parseColors(colors), players);
    }

    public QwirkleSettings
            (int nDecks, Collection<QwirkleShape> shapes,
             Collection<QwirkleColor> colors, Collection<QwirklePlayer> players)
    {
        this.nDecks = nDecks;
        this.shapes = Collections.unmodifiableList(new ArrayList<>(shapes));
        this.colors = Collections.unmodifiableList(new ArrayList<>(colors));
        this.players = Collections.unmodifiableList(new ArrayList<>(players));
    }

    public QwirkleSettings(int deckCount) {
        this(deckCount, QwirkleShape.DEFAULT_SHAPES, QwirkleColor.DEFAULT_COLORS, DEFAULT_PLAYERS);
    }

    /** A game with all default settings. */
    public QwirkleSettings() { this(DEFAULT_DECK_COUNT); }

    public QwirkleSettings(Collection<QwirklePlayer> players) {
        this(players, DEFAULT_DECK_COUNT);
    }

    public QwirkleSettings(Collection<QwirklePlayer> players, int deckCount) {
        this(deckCount, QwirkleShape.DEFAULT_SHAPES, QwirkleColor.DEFAULT_COLORS, players);
    }

    public QwirkleSettings(QwirkleAI... ais) {
        this(convertToList(ais));
    }

    private static Collection<QwirklePlayer> convertToList(QwirkleAI[] ais) {
        List<QwirklePlayer> result = new ArrayList<>();
        for (QwirkleAI ai : ais)
            result.add(new QwirklePlayer(ai));
        return result;
    }

    /** The number of copies of all the kinds of pieces to use in this game.
     *  Default 3 -- in a default game, there are three of each possible kind of piece. */
    public int getDeckCount() { return nDecks; }

    /** What shapes should be used in this game? */
    public List<QwirkleShape> getShapes() { return shapes; }

    /** What colors should be used in this game? */
    public List<QwirkleColor> getColors() { return colors; }

    /** Who is playing? */
    public List<QwirklePlayer> getPlayers() { return players; }

    /** How many tiles should players hold at a time? Average of the number of colors & shapes, rounded up.
     *  For example, if we're playing with 7 colors and 4 shapes, hand size is 6. (7 + 4) / 2 = 5.5, round up to 6. */
    public int getHandSize() {
        int n2 = shapes.size() + colors.size();
        if (n2 %2 == 1) ++n2;
        return n2 / 2;
    }

    /** Generate a deck. */
    public List<QwirklePiece> generate() {
        List<QwirklePiece> result = new ArrayList<>();
        for (QwirkleColor color : colors)
            for (QwirkleShape shape : shapes)
                for (int i = 0; i < nDecks; ++i)
                    result.add(new QwirklePiece(color, shape));
        return result;
    }

    /** Is this the default setup with 3 decks of the 6 standard shapes and colors? */
    public boolean isDefaultSetup() {
        return getDeckCount() == DEFAULT_DECK_COUNT
                && getShapes() == QwirkleShape.DEFAULT_SHAPES
                && getColors() == QwirkleColor.DEFAULT_COLORS;
    }

    /** How many pieces are in a decks that these settings would generate? Default 108 (6 * 6 * 3). */
    public int getDeckSize() { return getDeckCount() * getColors().size() * getShapes().size(); }

    @Override
    public String toString() {
        return "Game with " + players + ": "
                + (isDefaultSetup()
                        ? "default pieces"
                        : getDeckCount() + " decks of " + getShapes().size()
                            + " shapes and " + getColors().size() + " colors");
    }
}
