package qwirkle.ui.view;

import qwirkle.game.base.QwirkleColor;
import qwirkle.ui.colors.ColorSet;
import qwirkle.ui.event.PassOver;

import java.util.ArrayList;

// TODO make background change fade in & out -- make a background manager thread ...
/** Manages a {@link HasBackground} UI element's background color based on mouse movements.
 *  Also posts {@link PassOver} events. */
public class BackgroundManager {
    private HasBackground ui;
    private java.util.List<ColorSet> bgStack = new ArrayList<>();
    private boolean pressed = false, mouseOver = false, highlighted = false;

    public BackgroundManager(HasBackground ui, ColorSet bg) {
        this.ui = ui;
        pushColors(bg);
        update();
    }

    /** Set this to be highlighted. May be triggered by a mouseover or some external reason. */
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        update();
    }

    /** Is this highlighted? May be set externally or by a mouseover. */
    public boolean isHighlighted() { return highlighted; }

    // TODO if mouse exits when should stay highlighted, stay highlighted
    /** The mouse button is currently pressed (with this as mouse focus) or not. */
    public void setMousePressed(boolean pressed) {
        if (pressed != this.pressed) {
            this.pressed = pressed;
            update();
        }
    }

    /** The mouse is currently over or not over this. */
    public void setMouseOver(boolean mouseOver) {
        // if already highlighted, assume it's from an external cause, and we shouldn't interfere
        if (this.mouseOver != mouseOver) {
            this.mouseOver = mouseOver;
            // only change highlighting because of entry, not because of external highlighting
            setHighlighted(mouseOver);
        }
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

    /** Undo previous {@link #pushColors}.
     *  <em>Note:</em> No effect if this would empty the stack -- at least one {@link ColorSet} is kept. */
    public void popColors() {
        if (bgStack.size() > 1)
            bgStack.remove(bgStack.size() - 1);
        update();
    }

    /** Change the current colors, with the option to undo this change later. */
    public void pushColors(ColorSet colors) {
        if (colors == null)
            throw new NullPointerException("ColorSet is null");
        bgStack.add(colors);
        update();
    }

    public void setColors(ColorSet colors) {
        popColors();
        pushColors(colors);
    }

    public ColorSet getColors() {
        return bgStack.get(bgStack.size() - 1);
    }

    private QwirkleColor getCurrentColor() {
        ColorSet cs = getColors();
        return isPressed() ? cs.getActivated() : (isHighlighted() ? cs.getHighlight() : cs.getNormal());
    }

    private void update() {
        ui.setBackground(getCurrentColor());
    }
}
