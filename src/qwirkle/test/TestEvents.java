package qwirkle.test;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.AnnotatedGame;
import qwirkle.control.event.GameOver;
import qwirkle.control.event.GameStarted;
import qwirkle.game.QwirkleLocation;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleTurn;

import java.util.Arrays;
import java.util.List;

/** Use a scripted game to test AnnotatedGame, which relies on
 *  GameStatus and QwirkleTurn events. */
public class TestEvents {
    public static void main(String[] args) {
        TestQwirkle.checkAssert();
        testAnnotatedGame(false);
    }

    private static void testAnnotatedGame(boolean printDebug) {
        String h1 = "pc,0,0,ph,0,1,pd,0,2,p4,0,3;"; // p1 plays 4
        String h2 = "oc,1,0,rc,2,0,gc,-1,0;"; // p2 plays 3
        h1 += "oh,1,1,od,1,2;"; // p1 plays 2
        h2 += "d:os,rs,gs;"; // p2 discards 2
        h1 += "d:p4,p8;"; // p1 discards 2
        h2 += "rh,2,1,rd,2,2;"; // p2 plays 2

        ScriptedPlayer p1 = new ScriptedPlayer("p1", h1);
        ScriptedPlayer p2 = new ScriptedPlayer("p2", h2);
        List<QwirklePlayer> players = Arrays.asList((QwirklePlayer) p1, p2);
        ScriptedPlayer.ScriptedSettings settings = new ScriptedPlayer.ScriptedSettings(players,
                "pc,ph,pd,p4,od,oh," // p1 initial
                        + "oc,rc,gc,os,rs,gs," // p2 initial
                        + "p4,p8,yc,bc," // p1 played 4
                        + "rh,rd,r4," // p2 played 3
                        + "os,os," // p1 played 2
                        + "yd,yd,yd," // p2 discarded 3
                        + "gd,gd," // p1 discarded 1
                        + "os,os," // p2 played 2
        );
        final ScriptedPlayer.ScriptedGameManager mgr = new ScriptedPlayer.ScriptedGameManager();

        // listen for the game to start, end, and each turn to pass
        final int[] receivedGameStarted = { 0 };
        final AnnotatedGame[] firstAnnotated = { null };
        final int[] receivedGameOver = { 0 };
        final int[] turnCount = {0};
        mgr.getEventBus().register(new Object() {
            @Subscribe public void gameStarted(GameStarted started) {
                receivedGameStarted[0]++;
                if (firstAnnotated[0] == null) // only grab it the first time
                    firstAnnotated[0] = started.getStatus().getAnnotatedGame();
            }
            @Subscribe public void gameOver(GameOver over) { receivedGameOver[0]++; }
            @Subscribe public void turnPassed(QwirkleTurn turn) { ++turnCount[0]; }
        });

        assert firstAnnotated[0] == null;
        mgr.start(settings);
        assert receivedGameStarted[0] == 1;
        assert firstAnnotated[0] != null;

        mgr.step(); // p1 plays 4 for 4 points
        assert turnCount[0] == 1;
        if (printDebug) System.out.println(mgr);
        AnnotatedGame ag = mgr.getStatus().getAnnotatedGame();
        assert ag.getScore(p1) == 4;
        assert ag.getScore(p2) == 0;

        mgr.step(); // p2 plays 3 for 4 points
        assert turnCount[0] == 2;
        if (printDebug) System.out.println(mgr);
        assert ag.getScore(p2) == 4;
        assert mgr.getBoard().get(new QwirkleLocation(-1, 0)).equals(new QwirklePiece("gc"));

        mgr.step(); // p1 plays 3 for 7 points (total 11)
        assert turnCount[0] == 3;
        if (printDebug) System.out.println(mgr);
        assert ag.getScore(p1) == 11;

        mgr.step(); // p2 discards 2 (still 4)
        assert turnCount[0] == 4;
        if (printDebug) System.out.println(mgr);
        assert ag.getScore(p2) == 4;

        mgr.step(); // p1 discards 2 (still 11)
        assert turnCount[0] == 5;
        if (printDebug) System.out.println(mgr);

        mgr.step(); // p2 plays 2 for 9 (13)
        assert turnCount[0] == 6;
        if (printDebug) System.out.println(mgr);
        assert ag.getScore(p2) == 13;
        assert ag.getScore(p1) == 11;

        mgr.step(); // p1 plays 2 to complete a set for 12 (23) (controlled by MaxPlayer)
        assert turnCount[0] == 7;
        if (printDebug) System.out.print(mgr);
        assert ag.getScore(p1) == 23;

        // Shouldn't have received game over yet
        assert receivedGameOver[0] == 0;

        // march to end and make sure we heard about it through our listener
        while (!mgr.isFinished()) {
            int prev = turnCount[0];
            mgr.step();
            if (printDebug) System.out.println(mgr);
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
        mgr.getEventBus().register(new Object() {
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
}
