package qwirkle.test;

import qwirkle.game.QwirklePlayer;
import qwirkle.players.MaxPlayer;

import java.util.ArrayList;
import java.util.List;

/** Run some games ({@link #N_GAMES}). May take a long time if it's a lot of games. */
public class PlayTest {
    public static final int N_GAMES = 100;

    public static void main(String[] args) {
//        try { Thread.sleep(10000); } catch(InterruptedException ignored) {}
        List<QwirklePlayer> players = new ArrayList<>();
        players.add(new MaxPlayer());
        players.add(new MaxPlayer());
        players.add(new MaxPlayer());
        players.add(new MaxPlayer());
//        players.add(new StupidPlayer("2"));
        PlayTester tester = new PlayTester(players);
        List<Long> times = new ArrayList<>();
        long total = 0;
        for (int i = 0; i < N_GAMES; ++i) {
            long start = System.currentTimeMillis();
            try {
                tester.play(3);
            } catch(IllegalStateException e) {
                e.printStackTrace();
            }
            long elapsed = System.currentTimeMillis() - start;
            times.add(elapsed);
            System.out.println();
            System.out.print(tester.getGame());
            int turns = tester.getGame().getBoard().getTurnCount();
            System.out.println("Elapsed time: " + elapsed + " ms (" + (elapsed / turns) + " per turn)");
            total += elapsed;
            System.out.println("Total elapsed " + times + ": ");
            System.out.println("     = " + total + " (" + total / (i+1) + " per game)");
        }
    }
}
