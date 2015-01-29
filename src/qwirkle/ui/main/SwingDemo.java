package qwirkle.ui.main;

import qwirkle.control.GameManager;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleSettings;
import qwirkle.players.MaxPlayer;
import qwirkle.ui.GameControlPanel;
import qwirkle.ui.GameStatusPanel;
import qwirkle.ui.QwirkleGamePanel;
import qwirkle.ui.util.SwingSetup;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SwingDemo {

    // TODO add human player
    // TODO add player panels
    // TODO add taunts
    // TODO choose shapes & colors to use
    // TODO enable designing your own shape / color
    // TODO test different numbers of shapes & colors

    public static void main(String[] args) {
        // make a window
        QwirkleFrame frame = new QwirkleFrame();
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // listen for movement & save it to prefs
        SwingSetup.addWindowSizer(frame);

        // set up a game
        java.util.List<QwirklePlayer> players
                = Arrays.asList((QwirklePlayer) new MaxPlayer("Sam"), new MaxPlayer("Gilly"));
//        final QwirklePlayer player3 = new StupidPlayer("1");
        QwirkleSettings settings = new QwirkleSettings(players);
        final GameManager game = new GameManager(settings);

        // create a view
        final JPanel outer = new JPanel();
//        outer.setBackground(Color.BLACK);
        outer.setLayout(new BorderLayout());
        frame.setContentPane(outer);

        // center: game & players
        QwirkleGamePanel gamePanel = new QwirkleGamePanel(game);
//        setColors(gamePanel);
        outer.add(gamePanel, BorderLayout.CENTER);

        // bottom: game controls
        GameControlPanel controls = new GameControlPanel(game);
//        setColors(controls);
        outer.add(controls, BorderLayout.SOUTH);

        // top: game status messages
        GameStatusPanel status = new GameStatusPanel(game);
//        setColors(status);
        outer.add(status, BorderLayout.NORTH);

        setColors(outer);

/*
        // TODO remove this once we fix getPreferredSize in QwirkleGridLayout
        // resize behavior
        outer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension d = new Dimension(outer.getWidth() / 8, grid.getHeight());
                for (PlayerPanel p : playerPanels) {
//                    p.setSize(d);
                    p.setMinimumSize(d);
                }
                outer.invalidate();
//                outer.repaint();
            }
        });
*/

        // show the window
        frame.setVisible(true);
        game.start();
    }

    // Recursively set the colors & font of comp to match our scheme
    public static void setColors(Component comp) {
        comp.setForeground(Color.WHITE);
        comp.setBackground(Color.BLACK);
        comp.setFont(comp.getFont().deriveFont(30f));
        if (comp instanceof Container) {
            Container panel = (Container) comp;
            synchronized (panel.getTreeLock()) {
                for (Component child : panel.getComponents())
                    setColors(child);
            }
        }
    }
}
