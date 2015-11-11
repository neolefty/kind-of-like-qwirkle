package qwirkle.test;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameController;
import qwirkle.control.QwirkleThreads;
import qwirkle.control.impl.NewThreadEachTime;
import qwirkle.event.GameOver;
import qwirkle.event.TurnCompleted;
import qwirkle.game.AsyncPlayer;
import qwirkle.game.QwirkleColor;
import qwirkle.game.QwirkleSettings;
import qwirkle.game.QwirkleShape;
import qwirkle.game.impl.AsyncPlayerWrapper;
import qwirkle.players.MaxPlayer;
import qwirkle.players.RainbowPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Test QwirkleThreads, which can run a game with machine players on a clock. */
public class TestThreads {
    public static void main(String[] args) {
        TestMain.checkAssert();
        testPreventClobber();
    }

    private static void testPreventClobber() {
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
        final CountDownLatch waiting = new CountDownLatch(1);
        control.register(new Object() {
            @Subscribe public void turn(TurnCompleted event) {
//                System.out.println(event);
            }
            @Subscribe public void over(GameOver over) {
//                System.out.println(over.getStatus().getLeader() + " wins " );
//                System.out.println(over.getStatus().getBoard());
                waiting.countDown();
            }
        });
        try {
            waiting.await(30, TimeUnit.SECONDS);
            System.out.println("+++ TestThreads: Done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // TODO test that stupid players finish a game in almost exactly the time you expect (turn wait * number of pieces, +/- a few for passes)
}
