package qwirkle.ui.board;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.*;
import qwirkle.game.QwirkleBoard;
import qwirkle.ui.util.AutoSizeButton;
import qwirkle.ui.util.AutoSizeLabel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Start, stop, take a turn. */
public class GameControlPanel extends JPanel {
    private static final String PAUSE = "  | |  ", PLAY = "  >>  ",
        STEP = "Turn", NEW_GAME = "New Game", RESTART = "New Game";

    public static final double FONT_PROPORTION = 0.03;

    public GameControlPanel(final GameManager game) {
        final JLabel remaining = new AutoSizeLabel(this, "", FONT_PROPORTION);
        // label: the number of remaining cards
        game.getEventBus().register(new Object() {
            @Subscribe
            void update(QwirkleBoard board) {
                remaining.setText(game.getDeck().size() + "");
            }
        });

        // TODO consider putting score labels under buttons

        // button: new game
        final JButton newGame = new AutoSizeButton(this, NEW_GAME, FONT_PROPORTION);
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                game.start();
            }
        });

        // take turns in their own thread, to avoid blocking the event queue
        final ExecutorService turnTaker = Executors.newSingleThreadExecutor();
        // button: take a single turn
        final JButton stepButton = new AutoSizeButton(this, STEP, FONT_PROPORTION);
        stepButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent actionEvent) {
                stepButton.setEnabled(false);
                // take a turn outside of the event thread, to avoid delays
                turnTaker.submit(new Runnable() {
                    @Override public void run() {
                        game.step();
                        // get back into the event loop to re-enable the button
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                stepButton.setEnabled(true);
                                stepButton.grabFocus();
                            }
                        });
                    }
                });
            }
        });

        // button: start/pause a game running
        final JButton runButton = new AutoSizeButton(this, PLAY, FONT_PROPORTION);
        final QwirkleThreads threads = new QwirkleThreads(game);
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (threads.isRunning())
                    threads.stop();
                else {
                    if (game.isFinished())
                        game.start();
                    threads.go();
                }
            }
        });

        game.getEventBus().register(new Object() {
            @Subscribe
            public void update(final QwirkleThreads threads) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (threads.isRunning()) {
                            runButton.setText(PAUSE);
                            stepButton.setEnabled(false);
                        } else {
                            runButton.setText(PLAY);
                            stepButton.setEnabled(true);
                        }
                    }
                });
            }
        });

        // enable turn button based on whether the game is finished
        game.getStatus().listen(new GameStatus.StatusListener() {
            @Override public void gameStarted(GameStarted started) {
                stepButton.setEnabled(true);
                newGame.setText(RESTART);
            }
            @Override public void gameOver(GameOver over) {
                stepButton.setEnabled(false);
                newGame.setText(NEW_GAME);
            }
        });

        // lay them out
        add(remaining);
        add(newGame);
        add(Box.createHorizontalStrut(10));
        add(stepButton);
        add(Box.createHorizontalStrut(10));
        add(runButton);
    }
}
