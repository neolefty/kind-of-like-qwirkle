package qwirkle.ui.swing.attic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
* Created by Bill on 6/30/2014.
*/
public class RandomColorListener implements ActionListener {
//    // button: random color
//    JButton randomColorButton = new JButton("Random Color");
//    outer.add(panel, BorderLayout.SOUTH);
//    randomColorButton.addActionListener(new RandomColorListener(panel));

    private final JPanel panel;

    public RandomColorListener(JPanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        panel.setBackground(randomColor());
    }

    public static Color randomColor() {
        return Color.getHSBColor((float) Math.random(), 1, 1);
    }
}
