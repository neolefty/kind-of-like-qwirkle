package qwirkle.test;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.control.AnnotatedGame;
import qwirkle.event.GameOver;
import qwirkle.event.GameStarted;
import qwirkle.event.QwirkleTurn;
import qwirkle.game.*;
import qwirkle.game.impl.AsyncPlayerWrapper;
import qwirkle.test.scripted.ScriptedGameModel;
import qwirkle.test.scripted.ScriptedPlayer;
import qwirkle.test.scripted.ScriptedSettings;

import java.util.Arrays;
import java.util.List;

/** Use a scripted game to test events, scoring, GameStatus, and AnnotatedGame. */
public class TestScripted {
    public static void main(String[] args) {
        TestMain.checkAssert();
        testScoring(false);
        testAnnotatedGame(false);
    }

    private static void testAnnotatedGame(boolean printDebug) {
        // turn 0
        String h1 = "pc,0,0,ph,0,1,pd,0,2,p4,0,3;"; // p1 plays 4
        // turn 1
        String h2 = "oc,1,0,rc,2,0,gc,-1,0;"; // p2 plays 3
        // turn 2
        h1 += "oh,1,1,od,1,2;"; // p1 plays 2
        // turn 3
        h2 += "d:os,rs,gs;"; // p2 discards 2
        // turn 4
        h1 += "d:p4,p8;"; // p1 discards 2
        // turn 5
        h2 += "rh,2,1,rd,2,2;"; // p2 plays 2
        // turn 6 -- p1 MaxPlayer plays 2 to complete a set

        AsyncPlayer p1 = new AsyncPlayerWrapper(new ScriptedPlayer("p1", h1));
        AsyncPlayer p2 = new AsyncPlayerWrapper(new ScriptedPlayer("p2", h2));
        List<AsyncPlayer> players = Arrays.asList(p1, p2);
        ScriptedSettings settings = new ScriptedSettings(players);
        // add two pieces to test that MaxPlayer knows what to do
        settings.setDeckPrefix("bc,yc");
        EventBus bus = new EventBus();
        final ScriptedGameModel mgr = new ScriptedGameModel(bus);

        // listen for the game to start, end, and each turn to pass
        final int[] receivedGameStarted = { 0 };
        final AnnotatedGame[] firstAnnotated = { null };
        final int[] receivedGameOver = { 0 };
        final int[] turnCount = {0};
        bus.register(new Object() {
            @Subscribe
            public void gameStarted(GameStarted started) {
                receivedGameStarted[0]++;
                if (firstAnnotated[0] == null) // only grab it the first time
                    firstAnnotated[0] = started.getStatus().getAnnotatedGame();
            }

            @Subscribe
            public void gameOver(GameOver over) {
                receivedGameOver[0]++;
            }

            @Subscribe
            public void turnPassed(QwirkleTurn turn) {
                ++turnCount[0];
            }
        });

        assert firstAnnotated[0] == null;
        mgr.start(settings);
        assert receivedGameStarted[0] == 1;
        assert firstAnnotated[0] != null;

        // turn 0
        mgr.step(); // p1 plays 4 for 4 points
        assert turnCount[0] == 1;
        if (printDebug) debugPrint(mgr);
        AnnotatedGame ag = mgr.getStatus().getAnnotatedGame();
        assert ag.getScore(p1) == 4;
        assert ag.getScore(p2) == 0;

        // turn 1
        mgr.step(); // p2 plays 3 for 4 points
        assert turnCount[0] == 2;
        if (printDebug) debugPrint(mgr);
        assert ag.getScore(p2) == 4;
        assert mgr.getBoard().get(new QwirkleLocation(-1, 0)).equals(new QwirklePiece("gc"));

        // turn 2
        mgr.step(); // p1 plays 3 for 7 points (total 11)
        assert turnCount[0] == 3;
        if (printDebug) debugPrint(mgr);
        assert ag.getScore(p1) == 11;

        // turn 3
        mgr.step(); // p2 discards 2 (still 4)
        assert turnCount[0] == 4;
        if (printDebug) debugPrint(mgr);
        assert ag.getScore(p2) == 4;

        // turn 4
        mgr.step(); // p1 discards 2 (still 11)
        assert turnCount[0] == 5;
        if (printDebug) debugPrint(mgr);

        // turn 5
        mgr.step(); // p2 plays 2 for 9 (13)
        assert turnCount[0] == 6;
        if (printDebug) debugPrint(mgr);
        assert ag.getScore(p2) == 13;
        assert ag.getScore(p1) == 11;
        assert ag.getBestTurn().getScore() == 9;
        assert ag.getBestTurn().containsLocation(2, 1);
        assert ag.getBestTurn().containsLocation(2, 2);

        // turn 6 -- automated
        mgr.step(); // p1 plays 2 to complete a set for 12 (23) (controlled by MaxPlayer)
        assert turnCount[0] == 7;
        if (printDebug) debugPrint(mgr);
        assert ag.getScore(p1) == 23;
        assert ag.getBestTurn().getScore() == 12;

        // Shouldn't have received game over yet
        assert receivedGameOver[0] == 0;

        // march to end and make sure we heard about it through our listener
        while (!mgr.isFinished()) {
            int prev = turnCount[0];
            mgr.step();
            if (printDebug) debugPrint(mgr);
            else System.out.print(turnCount[0] + " ");
            assert turnCount[0] == prev + 1;
        }
        //noinspection AssertWithSideEffects
        assert firstAnnotated[0].getTurns().size() == turnCount[0]; // no real side effects

        // check that we received the game-over event
        assert receivedGameOver[0] == 1;
        if (!printDebug)
            System.out.println("Scripted game ended");

        // check that we can start a new game
        final GameStarted[] nextStarted = { null };
        bus.register(new Object() {
            @Subscribe public void started(GameStarted start) {
                nextStarted[0] = start;
            }
        });
        assert nextStarted[0] == null;
        mgr.start();
        assert nextStarted[0] != null;
        AnnotatedGame newAnnotated = nextStarted[0].getStatus().getAnnotatedGame();
        assert newAnnotated != firstAnnotated[0] : "old annotated game didn't detach when game ended";
        //noinspection AssertWithSideEffects
        assert newAnnotated.getTurns().size() == 0; // no real side effects
    }

