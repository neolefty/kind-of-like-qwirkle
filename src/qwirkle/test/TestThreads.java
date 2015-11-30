package qwirkle.test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.*;
import qwirkle.game.control.GameController;
import qwirkle.game.control.QwirkleThreads;
import qwirkle.game.control.impl.NewThreadEachTime;
import qwirkle.game.control.players.MaxAI;
import qwirkle.game.control.players.RainbowAI;
import qwirkle.game.control.players.StupidAI;
import qwirkle.game.event.GameOver;
import qwirkle.game.event.TurnCompleted;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.util.Stopwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Test QwirkleThreads, which can run a game with machine players on a clock. */
public class TestThreads {
    public static void main(String[] args) throws InterruptedException {
        System.out.print("Testing threads: ");
        TestMain.checkAssert();
        Stopwatch w = new Stopwatch(true);
        int nGames = 5;

        try {
            int printLevel = 0;

            testGamePace(nGames, 1, 1, 3, printLevel); // warmup
            w.mark("warmup");
            testPreventClobber(nGames * 2, printLevel);
            w.mark("prevent clobber");
            testFixedTime(nGames * 300, printLevel);
            w.mark("fixed time");
            testGamePace(nGames, 1, 6, 0.3, printLevel); // fast clock, slow players
            w.mark("player slow");
            testGamePace(nGames, 7, 0, 0.2, printLevel); // slow clock, fast players
            w.mark("clock slow");
            testGamePace(nGames, 5, 5, 0.3, printLevel); // slow both
            w.mark("both slow");
            System.out.print(" -- Completed");
        } finally {
            System.out.println(" thread test: " + w.getTotal());
        }
    }

    private static void testFixedTime(long duration, int printLevel) throws InterruptedException {
        final boolean verbose = printLevel >= 2, discrete = printLevel >= 1;
        Collection<QwirkleColor> colors = QwirkleColor.DEFAULT_COLORS;
        Collection<QwirkleShape> shapes = QwirkleShape.FOUR_SHAPES;
        int decks = 1;

        long delay = 0;
        List<QwirklePlayer> players = new ArrayList<>();
        players.add(new QwirklePlayer(new DelayAI(delay, new MaxAI())));
        players.add(new QwirklePlayer(new DelayAI(delay, new RainbowAI(colors))));
        players.add(new QwirklePlayer(new StupidAI()));

        QwirkleSettings settings = new QwirkleSettings(decks, shapes, colors, players);
        GameController game = new GameController(new EventBus(), settings, new NewThreadEachTime());
        QwirkleThreads threads = new QwirkleThreads(game);
        threads.setStepMillis(10);
        threads.setGameOverMillis(10);
        threads.setAutoRestart(true);
        game.getEventBus().register(new Object() {
            @Subscribe public void turn(TurnCompleted event) { if (verbose) System.out.print("."); }
            @Subscribe public void game(GameOver event) {
                if (verbose) System.out.println(event.getStatus().getFinishedLong());
            }
        });
        if (discrete)
            System.out.println("Running for " + duration + " ms");
        threads.go();
        Thread.sleep(duration);
        threads.stop();
        if (verbose)
            System.out.println();
    }

