package qwirkle.ui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseSensitivePanel extends JPanel {
    private Color background, mouseover, press;
    private boolean pressed = false;
    private boolean mouseOver = false;
    private boolean highlighted = false;

    public MouseSensitivePanel(Color background, Color mouseover, final Color press) {
        this.background = background;
        this.mouseover = mouseover;
        this.press = press;
        setOpaque(true);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                // if already highlighted, assume it's from an external cause, and we shouldn't interfere
                if (!highlighted) {
                    mouseOver = true;
                    setHighlighted(true);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                // only unhighlight if we are highlighted because of entry, not because of external highlighting
                if (mouseOver) {
                    mouseOver = false;
                    setHighlighted(false);
                }
            }
            @Override public void mousePressed(MouseEvent e) {
                pressed = true; repaint();
            }

            @Override public void mouseReleased(MouseEvent e) {
                pressed = false; repaint();
            }
        });
    }

    /** Set this to be highlighted. */
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        repaint();
    }

    /** Is this highlighted? */
    public boolean isHighlighted() { return highlighted; }

    /** Is the mouse over this? */
    public boolean isMouseOver() { return mouseOver; }

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
//        else if (highlighted)
//            System.out.println("Entered " + this);
        return pressed ? press : (highlighted ? mouseover : background);
    }
}
