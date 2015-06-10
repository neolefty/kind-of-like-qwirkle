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
        super(parent, "", fraction);
        this.highlightBg = highlightBg;
        this.highlightAction = highlightAction;
        this.unhighlightAction = unhighlightAction;
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { highlight(); }
            @Override public void mouseExited(MouseEvent e) { unhighlight(); }
        });
    }

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
