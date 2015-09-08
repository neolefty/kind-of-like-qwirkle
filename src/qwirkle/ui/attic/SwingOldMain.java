package qwirkle.ui.attic;

import qwirkle.control.NewThreadEachTime;
import qwirkle.game.QwirkleSettings;
import qwirkle.game.QwirklePlayer;
import qwirkle.players.AsyncPlayerWrapper;
import qwirkle.players.MaxPlayer;
import qwirkle.control.GameManager;
import qwirkle.ui.main.QwirkleFrame;
import qwirkle.ui.swing.SwingSetup;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SwingOldMain {
    public static final int STEP_MILLIS = 5000;

    public static void main(String[] args) {
        // game model
        List<QwirklePlayer> players = new ArrayList<>();
//        players.add(new StupidPlayer("1"));
        players.add(new MaxPlayer());
//        players.add(new StupidPlayer("2"));
        players.add(new MaxPlayer());
        players.add(new MaxPlayer());
        players.add(new MaxPlayer());
        players.add(new MaxPlayer());
        players.add(new MaxPlayer());
        QwirkleSettings settings = new QwirkleSettings(AsyncPlayerWrapper.wrap(players));
        final GameManager game = new GameManager(settings, new NewThreadEachTime());
        game.start();

        // display
        JPanel ui = SwingSetup.createUI(game);
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
                        if (game.isFinished()) {
                            if (game.getBoard() != null && game.getBoard().size() > 0) {
                                System.out.println(game);
                                sleep(5000);
                            }
                            game.start(new QwirkleSettings(20));
                        }
                        game.step();
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
