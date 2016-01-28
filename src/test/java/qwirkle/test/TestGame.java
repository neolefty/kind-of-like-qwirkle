package qwirkle.test;

import qwirkle.game.base.QwirkleAI;
import qwirkle.game.control.players.MaxAI;
import qwirkle.test.support.PlayTester;
import qwirkle.util.Stopwatch;

import java.util.ArrayList;
import java.util.List;

/** Run some games ({@link #N_GAMES}). May take a long time if it's a lot of games. */
public class TestGame {
    public static final int N_GAMES = 10;

    public static void main(String[] args) {
        System.out.print("Testing game: ");
        TestMain.checkAssert();
        Stopwatch w = new Stopwatch(true);

        for (int i = 0; i < N_GAMES; ++i) {
            testPlay(1, 0);
            w.mark(i);
        }
        System.out.println(" -- Completed game test: " + w.getTotal() + " (average " + (long) w.getAverage() + ")");
    }

    private static void testPlay(int n, int printLevel) {
        boolean verbose = printLevel >= 2, discrete = printLevel >= 1;
        List<QwirkleAI> players = new ArrayList<>();
        players.add(new MaxAI());
        players.add(new MaxAI());
        players.add(new MaxAI());
        players.add(new MaxAI());
//        players.add(new StupidPlayer("2"));
        PlayTester tester = new PlayTester(players);
        List<Long> times = new ArrayList<>();
        long total = 0;
        for (int i = 0; i < n; ++i) {
            long start = System.currentTimeMillis();
            try {
                tester.play(3);
            } catch(IllegalStateException e) {
                e.printStackTrace();
            }
            long elapsed = System.currentTimeMillis() - start;
            times.add(elapsed);
            if (verbose) {
                System.out.println();
                System.out.print(tester.getGame());
            }
            int turns = tester.getGame().getBoard().getTurnCount();
            if (discrete)
                System.out.println("Elapsed time: " + elapsed + " ms (" + (elapsed / turns) + " per turn)");
            total += elapsed;
            if (verbose) {
                System.out.println("Total elapsed " + times + ": ");
                System.out.println("     = " + total + " (" + total / (i + 1) + " per game)");
            }
        }
    }
}
