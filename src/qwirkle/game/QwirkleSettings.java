package qwirkle.game;

import qwirkle.players.AsyncPlayerWrapper;
import qwirkle.players.MaxPlayer;

import java.util.*;

/** The settings to start a game of Qwirkle. Immutable. */
public class QwirkleSettings {
    static public final int DEFAULT_DECK_COUNT = 3;

    static public final List<QwirkleShape> DEFAULT_SHAPES
            = Collections.unmodifiableList(Arrays.asList(
            QwirkleShape.square, QwirkleShape.circle, QwirkleShape.diamond,
            QwirkleShape.star4, QwirkleShape.star8, QwirkleShape.heart));

    static public final List<QwirkleColor> DEFAULT_COLORS
            = Collections.unmodifiableList(Arrays.asList(
            QwirkleColor.blue, QwirkleColor.green, QwirkleColor.yellow,
            QwirkleColor.orange, QwirkleColor.red, QwirkleColor.purple));

    static public final List<AsyncPlayer> DEFAULT_PLAYERS
            = Collections.unmodifiableList(AsyncPlayerWrapper.wrap(Arrays.asList(
            (QwirklePlayer) new MaxPlayer(), new MaxPlayer())));

    private final int nDecks;
    private final List<QwirkleShape> shapes;
    private final List<QwirkleColor> colors;
    private final List<AsyncPlayer> players;

    public QwirkleSettings
            (int nDecks, String shapes, String colors, Collection<AsyncPlayer> players)
    {
        this(nDecks, QwirkleShape.parseShapes(shapes), QwirkleColor.parseColors(colors), players);
    }

    public QwirkleSettings
            (int nDecks, Collection<QwirkleShape> shapes,
             Collection<QwirkleColor> colors, Collection<AsyncPlayer> players)
    {
        this.nDecks = nDecks;
        this.shapes = Collections.unmodifiableList(new ArrayList<>(shapes));
        this.colors = Collections.unmodifiableList(new ArrayList<>(colors));
        this.players = Collections.unmodifiableList(new ArrayList<>(players));
    }

    public QwirkleSettings(int deckCount) {
        this(deckCount, DEFAULT_SHAPES, DEFAULT_COLORS, DEFAULT_PLAYERS);
    }

    /** A game with all default settings. */
    public QwirkleSettings() { this(DEFAULT_DECK_COUNT); }

    public QwirkleSettings(Collection<AsyncPlayer> players) {
        this(players, DEFAULT_DECK_COUNT);
    }

    public QwirkleSettings(Collection<AsyncPlayer> players, int deckCount) {
        this(deckCount, DEFAULT_SHAPES, DEFAULT_COLORS, players);
    }

    /** The number of copies of all the kinds of pieces to use in this game.
     *  Default 3 -- in a default game, there are three of each possible kind of piece. */
    public int getDeckCount() { return nDecks; }

    /** What shapes should be used in this game? */
    public List<QwirkleShape> getShapes() { return shapes; }

    /** What colors should be used in this game? */
    public List<QwirkleColor> getColors() { return colors; }

    /** Who is playing? */
    public List<AsyncPlayer> getPlayers() { return players; }

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
}