    /** Control how long the players take and how long the clock ticks are, independently.
     *  Whichever is longer should control the pace of the game (with simple players that
     *  minimize computation).
     *  @param leniency how much extra time to allow beyond minimum theoretical possible. 0.3 is good. */
    private static void testGamePace
            (final int nGames, final long clockDelay, final long playerDelay, final double leniency, int printLevel)
            throws InterruptedException
    {
        final boolean verbose = printLevel >= 2, discrete = printLevel >= 1;
        final long delay = Math.max(clockDelay, playerDelay);
        // a default game with 3 stupid players
        final List<QwirklePlayer> players = new ArrayList<>();
        for (int i = 0; i < 3; ++i)
            players.add(new QwirklePlayer(new DelayAI(playerDelay)));
        QwirkleSettings settings = new QwirkleSettings(players);
        QwirkleUIController control = new QwirkleUIController(settings, new NewThreadEachTime());
//        QwirkleUIController control = new QwirkleUIController(settings, new SingleThreadedStrict());
//        QwirkleUIController control = new QwirkleUIController(settings, new SingleThreadedForgiving());
        control.getThreads().setStepMillis(clockDelay);
        control.getThreads().setGameOverMillis(clockDelay);
        final CountDownLatch waiting = new CountDownLatch(nGames);
        final long[] start = { System.currentTimeMillis() };
        final int[] count = { 0 };
        long expect = (int) (settings.getDeckSize() * delay * (1+leniency)) * nGames;
        if (discrete)
            System.out.println("Playing " + nGames + " games with " + settings.getDeckSize()
                    + " pieces. Clock " + clockDelay + " ms; player " + playerDelay + " ms. Allowing " + expect + " ms total.");
        control.register(new Object() {
            @Subscribe public void gameOver(GameOver event) {
                int played = event.getStatus().getBoard().size();
                long expectedTime = played * delay;
                long slop = (long) (expectedTime * leniency);
                long now = System.currentTimeMillis();
                long elapsed = now - start[0];
                if (played == 0)
                    System.out.println("---> 0 played, " + elapsed + " ms elapsed <---");
                count[0]++;
                start[0] = now;
                String msg = "Game " + count[0] + "/" + nGames + ": pieces played: " + played
                        + "; elapsed: " + elapsed + " (expected " + expectedTime
                        + "; allow " + (expectedTime + slop) + ") <" + waiting.getCount() + ">";
                assert elapsed <= (expectedTime + slop) : msg;
                assert event.getStatus().isFinished() : "concurrency failure";
                if (verbose)
                    System.out.println("  > " + msg);
                waiting.countDown();
            }
        });
        control.getThreads().go();
        waiting.await(expect, TimeUnit.MILLISECONDS);
        control.getThreads().stop();
        assert count[0] == nGames  : count[0] + " games (expected " + nGames + ")";
    }

    private static class DelayAI implements QwirkleAI {
        private QwirkleAI wrapped;
        private long msDelay;

        public DelayAI(long msDelay, QwirkleAI wrapped) {
            this.msDelay = msDelay;
            this.wrapped = wrapped;
        }

        public DelayAI(long msDelay) {
            this(msDelay, new StupidAI());
        }

        @Override
        public String getName() {
            return wrapped.getName() + " + " + msDelay + " ms";
        }

        @Override
        public Collection<QwirklePlacement> play(QwirkleBoard board, List<QwirklePiece> hand) {
            long start = System.currentTimeMillis();
            Collection<QwirklePlacement> result = wrapped.play(board, hand);
            long remain = msDelay - (System.currentTimeMillis() - start);
            if (remain > 0)
                try { Thread.sleep(remain); } catch (InterruptedException ignored) { }
            return result;
        }

        @Override
        public Collection<QwirklePiece> discard(QwirkleBoard board, List<QwirklePiece> hand) {
            return wrapped.discard(board, hand);
        }

        @Override public String toString() { return getName(); }
    }

    /** Play 10 short games without errors occurring, in less than 30 seconds. */
    private static void testPreventClobber(int nGames, int printLevel) throws InterruptedException {
        final boolean verbose = printLevel >= 2, discrete = printLevel >= 1;
        final Stopwatch w = new Stopwatch();
        Collection<QwirkleColor> colors = QwirkleColor.FIVE_COLORS;
        Collection<QwirkleShape> shapes = QwirkleShape.FIVE_SHAPES;
        List<QwirklePlayer> players = new ArrayList<>();
        players.add(new QwirklePlayer(new RainbowAI("Rainbow", colors)));
        players.add(new QwirklePlayer(new MaxAI("Max")));
        QwirkleSettings settings = new QwirkleSettings(1, shapes, colors, players);
        QwirkleUIController control = new QwirkleUIController(settings, new NewThreadEachTime());
        QwirkleThreads threads = control.getThreads();
        threads.setStepMillis(10);
        threads.setGameOverMillis(10);
        threads.go();
        final CountDownLatch waiting = new CountDownLatch(nGames);
        final int[] i = { 0 };
        if (discrete)
            System.out.print("Prevent concurrent clobbering. Winners: ");
        control.register(new Object() {
//            @Subscribe
//            public void turn(TurnCompleted event) {
//                System.out.println(event);
//            }
            @Subscribe
            public void over(GameOver over) {
                if (discrete)
                    System.out.print(over.getStatus().getAnnotated().getLeader() + " ");
//                System.out.println(over.getStatus().getBoard());
                waiting.countDown();
                w.mark("game " + i[0]++);
            }
        });
        waiting.await(nGames, TimeUnit.SECONDS);
        threads.stop();
        if (verbose) {
            System.out.println();
            System.out.println("Prevent clobber: " + w);
        }
    }
}
