package qwirkle.test;

import qwirkle.game.*;
import qwirkle.game.impl.QwirkleBoardImpl;
import qwirkle.players.Rainbow;
import qwirkle.players.RainbowPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Test {@link RainbowPlayer} */
public class TestRainbow {
    public static void main(String[] args) {
        TestMain.checkAssert();
        testRainbow();
        testDegenerateColors();
        testMinibow();
    }

    // TODO test with smaller numbers of colors, including 1-3
    private static void testRainbow() {
        QwirkleSettings settings = new QwirkleSettings();
        Rainbow r = new Rainbow(settings.getColors());
        r.setDislikeRainbow(0);
        r.setDislikeJumps(10);
        r.setDislikeMonochrome(3);
        QwirkleBoard board = new QwirkleBoardImpl(settings);
        board = board.play(Arrays.asList(new QwirklePlacement("rh", 0, 0), new QwirklePlacement("ph", 1, 0),
                new QwirklePlacement("bh", 2, 0)));
        assert r.computeRainbowDeviation(board) == 0 : r.computeRainbowDeviation(board);
        board = board.play(Arrays.asList(new QwirklePlacement("rd", 0, 1), new QwirklePlacement("rs", 0, 2)));
        assert r.computeRainbowDeviation(board) == 6 : r.computeRainbowDeviation(board);
        board = board.play(Arrays.asList(new QwirklePlacement("os", 1, 2), new QwirklePlacement("ys", 2, 2)));
        assert r.computeRainbowDeviation(board) == 6 : r.computeRainbowDeviation(board);
        board = board.play(Arrays.asList(new QwirklePlacement("gs", 2, 3), new QwirklePlacement("ys", 1, 3)));
        assert r.computeRainbowDeviation(board) == 6 : r.computeRainbowDeviation(board);
        board = board.play(Collections.singletonList(new QwirklePlacement("ps", 2, 4)));
        assert r.computeRainbowDeviation(board) == 26 : r.computeRainbowDeviation(board);
        board = board.play(Collections.singletonList(new QwirklePlacement("bs", 2, 5)));
        assert r.computeRainbowDeviation(board) == 26 : r.computeRainbowDeviation(board);
        r.setDislikeRainbow(1);
        System.out.println(board);
        assert r.computeRainbowDeviation(board) == 34 : r.computeRainbowDeviation(board);
    }

    private static void testMinibow() {
        RainbowPlayer player = new RainbowPlayer(QwirkleColor.DEFAULT_COLORS);
        QwirkleSettings settings = new QwirkleSettings();
        QwirkleBoard board = new QwirkleBoardImpl(settings);
        // hand: three in a mini-rainbow
        List<QwirklePiece> miniBow = Arrays.asList(
//                new QwirklePiece("bf"),
                new QwirklePiece("pf"),
                new QwirklePiece("rf"),
                new QwirklePiece("of")
        );
        List<QwirklePiece> hand = new ArrayList<>();
        hand.addAll(miniBow);
        // plus one that's not quite in the rainbow
        hand.add(new QwirklePiece("gf"));
        // and three others that make a separate, same-size monochrome strip
        hand.add(new QwirklePiece("gd"));
        hand.add(new QwirklePiece("gs"));
//        hand.add(new QwirklePiece("gc"));
        hand.add(new QwirklePiece("bh"));
        hand.add(new QwirklePiece("yh"));

        // yeah, the hand is illegally large, but it's a better test than just 6 cards, and we need to get all the colors in
        // I suppose we could use the 8-color & 8-shape sets
        board = board.play(player.play(board, hand));
        assert board.size() == 3 : board;
        List<QwirklePiece> played = new ArrayList<>();
        for (QwirklePlacement place : board.getPlacements())
            played.add(place.getPiece());

        assert played.containsAll(miniBow) : played;
        miniBow = new ArrayList<>(miniBow);
        List<QwirklePiece> miniBack = new ArrayList<>(miniBow);
        Collections.reverse(miniBack);
        assert played.equals(miniBack) || played.equals(miniBow) : "Not in rainbow order: " + played;
    }

    private static void testDegenerateColors() {
        RainbowPlayer player = new RainbowPlayer(QwirkleColor.DEFAULT_COLORS);
        QwirkleSettings settings = new QwirkleSettings();
        QwirkleBoard board = new QwirkleBoardImpl(settings);
        QwirklePiece rf = new QwirklePiece("rf"); // red flower
        List<QwirklePiece> hand = Arrays.asList(rf, rf, rf, rf, rf, rf);
        board = board.play(player.play(board, hand));
        assert board.size() == 1;
        assert board.getPlacements().iterator().next().getPiece().equals(rf);
    }
}
