package qwirkle.attic.swing;

import qwirkle.control.GameManager;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;

import javax.swing.*;

public class AtticMain {
    // compare the three game board panels
    public static JComponent createThreePanelTest(GameManager game) {
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(new BoardMonolithicPanel(game.getEventBus()));
        box.add(new QwirkleGridPanel(game.getEventBus()));
        box.add(new SquareLayout.SquarePanel(new QwirkleGridLayoutPanel(game.getEventBus())));
        return box;
    }
}
