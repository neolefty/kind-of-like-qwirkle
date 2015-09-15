package qwirkle.ui.swing;

import qwirkle.ui.paint.colors.ColorSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// TODO make background change fade in & out -- make a background manager thread ...
/** Manages a compoment's background color based on mouse movements. */
public class BackgroundManager {
    private JComponent comp;
    private ColorSet bg;
    private boolean pressed = false, mouseOver = false, highlighted = false;

    public BackgroundManager(JComponent comp, ColorSet bg) {
        this.comp = comp;
        comp.setOpaque(true);
        comp.addMouseListener(new Mouser());
        this.bg = bg;
        update();
    }

    /** Set this to be highlighted. May be triggered by a mouseover or some external reason. */
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        update();
    }

    /** Is this highlighted? May be set externally or by a mouseover. */
    public boolean isHighlighted() { return highlighted; }

    /** Is the mouse button currently pressed (with this as mouse focus)? */
    private void setPressed(boolean pressed) {
        this.pressed = pressed;
        update();
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

    private class Mouser extends MouseAdapter {
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
    }

    private Color getCurrentColor() {
        return isPressed() ? bg.getActivated() : (isHighlighted() ? bg.getHighlight() : bg.getNormal());
    }

    private void update() {
        comp.setBackground(getCurrentColor());
    }
}
