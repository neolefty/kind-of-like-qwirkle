package qwirkle.test;

import com.google.common.eventbus.EventBus;
import qwirkle.game.base.QwirkleAI;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.QwirkleSettings;
import qwirkle.game.base.TimeLimitAI;
import qwirkle.game.control.GameController;
import qwirkle.game.control.impl.SingleThreadedStrict;
import qwirkle.game.control.players.MaxAI;
import qwirkle.game.control.players.RainbowAI;
import qwirkle.util.Stopwatch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/** Performance tests. */
public class TestPerformance {
    public static void main(String[] args) {
        // force System.err warning now, rather than mid-line
        getPref("foo", null);

        System.out.print("Testing performance: ");
        TestMain.checkAssert();

        testTimeLimit();

        Stopwatch w = new Stopwatch(true);
        List<Long> times = timeMaxPlayer(0, w);
        System.out.println(" -- Completed performance test: " + w.getTotal());
        System.out.println("    Last " + times.size() + " times: " + times);
    }

    private static final long MAX_MILLIS = 50;
    /** Test that giving a MaxAI a time limit works. */
    private static void testTimeLimit() {
        TimeLimitAI a = new MaxAI("a"), b = new RainbowAI("b");
        QwirkleSettings qs = new QwirkleSettings(a, b);
        GameController mgr = new GameController(new EventBus(), qs, new SingleThreadedStrict());
        Stopwatch w = new Stopwatch(false);

        // run once without a time limit
        mgr.start();
        int exceed = 0;
        int i = 0;
        Stopwatch untimed = new Stopwatch();
        while(!mgr.isFinished()) {
            String label = "" + ++i;
            mgr.stepAI();
            untimed.mark(label);
            if (untimed.getElapsed(label) > MAX_MILLIS)
                exceed++;
        }
        String untimedLabel = "untimed (" + mgr.getBoard().getTurnCount() + " turns)";
        w.mark(untimedLabel);
        assert exceed > 5 : "game too fast (" + exceed + "); reduce MAX_MILLIS (" + MAX_MILLIS + ")";

        // run with a time limit
        a.setMaxMillis(MAX_MILLIS); b.setMaxMillis(MAX_MILLIS);
        mgr.start();
        i = 0;
        Stopwatch timed = new Stopwatch();
        while (!mgr.isFinished()) {
            mgr.stepAI();
            String label = "" + ++i;
            timed.mark(label);
            assert timed.getElapsed(label) <= MAX_MILLIS * 5
                    : "Exceeded " + MAX_MILLIS + " ms (" + timed.getElapsed(label) + ")";
        }
        long avgTime = timed.getElapsed() / mgr.getBoard().getTurnCount();
        assert avgTime <= MAX_MILLIS : "Average time for a turn too long (" + avgTime + ")";
        String timedLabel = MAX_MILLIS + " ms (" + mgr.getBoard().getTurnCount() + " turns)";
        w.mark(timedLabel);

        assert w.getElapsed(untimedLabel) > w.getElapsed(timedLabel);

        System.out.println("Passed: Time Limit (average time " + avgTime + ", limit " + MAX_MILLIS + ")");
    }

    private static ArrayList<Long> timeMaxPlayer(int verbosity, Stopwatch w) {
        // warmup
        playMax(2, 1, 1, 0, new Stopwatch());
        w.mark("warmup");

        String key = "5 games time millis";
        ArrayList<Long> times = getPref(key, new ArrayList<Long>());
        if (verbosity >= 1)
            System.out.println("    Previous times: " + times);
        long elapsed = playMax(2, 3, 5, verbosity, w);
        times.add(elapsed);
        writePref(key, times);
        return times;
    }

    private static long playMax(int nPlayers, int nDecks, int nGames, int verbosity, Stopwatch w) {
        boolean verbose = verbosity >= 2, discrete = verbosity >= 1;
        List<QwirkleAI> players = new ArrayList<>();
        for (int i = 0; i < nPlayers; ++i)
            players.add(new MaxAI("Player " + i));
        QwirkleSettings settings = new QwirkleSettings(QwirklePlayer.wrap(players), nDecks);
        GameController mgr = new GameController(new EventBus(), settings, new SingleThreadedStrict());
        if (verbose)
            System.out.println("Playing " + nGames + " games:");
        long start = System.currentTimeMillis();
        long lap = start;
        for (int i = 0; i < nGames; ++i) {
            mgr.start();
            while (!mgr.isFinished())
                mgr.stepAI();
            w.mark(i);
            long end = System.currentTimeMillis();
            if (verbose)
                System.out.println("  " + i + " (" + (end - lap) + " ms): " + mgr.getFinishedMessageLong());
            lap = end;
        }
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        if (verbose)
            System.out.println("Elapsed time for " + nGames + " "
                    + nDecks + "-deck games with " + nPlayers + " players: " + elapsed);
        return elapsed;
    }

    private static <T extends Serializable> void writePref(String key, T value) {
        try {
            Preferences prefs = getPrefs();
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            ObjectOutputStream oOut = new ObjectOutputStream(bOut);
            oOut.writeObject(value);
            oOut.flush();
            prefs.putByteArray(key, bOut.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <T extends Serializable> T getPref(String key, T def) {
        Preferences prefs = getPrefs();
        byte[] bytes = prefs.getByteArray(key, null);
        T result = null;
        if (bytes != null && bytes.length != 0) {
            try {
                ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bIn);
                //noinspection unchecked
                result = (T) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null)
            return def;
        else
            return result;
    }

    private static Preferences getPrefs() {
        return Preferences.userNodeForPackage(TestPerformance.class);
    }
}
