package qwirkle.ui;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.*;
import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleTurn;
import qwirkle.ui.util.AutoSizeLabel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Start, stop, take a turn. */
public class GameControlPanel extends JPanel {
    private static final String PAUSE = "  | |  ", PLAY = "  >>  ",
        STEP = "Turn", NEW_GAME = "New Game", RESTART = "New Game";

    public GameControlPanel(final GameManager game) {
        final JLabel remaining = new AutoSizeLabel(this, "", 0.05);
        // label: the number of remaining cards
        game.getEventBus().register(new Object() {
            @Subscribe
            void update(QwirkleBoard board) {
                remaining.setText(game.getDeck().size() + "");
            }
        });

        // button: new game
        final JButton newGame = new JButton(NEW_GAME);
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                game.start();
            }
        });

        // button: take a single turn
        final JButton stepButton = new JButton(STEP);
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                game.step();
            }
        });

        // button: start/pause a game running
        final JButton runButton = new JButton(PLAY);
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

        // score labels - update when score changes
        for (final QwirklePlayer player : game.getPlayers()) {
            final JLabel score = new AutoSizeLabel(this, "", 0.05);
//            setColors(score);
            game.getStatus().listen(new GameStatus.TurnListener() {
                @Override
                public void turn(final QwirkleTurn turn) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            String summary = "  " + player.getName();
                            if (turn != null && turn.getStatus() != null)
                                summary += ": " + turn.getStatus().getScore(player);
                            score.setText(summary);
                        }
                    });
                }
            });
            add(score);
        }
    }
}
