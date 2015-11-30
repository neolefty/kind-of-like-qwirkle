package qwirkle.ui.swing.game;

import com.google.common.eventbus.Subscribe;
import qwirkle.game.event.GameOver;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.ThreadStatus;
import qwirkle.game.base.QwirkleBoard;
import qwirkle.game.event.TurnCompleted;
import qwirkle.ui.event.PlayPiece;
import qwirkle.ui.swing.util.AutoSizeButton;
import qwirkle.ui.swing.util.AutoSizeLabel;
import qwirkle.ui.control.QwirkleUIController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Start, stop, take a turn. */
public class GameControlPanel extends JPanel {
    private static final String PAUSE = "  | |  ", PLAY = "  >>  ",
        STEP_AI = "AI Turn", NEW_GAME = "New Game", RESTART = "New Game",
        STEP_FINISH_HUMAN = "Finished Turn";

    public static final double FONT_PROPORTION = 0.027;

    public GameControlPanel(final QwirkleUIController control) {
        // label: the number of remaining cards
        final JLabel remaining = new AutoSizeLabel(this, "108", FONT_PROPORTION);
        // button: new game
        final JButton newGame = new AutoSizeButton(this, NEW_GAME, FONT_PROPORTION);
        // button: take a single turn
        final JButton stepButton = new AutoSizeButton(this, STEP_AI, FONT_PROPORTION);
        // button: start/pause a game running
        final JButton runButton = new AutoSizeButton(this, PLAY, FONT_PROPORTION);

        // lay them out
        add(new AutoSizeLabel(this, "Remaining: ", FONT_PROPORTION));
        add(remaining);
        add(new AutoSizeLabel(this, " ", FONT_PROPORTION));
        add(newGame);
        add(Box.createHorizontalStrut(10));
        add(stepButton);
        add(Box.createHorizontalStrut(10));
        add(runButton);

        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                control.getGame().start();
            }
        });

        // take turns in their own thread, to avoid blocking the event queue
        final ExecutorService turnTaker = Executors.newSingleThreadExecutor();
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                stepButton.setEnabled(false); // button will be re-enabled once the turn has been taken
                // take a turn outside of the event thread, to avoid delays
                if (!control.getHypothetical().isEmpty())
                    control.getHypothetical().confirm();
                else
                    turnTaker.submit(new Runnable() {
                        @Override
                        public void run() {
                            control.getGame().stepAI();
                        }
                    });
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            if (control.getThreads().isRunning())
                control.getThreads().stop();
            else
                control.getThreads().go();
            }
        });

        control.register(new Object() {
        });

        control.register(new Object() {
            // update run button when autoplay starts & stops
            @Subscribe
            public void update(final ThreadStatus event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (event.isRunning()) {
                            runButton.setText(PAUSE);
                            // disable step button while game is auto-playing
                            stepButton.setEnabled(false);
                        } else {
                            runButton.setText(PLAY);
                            // enable step button while not auto-playing
                            stepButton.setEnabled(true);
                        }
                    }
                });
            }

            // change text of turn button when a human is playing
            @Subscribe
            public void updateHumanPlay(PlayPiece event) {
                if (event.isAccept())
                    stepButton.setText(STEP_FINISH_HUMAN);
                else if (event.isUnpropose() && event.getPlay().size() == 0)
                    stepButton.setText(STEP_AI);
            }

            // display the number of cards remaining
            @Subscribe
            public void update(QwirkleBoard board) {
                remaining.setText(control.getGame().getDeck().size() + "");
            }

            // after a turn has been taken, enable the take-a-turn button
            @Subscribe
            public void turn(TurnCompleted turn) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!control.getThreads().isRunning()) {
                            stepButton.setEnabled(true);
                            stepButton.grabFocus();
                            stepButton.setText(STEP_AI);
                        }
                    }
                });
            }

            // when a game ends, disable taking another turn
            @Subscribe
            public void gameOver(GameOver event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        stepButton.setEnabled(false);
                        newGame.setText(NEW_GAME);
                    }
                });
            }

            // when a game starts, update button text & enable the turn button (unless auto-playing)
            @Subscribe
            public void gameStarted(GameStarted started) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!control.getThreads().isRunning())
                            stepButton.setEnabled(true);
                        newGame.setText(RESTART);
                    }
                });
            }
        });
    }
}
