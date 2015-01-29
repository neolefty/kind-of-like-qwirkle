package qwirkle.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseSensitivePanel extends JPanel {
    private Color background, mouseover, press;
    private boolean pressed = false;
    private boolean entered = false;

    public MouseSensitivePanel(Color background, Color mouseover, final Color press) {
        this.background = background;
        this.mouseover = mouseover;
        this.press = press;
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                entered = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) {
                entered = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) {
                pressed = true; repaint(); }
            @Override public void mouseReleased(MouseEvent e) {
                pressed = false; repaint(); }
        });
    }

    @Override
    public void paint(Graphics g) {
        Color c = g.getColor();
        g.setColor(getCurrentColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(c);
    }

    private Color getCurrentColor() {
//        if (pressed)
//            System.out.println("Pressed " + this);
//        else if (entered)
//            System.out.println("Entered " + this);
        return pressed ? press : (entered ? mouseover : background);
    }
}
