package qwirkle.attic.swing;

import qwirkle.game.base.QwirkleAI;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.QwirkleSettings;
import qwirkle.game.control.GameController;
import qwirkle.game.control.impl.NewThreadEachTime;
import qwirkle.game.control.players.MaxAI;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.swing.main.QwirkleFrame;
import qwirkle.ui.swing.util.SwingSetup;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SwingOldMain {
    public static final int STEP_MILLIS = 5000;

    public static void main(String[] args) {
        // game model
        List<QwirkleAI> players = new ArrayList<>();
//        players.add(new StupidPlayer("1"));
        players.add(new MaxAI());
//        players.add(new StupidPlayer("2"));
        players.add(new MaxAI());
        players.add(new MaxAI());
        players.add(new MaxAI());
        players.add(new MaxAI());
        players.add(new MaxAI());
        QwirkleSettings settings = new QwirkleSettings(QwirklePlayer.wrap(players));
        final QwirkleUIController control = new QwirkleUIController(settings, new NewThreadEachTime());
        control.getGame().start();

        // display
        JPanel ui = SwingSetup.createUI(control);
        JFrame frame = new QwirkleFrame();
        frame.setContentPane(ui);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        SwingSetup.addWindowSizer(frame, SwingOldMain.class);
        frame.setVisible(true);

        // go!
        new Thread() {
            @Override
            public void run() {
                try {
                    //noinspection InfiniteLoopStatement
//                    for (int i = 0; i < 10; ++i) {
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        long start = System.currentTimeMillis();
                        GameController game = control.getGame();
                        if (game.isFinished()) {
                            if (game.getBoard() != null && game.getBoard().size() > 0) {
                                System.out.println(game);
                                sleep(5000);
                            }
                            game.start(new QwirkleSettings(20));
                        }
                        game.stepAI();
//                        System.out.println(game.getBoard());
                        long elapsed = System.currentTimeMillis() - start;
                        long sleep = STEP_MILLIS - elapsed;
                        if (sleep > 1) sleep(sleep);
                    }
                } catch(InterruptedException ignored) { }
            }
        }.start();
    }
}
