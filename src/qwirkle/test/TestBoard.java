package qwirkle.test;

import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.*;
import qwirkle.game.base.impl.QwirkleBoardImpl;
import qwirkle.game.control.GameController;
import qwirkle.game.control.impl.SingleThreadedStrict;
import qwirkle.game.control.players.MaxAI;
import qwirkle.game.control.players.StupidAI;
import qwirkle.game.event.TurnStarting;
import qwirkle.ui.control.QwirkleUIController;

import java.util.*;

// TODO deal with the situation where no move whatsoever is possible -- a perfect 6 x 6 square, for example?
/** Basic tests of the Qwirkle game board. */
public class TestBoard {
    private static final Random r = new Random();

    public static void main(String[] args) {
        TestMain.checkAssert();

        TestBoard.testLogic();
        TestBoard.testInit();
        TestBoard.testLegal();

        int i = 0;
        while (i < 100) // succeed 100 times
            if (TestBoard.testIndividual(1, false))
                ++i;

        TestBoard.testGroup();
    }

    /** Test some miscellaneous logical things. */
    private static void testLogic() {
        testCountMatches();
        System.out.println("Logic passes");
    }

    /** Test counting the number of matching pieces a player has. Used to determine who goes first. */
    private static void testCountMatches() {
        // bd and gd match; ys doesn't
        List<QwirklePiece> a = Arrays.asList(new QwirklePiece("bd"), new QwirklePiece("bd"),
                new QwirklePiece("gd"), new QwirklePiece("ys"));
        assert QwirkleKit.countMatches(a) == 2;
        // p4, y4, and g4 match; y4 and ys match
        List<QwirklePiece> b = Arrays.asList(new QwirklePiece("p4"), new QwirklePiece("p4"),
                new QwirklePiece("ys"), new QwirklePiece("ys"), new QwirklePiece("ys"),
                new QwirklePiece("y4"), new QwirklePiece("g4"));
        assert QwirkleKit.countMatches(b) == 3;
    }

    /** Test game initialization (pieces, colors, players, etc) */
    private static void testInit() {
        int trials = 50; // run N times to make sure nothing random is going wrong
        QwirkleShape[] shapes = {QwirkleShape.heart, QwirkleShape.star8, QwirkleShape.diamond};
        QwirkleColor[] colors = {QwirkleColor.blue, QwirkleColor.purple};
        QwirkleAI[] rawPlayers = { new MaxAI(), new StupidAI("duh") };
        List<QwirklePlayer> players = QwirklePlayer.wrap(Arrays.asList(rawPlayers));

        // a deck should include one of each card
        for (int i = 0; i < trials; ++i) {
            QwirkleSettings settings = new QwirkleSettings(1, Arrays.asList(shapes),
                    Arrays.asList(colors), players);
            assert settings.getHandSize() == 3;
            List<QwirklePiece> deck = settings.generate();
            assert deck.size() == shapes.length * colors.length;
            for (QwirkleShape s : shapes)
                for (QwirkleColor c : colors)
                    assert deck.contains(new QwirklePiece(c, s));
            assert !deck.contains(new QwirklePiece(QwirkleColor.blue, QwirkleShape.butterfly));
            assert !deck.contains(new QwirklePiece(QwirkleColor.orange, QwirkleShape.heart));
        }

        // make sure a multi-deck deal works
        int nDecks = 5;
        QwirkleSettings settings = new QwirkleSettings(nDecks, Arrays.asList(shapes),
                QwirkleColor.DEFAULT_COLORS, players);
        List<QwirklePiece> deck = settings.generate();
        assert deck.size() == QwirkleColor.DEFAULT_COLORS.size() * shapes.length * nDecks;
        QwirklePiece piece = new QwirklePiece(QwirkleColor.DEFAULT_COLORS.get(0), shapes[0]);
        int countPiece = 0;
        // count how many times a unique piece shows up in the deck. Should == nDecks.
        for (QwirklePiece p : deck)
            if (p.equals(piece))
                ++countPiece;
        assert countPiece == nDecks;

        // try a few times to make sure game works every time
        for (int n = 0; n < trials; ++n) {
            // play a few rounds of a game
            QwirkleUIController control = new QwirkleUIController(settings, new SingleThreadedStrict());
            final boolean[] boardChanged = { false }, turnStarted = { false };
            control.register(new Object() {
                @Subscribe
                public void board(QwirkleBoard board) {
                    boardChanged[0] = true;
                }

                @Subscribe
                public void status(TurnStarting turn) {
                    turnStarted[0] = true;
                }
            });
            assert !boardChanged[0];
            assert !turnStarted[0];
            control.getGame().start();
            // advance to player #0
            GameController game = control.getGame();
            if (game.getCurrentPlayer() == players.get(1)) {
                game.stepAI();
//                System.out.println(mgr);
            }
            // ensure that we're alternating between players
            //noinspection AssertWithSideEffects
            assert game.getCurrentPlayer() == players.get(0);
            game.stepAI();
//            System.out.println(mgr);
            assert boardChanged[0];
            assert turnStarted[0];
            //noinspection AssertWithSideEffects
            assert game.getCurrentPlayer() == players.get(1);
            game.stepAI();
//            System.out.println(mgr);
            //noinspection AssertWithSideEffects
            assert game.getCurrentPlayer() == players.get(0);
        }
    }