    public static void debugPrint(ScriptedGameModel mgr) {
        System.out.println();
        System.out.println("Turn #" + (mgr.getStatus().getAnnotatedGame().getTurns().size() - 1) + ":");
        System.out.println(mgr);
    }

    static void testScoring(boolean debug) {
        // shapes: csd; colors: rgby

        // turn 0 -- p1 plays only 3 (could play 4)
        String h1 = "rc,0,0,gc,1,0,bc,2,0;";
        // turn 1 -- p2 plays 2 to complete a green set, for 6 points
        String h2 = "gs,1,1,gd,1,2;";
        // turn 2 -- p1 plays 3 to complete 2 sets, for 14 points
        h1 += "yc,3,0,ys,3,-1,yd,3,-2;";
        // turn 3 -- p2 plays 3 along the top, for 8 points
        h2 += "bd,2,-2,gd,1,-2,rd,0,-2;";
        // turn 4 -- p1 plays 2 along the bottom, for 3 points
        h1 += "rd,0,2,bd,-1,2;";
        // turn 5 -- p2 plays 3 up the left side, for 11 points
        h2 += "yd,-2,2,gd,-2,1,bd,-2,0;";
        // turn 6 -- p1 plays 3 out to the left, completing 3 sets, for 20 points
        h1 += "rs,0,-1,rc,-1,-1,rd,-2,-1;";
        // turn 7 -- p2 makes a play skipping across a column, for 10 points
        h2 += "rd,-1,1,bd,-3,1,yd,-4,1;";
        // turns 8-12 -- set up a double completion
        h1 += "yd,4,-1,yc,5,-1;";
        h2 += "bc,5,-2,rc,5,0;";
        h1 += "bc,6,0,yc,7,0,gc,8,0;";
        h2 += "gc,6,-2,rc,7,-2;";
        h1 += "rc,8,-1;";
        // turn 13 -- the corner + skip below, for 16
        h2 += "yc,8,-2,bc,8,1;";

        // initialize game
        AsyncPlayer p1 = new AsyncPlayerWrapper(new ScriptedPlayer("p1", h1));
        AsyncPlayer p2 = new AsyncPlayerWrapper(new ScriptedPlayer("p2", h2));
        List<AsyncPlayer> players = Arrays.asList(p1, p2);
        // simple deck with 3 shapes & 4 colors
        ScriptedSettings settings
                = new ScriptedSettings(players, "csd", "rgby");
        EventBus bus = new EventBus();
        ScriptedGameModel mgr = new ScriptedGameModel(bus);
        mgr.start(settings);

        // before turn 0
        assert mgr.getHand(p1).size() == 4;
        assert mgr.getHand(p2).size() == 4;

        // turn 0 -- p1 plays 3 for 3
        mgr.step();
        if (debug) debugPrint(mgr);
        assert mgr.getStatus().getAnnotatedGame().getScore(p1) == 3;

        // turn 1 -- p2 plays 2 for 6
        mgr.step();
        if (debug) debugPrint(mgr);
        assert mgr.getStatus().getAnnotatedGame().getScore(p2) == 6;

        // turn 2 -- p1 plays 3 for 14
        mgr.step();
        if (debug) debugPrint(mgr);
        assert mgr.getStatus().getAnnotatedGame().getBestTurn().getScore() == 14;
        assert mgr.getStatus().getAnnotatedGame().getScore(p1) == 17;

        // turn 3 -- p2 plays 3 for 8
        mgr.step();
        if (debug) debugPrint(mgr);
        assert mgr.getStatus().getAnnotatedGame().getMostRecentTurn().getScore() == 8;

        // turn 4 -- p1 plays 2 for 3
        mgr.step();
        if (debug) debugPrint(mgr);
        assert mgr.getStatus().getAnnotatedGame().getMostRecentTurn().getScore() == 3;

        // turn 5 -- p2 plays 3 for 8
        mgr.step();
        if (debug) debugPrint(mgr);
        assert mgr.getStatus().getAnnotatedGame().getMostRecentTurn().getScore() == 11;

        // turn 6 -- p1 plays 3 for 20
        mgr.step();
        if (debug) debugPrint(mgr);
        assert mgr.getStatus().getAnnotatedGame().getMostRecentTurn().getScore() == 20;

        // turn 7 -- p2 plays 3 for 10
        mgr.step();
        if (debug) debugPrint(mgr);
        assert mgr.getStatus().getAnnotatedGame().getMostRecentTurn().getScore() == 10;

        mgr.step();
        mgr.step();
        mgr.step();
        mgr.step();
        mgr.step();
        if (debug) debugPrint(mgr);

        mgr.step();
//        if (debug)
            debugPrint(mgr);
        assert mgr.getStatus().getAnnotatedGame().getMostRecentTurn().getScore() == 16;
    }
}
