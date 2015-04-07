package qwirkle.ui.main;

import qwirkle.game.QwirkleColor;
import qwirkle.game.QwirkleShape;
import qwirkle.ui.util.SwingKitty;
import qwirkle.ui.util.SwingSetup;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllTheShapes {
    public static void main(String[] args) {
        JFrame frame = new JFrame("All The Shapes!");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        SwingSetup.addWindowSizer(frame);

        int copies = 2;
        List<QwirkleShape> shapes = new ArrayList<>();
        for (int i = 0; i < copies; ++i)
            shapes.addAll(Arrays.asList(QwirkleShape.values()));
        QwirkleShape[] shapesArray = shapes.toArray(new QwirkleShape[shapes.size()]);
        ShapeBouncer bouncer = new ShapeBouncer(shapesArray, QwirkleColor.values());
        frame.setContentPane(bouncer);
        SwingKitty.setColors(bouncer);

        frame.setVisible(true);
    }
}
