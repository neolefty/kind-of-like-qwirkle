package qwirkle.test;

import com.google.common.eventbus.EventBus;
import qwirkle.game.base.QwirkleAI;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.QwirkleSettings;
import qwirkle.game.control.GameController;
import qwirkle.game.control.impl.SingleThreadedStrict;
import qwirkle.game.control.players.MaxAI;
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
        Stopwatch w = new Stopwatch(true);

        List<Long> times = timeMaxPlayer(0, w);
        System.out.println(" -- Completed performance test: " + w.getTotal());
        System.out.println("    Last " + times.size() + " times: " + times);
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
