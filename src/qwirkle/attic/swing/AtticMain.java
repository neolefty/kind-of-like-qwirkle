package qwirkle.attic.swing;

import qwirkle.control.InteractionController;
import qwirkle.ui.swing.game.board.QwirkleGridPanel;

import javax.swing.*;

public class AtticMain {
    // compare the three game board panels
    public static JComponent createThreePanelTest(InteractionController events) {
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(new BoardMonolithicPanel(events.getEventBus()));
        box.add(new QwirkleGridPanel(events.getEventBus()));
        box.add(new SquareLayout.SquarePanel(new QwirkleGridLayoutPanel(events.getEventBus())));
        return box;
    }
}
