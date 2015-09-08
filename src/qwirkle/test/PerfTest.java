package qwirkle.test;

import qwirkle.control.GameManager;
import qwirkle.control.SingleThreaded;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleSettings;
import qwirkle.players.AsyncPlayerWrapper;
import qwirkle.players.MaxPlayer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/** Performance tests. */
public class PerfTest {
    public static void main(String[] args) {
        TestQwirkle.checkAssert();
        timeMaxPlayer();
    }

    private static void timeMaxPlayer() {
        // warmup
        playMax(2, 1, 1);

        String key = "5 games time millis";
        ArrayList<Long> times = getPref(key, new ArrayList<Long>());
        System.out.println("Previous times: " + times);
        long elapsed = playMax(2, 3, 5);
        times.add(elapsed);
        writePref(key, times);
    }

    private static long playMax(int nPlayers, int nDecks, int nGames) {
        List<QwirklePlayer> players = new ArrayList<>();
        for (int i = 0; i < nPlayers; ++i)
            players.add(new MaxPlayer("Player " + i));
        QwirkleSettings settings = new QwirkleSettings(AsyncPlayerWrapper.wrap(players), nDecks);
        GameManager mgr = new GameManager(settings, new SingleThreaded());
        System.out.println("Playing " + nGames + " games:");
        long start = System.currentTimeMillis();
        long lap = start;
        for (int i = 0; i < nGames; ++i) {
            mgr.start();
            while (!mgr.isFinished())
                mgr.step();
            long end = System.currentTimeMillis();
            System.out.println("  " + i + " (" + (end - lap) + " ms): " + mgr.getFinishedMessage());
            lap = end;
        }
        long end = System.currentTimeMillis();
        long elapsed = end - start;
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
        return Preferences.userNodeForPackage(PerfTest.class);
    }
}
