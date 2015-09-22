package qwirkle.ui.swing.game;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.*;
import qwirkle.event.GameOver;
import qwirkle.event.GameStarted;
import qwirkle.event.GameThreadStatus;
import qwirkle.game.QwirkleBoard;
import qwirkle.event.TurnCompleted;
import qwirkle.ui.swing.util.AutoSizeButton;
import qwirkle.ui.swing.util.AutoSizeLabel;

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

    public GameControlPanel(final GameController control) {
        // label: the number of remaining cards
        final JLabel remaining = new AutoSizeLabel(this, "", FONT_PROPORTION);
        // button: new game
        final JButton newGame = new AutoSizeButton(this, NEW_GAME, FONT_PROPORTION);
        // button: take a single turn
        final JButton stepButton = new AutoSizeButton(this, STEP, FONT_PROPORTION);

        control.register(new Object() {
            @Subscribe
            public void update(QwirkleBoard board) {
                remaining.setText(control.getGame().getDeck().size() + "");
            }

            @Subscribe
            public void turn(TurnCompleted turn) { // when a turn has been taken, enable the take-a-turn button
                // get back into the event loop to re-enable the button
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        stepButton.setEnabled(true);
                        stepButton.grabFocus();
                    }
                });
            }
        });

        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                control.getGame().start();
            }
        });

        // take turns in their own thread, to avoid blocking the event queue
        final ExecutorService turnTaker = Executors.newSingleThreadExecutor();
        stepButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent actionEvent) {
                stepButton.setEnabled(false); // button will be re-enabled once the turn has been taken
                // take a turn outside of the event thread, to avoid delays
                if (!control.getHypothetical().isEmpty())
                    control.getHypothetical().confirm();
                else
                    turnTaker.submit(new Runnable() {
                    @Override public void run() {
                        control.getGame().step();
                    }
                });
            }
        });

        // button: start/pause a game running
        final JButton runButton = new AutoSizeButton(this, PLAY, FONT_PROPORTION);
        final QwirkleThreads threads = new QwirkleThreads(control);
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (threads.isRunning())
                    threads.stop();
                else {
                    if (control.getGame().isFinished())
                        control.getGame().start();
                    threads.go();
                }
            }
        });

        control.register(new Object() {
            @Subscribe
            public void update(final GameThreadStatus event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (event.isRunning()) {
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
        control.register(new Object() {
            @Subscribe
            public void gameStarted(GameStarted started) {
                stepButton.setEnabled(true);
                newGame.setText(RESTART);
            }

            @Subscribe
            public void gameOver(GameOver over) {
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