    /** Test playing groups of tiles. */
    private static QwirkleBoard testGroup() {
        QwirkleBoard board = new QwirkleBoardImpl(new QwirkleSettings());

        // a row across the middle
        List<QwirklePlacement> first = new ArrayList<>();
        first.add(new QwirklePlacement("os", 0, 0));
        first.add(new QwirklePlacement("oc", 1, 0));
        first.add(new QwirklePlacement("o4", -1, 0));
        first.add(new QwirklePlacement("o4", 2, 1));
        assert !board.isLegal(first);
        first.remove(3);
        board = board.play(first);
        System.out.println(board);

        // a column crossing the middle
        List<QwirklePlacement> second = new ArrayList<>();
        second.add(new QwirklePlacement("o8", 0, 1));
        second.add(new QwirklePlacement("of", 0, -1));
        board = board.play(second);
        System.out.println(board);

        // a piece at the right end of the first play
        List<QwirklePlacement> third = new ArrayList<>();
        third.add(new QwirklePlacement("od", 2, 0));
        board = board.play(third);
        System.out.println(board);

        // now the second color
        List<QwirklePlacement> fourth = new ArrayList<>();
        fourth.add(new QwirklePlacement("rd", 2, 2));
        fourth.add(new QwirklePlacement("pd", 2, 1));
        board = board.play(fourth);
        System.out.println(board);

        QwirkleBoard afterFourth = board;
        QwirklePlacement qp = new QwirklePlacement("od", 0, 2);
        List<QwirklePlacement> qpl = new ArrayList<>();
        qpl.add(qp);
        board = board.play(qp);
        System.out.println(board);
        TestMain.checkContentsMatch(qpl, board.getLastPlay());
        assert afterFourth == board.getUndo();

        // now a crossing play that completes colors
        List<QwirklePlacement> sixth = new ArrayList<>();
        sixth.add(new QwirklePlacement("gd", 1, 2));
        sixth.add(new QwirklePlacement("pd", 3, 2));
        sixth.add(new QwirklePlacement("bd", -1, 2));
        sixth.add(new QwirklePlacement("yd", -2, 2));

        assert board.isLegal(sixth);
        // illegal: can't add a color to a row twice
        sixth.add(new QwirklePlacement("od", -3, 2));
        assert !board.isLegal(sixth);
        // illegal: not in line with the rest
        QwirklePlacement outOfLine = new QwirklePlacement("gd", 2, -1);
        assert board.isLegal(outOfLine); // on its own it's fine
        sixth.set(4, outOfLine);
        assert !board.isLegal(sixth);
        // illegal: already occupied
        QwirklePlacement occludes = new QwirklePlacement("rd", 2, 2);
        assert !board.isLegal(occludes);
        sixth.set(4, occludes);
        assert !board.isLegal(sixth);
        sixth.remove(4);
        board = board.play(sixth);
        System.out.println(board);

        // illegal: gap

        return board;
    }

