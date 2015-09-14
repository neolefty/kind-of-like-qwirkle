package qwirkle.ui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// TODO make this into a listener instead
/** A panel that highlights itself based on mouse movements. */
public class MouseSensitivePanel extends JPanel {
    // colors
    private Color bgColor, highlightColor, mousePressColor;

    // mouse state
    private boolean pressed = false, mouseOver = false, highlighted = false;

    public MouseSensitivePanel(Color bgColor, Color highlightColor, final Color mousePressColor) {
        this.highlightColor = highlightColor;
        this.bgColor = bgColor;
        this.mousePressColor = mousePressColor;
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // if already highlighted, assume it's from an external cause, and we shouldn't interfere
                if (!highlighted) {
                    mouseOver = true;
                    setHighlighted(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // only unhighlight if we are highlighted because of entry, not because of external highlighting
                if (isMouseOver()) {
                    mouseOver = false;
                    setHighlighted(false);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setPressed(true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setPressed(false);
            }
        });
    }

    /** Set this to be highlighted. May be triggered by a mouseover or some external reason. */
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        repaint();
    }

    /** Is this highlighted? May be set externally or by a mouseover. */
    public boolean isHighlighted() { return highlighted; }

    /** Is the mouse button currently pressed (with this as mouse focus)? */
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
        repaint();
    }

    /** Is the mouse button currently pressed (with this as mouse focus)? */
    public boolean isPressed() {
        return pressed;
    }

    /** Was the current highlight triggered by a mouseover?
     *
     *  <p>Note: Will be <tt>false</tt> even if the mouse is currently over this,
     *  if {@link #setHighlighted} was called externally.</p> */
    public boolean isMouseOver() { return mouseOver; }

    @Override
    public void paint(Graphics g) {
        Color c = g.getColor();
        g.setColor(getCurrentColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(c);
    }

    private Color getCurrentColor() {
        return isPressed() ? mousePressColor : (isHighlighted() ? highlightColor : bgColor);
    }
}
