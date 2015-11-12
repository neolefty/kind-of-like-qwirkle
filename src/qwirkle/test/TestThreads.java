package qwirkle.test;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameController;
import qwirkle.control.QwirkleThreads;
import qwirkle.control.impl.NewThreadEachTime;
import qwirkle.event.GameOver;
import qwirkle.game.*;
import qwirkle.game.impl.AsyncPlayerWrapper;
import qwirkle.players.MaxPlayer;
import qwirkle.players.RainbowPlayer;
import qwirkle.players.StupidPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Test QwirkleThreads, which can run a game with machine players on a clock. */
public class TestThreads {
    public static void main(String[] args) throws InterruptedException {
        TestMain.checkAssert();
        Stopwatch w = new Stopwatch();
        int nGames = 8;
        testPreventClobber(nGames);
        w.mark("prevent clobber");
        testGamePace(nGames, 7, 0); // slow clock, fast players
        w.mark("clock limited");
        testGamePace(nGames, 1, 6); // fast clock, slow players
        w.mark("player limited");
        testGamePace(nGames, 5, 5); // slow both
        w.mark("both slow");
        System.out.println("Thread testing complete: " + w);
    }

    /** Control how long the players take and how long the clock ticks are, independently.
     *  Whichever is longer should control the pace of the game (with simple players that minimize computation). */
    private static void testGamePace(final int nGames, final long clockDelay, final long playerDelay)
            throws InterruptedException
    {
        final long delay = Math.max(clockDelay, playerDelay);
        // a default game with 3 stupid players
        final List<AsyncPlayer> players = new ArrayList<>();
        for (int i = 0; i < 3; ++i)
            players.add(new AsyncPlayerWrapper(new DelayPlayer(playerDelay)));
        QwirkleSettings settings = new QwirkleSettings(players);
        GameController control = new GameController(settings, new NewThreadEachTime());
        control.getThreads().setStepMillis(clockDelay);
        control.getThreads().setGameOverMillis(clockDelay);
        final CountDownLatch waiting = new CountDownLatch(nGames);
        final long[] start = { System.currentTimeMillis() };
        final int[] count = { 0 };
        System.out.println("Playing " + nGames + " with " + settings.getDeckSize()
                + " pieces. Clock " + clockDelay + " ms; player " + playerDelay + " ms.");
        control.register(new Object() {
            @Subscribe public void gameOver(GameOver event) {
                int played = event.getStatus().getBoard().size();
                long expectedTime = played * delay;
                long slop = expectedTime / 7;
                long now = System.currentTimeMillis();
                long elapsed = now - start[0];
                count[0]++;
                start[0] = now;
                String msg = "  > Game " + count[0] + "/" + nGames + ": pieces played: " + played
                        + "; elapsed: " + elapsed + " (expected " + expectedTime
                        + "; allow " + (expectedTime + slop) + ")";
                assert elapsed < (expectedTime + slop);
                assert event.getStatus().isFinished() : "concurrency failure";
                System.out.println(msg);
                waiting.countDown();
            }
        });
        control.getThreads().go();
        int expect = (int) (settings.getDeckSize() * delay * 1.5) * nGames;
        waiting.await(expect, TimeUnit.MILLISECONDS);
        control.getThreads().stop();
        assert count[0] == nGames  : count[0] + " games (expected " + nGames + ")";
    }

    private static class DelayPlayer extends StupidPlayer {
        private static int serial = 1;
        private long msDelay;
        public DelayPlayer(long msDelay) {
            super("" + serial++);
            this.msDelay = msDelay;
        }

        @Override
        public List<QwirklePlacement> play(QwirkleBoard board, List<QwirklePiece> hand) {
            long start = System.currentTimeMillis();
            List<QwirklePlacement> result = super.play(board, hand);
            long remain = msDelay - (System.currentTimeMillis() - start);
            if (remain > 0)
                try { Thread.sleep(remain); } catch (InterruptedException ignored) { }
            return result;
        }
    }

    /** Play 10 short games without errors occurring, in less than 30 seconds. */
    private static void testPreventClobber(int nGames) throws InterruptedException {
        final Stopwatch w = new Stopwatch();
        Collection<QwirkleColor> colors = QwirkleColor.FIVE_COLORS;
        Collection<QwirkleShape> shapes = QwirkleShape.FIVE_SHAPES;
        List<AsyncPlayer> players = new ArrayList<>();
        players.add(new AsyncPlayerWrapper(new RainbowPlayer("Rainbow", colors)));
        players.add(new AsyncPlayerWrapper(new MaxPlayer("Max")));
        QwirkleSettings settings = new QwirkleSettings(1, shapes, colors, players);
        GameController control = new GameController(settings, new NewThreadEachTime());
        QwirkleThreads threads = control.getThreads();
        threads.setStepMillis(10);
        threads.setGameOverMillis(10);
        threads.go();
        final CountDownLatch waiting = new CountDownLatch(nGames);
        final int[] i = { 0 };
        System.out.print("Prevent concurrent clobbering. Winners: ");
        control.register(new Object() {
//            @Subscribe
//            public void turn(TurnCompleted event) {
//                System.out.println(event);
//            }
            @Subscribe
            public void over(GameOver over) {
                System.out.print(over.getStatus().getLeader() + " ");
//                System.out.println(over.getStatus().getBoard());
                waiting.countDown();
                w.mark("game " + i[0]++);
            }
        });
        waiting.await(nGames, TimeUnit.SECONDS);
        threads.stop();
        System.out.println();
        System.out.println("Prevent clobber: " + w);
    }
}
