package qwirkle.ui.swing;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** A JLabel that highlights when you mouse over it.
 *  Note: highlightAction is run in the Swing event thread. */
public class HighlightLabel extends AutoSizeLabel {
    private Color highlightBg;
    private Color regularBg;
    private Runnable highlightAction;
    private Runnable unhighlightAction;

    public HighlightLabel(Component parent, double fraction,
                          Color highlightBg, Runnable highlightAction, Runnable unhighlightAction)
    {
        this(parent, fraction, highlightBg);
        setHighlightAction(highlightAction);
        setUnhighlightAction(unhighlightAction);
    }

    public HighlightLabel(Component parent, double fraction, Color highlightBg) {
        super(parent, "", fraction);
        setOpaque(true);
        this.highlightBg = highlightBg;
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { highlight(); }
            @Override public void mouseExited(MouseEvent e) { unhighlight(); }
        });
    }

    public void setHighlightAction(Runnable highlightAction) { this.highlightAction = highlightAction; }
    public void setUnhighlightAction(Runnable unhighlightAction) { this.unhighlightAction = unhighlightAction; }

    private void highlight() {
        regularBg = getBackground();
        setBackground(highlightBg);
        if (highlightAction != null)
            highlightAction.run();
    }

    private void unhighlight() {
        if (regularBg != null)
            setBackground(regularBg);
        if (unhighlightAction != null)
            unhighlightAction.run();
    }
}
