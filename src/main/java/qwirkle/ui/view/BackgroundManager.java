package qwirkle.ui.view;

import qwirkle.game.base.QwirkleColor;
import qwirkle.ui.view.colors.ColorSet;
import qwirkle.ui.event.PassOver;

import java.util.ArrayList;

// TODO make background change fade in & out -- make a background manager thread ...
/** Manages a {@link HasBackground} UI element's background color based on mouse movements.
 *  Also posts {@link PassOver} events. */
public class BackgroundManager {
    private HasBackground ui;
    private java.util.List<ColorSet> bgStack = new ArrayList<>();
    private boolean pressed = false, hover = false, highlighted = false;

    public BackgroundManager(HasBackground ui, ColorSet bg) {
        this.ui = ui;
        pushColors(bg);
        update();
    }

    /** Should this be highlighted?
     * Is the pointer over it or has {@link #setHighlighted} been called? */
    public boolean shouldHighlight() { return highlighted || hover; }

    // TODO if mouse exits when should stay highlighted, stay highlighted

    /** The mouse button is currently pressed (with this as mouse focus) or not. */
    public void setMousePressed(boolean pressed) {
        if (pressed != this.pressed) {
            this.pressed = pressed;
            update();
        }
    }
    /** Set this to be highlighted for some reason other than a mouse hover. */
    public void setHighlighted(boolean highlighted) {
        if (this.highlighted != highlighted) {
            this.highlighted = highlighted;
            update();
        }
    }

    /** The pointer is currently over or not over this. */
    public void setHover(boolean hover) {
        if (this.hover != hover) {
            this.hover = hover;
            update();
        }
    }

    /** Is the mouse button currently pressed (with this as mouse focus)? */
    public boolean isPressed() {
        return pressed;
    }

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
        QwirkleColor result = isPressed() ? cs.getActivated() : (shouldHighlight() ? cs.getHighlight() : cs.getNormal());
        if (result == null)
            System.out.println("ColorSet: " + cs + "; is pressed? " + isPressed() + "; should highlight? " + shouldHighlight());
        return result;
    }

    private void update() {
        ui.setBackground(getCurrentColor());
    }
}