    /** Test a tricky legality situation. */
    private static void testLegal() {
        List<QwirklePlacement> play;
        QwirkleTestBoard board;

        // joining lines
        QwirkleSettings settings = new QwirkleSettings();
        QwirkleLine line1 = new QwirkleLine(new QwirklePlacement("yf", 0, 0), settings)
                .augment(new QwirklePlacement("of", 1, 0));
        QwirkleLine line2 = new QwirkleLine(new QwirklePlacement("bf", 3, 0), settings);
        QwirklePlacement join = new QwirklePlacement("gf", 2, 0);
        assert line1.canJoinWith(line2, join);
        assert line2.canJoinWith(line1, join);
        // but can't join if there is a duplicate piece
        QwirkleLine longer = line2.augment(new QwirklePlacement("yf", 4, 0));
        assert !line1.canJoinWith(longer, join);

        // matches two lines individually, but joins them illegally
        board = new QwirkleTestBoard()
                .play("oc", 0, 0).play("rc", 0, 1).play("pc", 0, 2)
                .play("ps", 1, 2)
                .play("pd", 2, 2).play("rd", 2, 1).play("gd", 2, 0);
        System.out.println(board);
        assert !board.isLegal(new QwirklePlacement("od", 1, 0));
        System.out.println();

        // dunno what was wrong here, but it's fixed now
        board = new QwirkleTestBoard()
                .play("rf", 2, 3).play("yf", 2, 4).play("pf", 2, 5)
                .play("y8", 1, 4).play("p8", 1, 5)
                .play("rd", 3, 3).play("yd", 3, 4).play("pd", 3, 5);
        // make sure you can play null and have no effect
        QwirkleBoard afterNull = board.play((Collection<QwirklePlacement>) null)
                .play(new ArrayList<QwirklePlacement>());
        assert afterNull == board;
        try {
            board.play((QwirklePlacement) null);
            assert false : "Should have throw NPE.";
        } catch(NullPointerException ignored) {}
        play = Arrays.asList(new QwirklePlacement("rs", 4, 3), new QwirklePlacement("ps", 4, 5),
                new QwirklePlacement("ys", 4, 4), new QwirklePlacement("os", 4, 6));
        System.out.println(board);
        assert board.isLegal(play);
        System.out.println(board.play(play));
        System.out.println();

        // a divided play that should be legal
        board = new QwirkleTestBoard()
                .play("y4", 2, -7).play("o4", 3, -7).play("g4", 4, -7).play("gf", 4, -8)
                .play("gd", 4, -6).play("od", 3, -6).play("rd", 5, -6);
        play = Arrays.asList(new QwirklePlacement("yf", 2, -8),
                new QwirklePlacement("of", 3, -8), new QwirklePlacement("pf", 5, -8));
        System.out.println(board);
        assert board.isLegal(play);

        // a joining play that should be legal
        QwirkleBoard nextBoard = board.play(play.get(0)).play(play.get(2));
        assert nextBoard.isLegal(play.get(1));

        System.out.println(board.play(play));
    }

    /** Test playing individual tiles.
     *  @return true if successfully plays all pieces,
     *      false if unable to play the last 1 or more pieces. */
    private static boolean testIndividual(int n, boolean print) {
        QwirkleBoard board = new QwirkleTestBoard();
        List<QwirklePiece> pieces = new QwirkleSettings(n).generate();
        int initialSize = pieces.size();

        int noLegal = 0; // total times we couldn't find a legal move in a game
        int noLegalInaRow = 0;
        while (!pieces.isEmpty()) {
            QwirklePiece piece = pieces.remove(r.nextInt(pieces.size()));
            Collection<QwirklePlacement> moves = board.getLegalPlacements(piece);
            if (moves.isEmpty()) { // couldn't find a legal move
                pieces.add(piece);
                ++noLegalInaRow;
                if (noLegalInaRow > pieces.size()) { // couldn't find a legal spot for any piece
                    System.out.println("Failed to finish game -- no legal moves remain.");
                    System.out.println(board);
                    System.out.println("Remaining pieces: " + pieces);
                    return false; // failed to finish game
                }
            }
            else {
                noLegalInaRow = 0;
                List<QwirklePlacement> movesList = new ArrayList<>(moves);
                board = board.play(movesList.get(r.nextInt(movesList.size())));
                assert pieces.size() + board.size() == initialSize;

                if (print) {
                    System.out.print(board);
                    System.out.println("Remaining (" + pieces.size() + "): " + QwirklePiece.abbrev(pieces));
                }
                QwirkleLine longest = board.getLines().iterator().next();
                for (QwirkleLine line : board.getLines())
                    if (line.size() > longest.size())
                        longest = line;
                if (print) {
                    System.out.println("Longest line (" + longest.size() + "): " + longest);
                    System.out.println();
                }
            }
            noLegal += noLegalInaRow;
        }
        System.out.println("Couldn't play chosen piece " + noLegal + " times");
        return true;
    }
}
