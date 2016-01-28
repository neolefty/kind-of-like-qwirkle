package qwirkle.ui.swing.util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/** Create a JPanel with a CardLayout which switches to another JPanel on hover.
 *  @author <a href="https://github.com/JamesMcMinn/EditableJLabel">James McMinn</a>
 *  @author Bill Baker*/
public class EditableJLabel extends JPanel {
    private JLabel label;
    private boolean editOnHover = false;
    private JTextField textField;
    private LinkedList<TextChangeListener> listeners = new LinkedList<>();

    private static final String HOVER = "edit", NORMAL = "static";

    /** A listener for the EditableJLabel. Called when the value of the JLabel is updated. */
    public interface TextChangeListener {
        void textChanged(String value, EditableJLabel source);
    }

    /** Create the new panel */
    public EditableJLabel(String startText) {
        super();

        // Create the listener and the layout
        CardLayout layout = new CardLayout(0, 0);
        this.setLayout(layout);
        EditableListener hl = new EditableListener();

        // Create the JPanel for the "normal" state
        JPanel labelPanel = new JPanel(new GridLayout(1, 1));
        label = new JLabel(startText);
        labelPanel.add(label);

        // Create the JPanel for the "hover state"
        JPanel inputPanel = new JPanel(new GridLayout(1, 1));
        textField = new JTextField(startText);
        textField.addMouseListener(hl);
        textField.addKeyListener(hl);
        textField.addFocusListener(hl);
        inputPanel.add(textField);

        this.addMouseListener(hl);

        // Set the states
        this.add(labelPanel, NORMAL);
        this.add(inputPanel, HOVER);

        // Show the correct panel to begin with
        layout.show(this, NORMAL);
    }

    public void setText(String text) {
        label.setText(text);
        textField.setText(text);
    }

    public String getText() {
        return label.getText();
    }

    public void setEditable(boolean editable) {
        getCardLayout().show(this, editable ? HOVER : NORMAL);
    }

    private CardLayout getCardLayout() { return (CardLayout) getLayout(); }

    /** Should this become editable on hover (true) or on click (false, default)? */
    public void setEditOnHover(boolean editOnHover) {
        this.editOnHover = editOnHover;
    }

    /** Should this become editable on hover (true) or on click (false, default)? */
    public boolean isEditOnHover() { return editOnHover; }

    /** Listen for value changes. */
    public void addValueChangedListener(TextChangeListener l) { this.listeners.add(l); }

    /** Listen for nearly everything happening */
    private class EditableListener implements MouseListener, KeyListener, FocusListener {
        boolean locked = false;
        String oldValue;

        /** Lock to the text field while we have focus */
        @Override
        public void focusGained(FocusEvent arg0) {
            locked = true;
            oldValue = textField.getText();
        }

        /** Release the lock so that we can go back to a JLabel */
        public void release() {
            this.locked = false;
            setEditable(false);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (isEditOnHover())
                setEditable(true);
        }

        @Override public void mouseClicked(final MouseEvent e) {
            if (!isEditOnHover()) {
                setEditable(true);
                textField.requestFocus();
                textField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        textField.setSelectionStart(0);
                        textField.setSelectionEnd(textField.getText().length());
                        textField.removeFocusListener(this);
                    }
                });
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!locked && isEditOnHover())
                setEditable(false);
        }

        /** Update the text when focus is lost and release the lock */
        @Override
        public void focusLost(FocusEvent e) {
            setText(textField.getText());
            for (TextChangeListener v : listeners) {
                v.textChanged(textField.getText(), EditableJLabel.this);
            }
            release();
        }

        /** Check for key presses. We're only interested in Enter (save the value
         *  of the field) and Escape (reset the field to its previous value) */
        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                setText(textField.getText());
                for (TextChangeListener v : listeners) {
                    v.textChanged(textField.getText(), EditableJLabel.this);
                }
                release();
            } else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                setText(oldValue);
                release();
            }
        }

        // We don't need anything below this point in the Listener Class
        @Override public void mousePressed(MouseEvent e) { }
        @Override public void mouseReleased(MouseEvent e) { }
        @Override public void keyPressed(KeyEvent e) { }
        @Override public void keyReleased(KeyEvent e) { }
    }

    public static class Demo extends JFrame {
        public Demo() {
            super("EditableJLabel Example");
            Container content = getContentPane();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            // Create the EditableJLabel
            final EditableJLabel label = new EditableJLabel("Meow");

            label.setPreferredSize(new Dimension(150,24));

            final JRadioButton hoverRadio = new JRadioButton("hover"),
                    clickRadio = new JRadioButton("click");
            ButtonGroup radios = new ButtonGroup();
            radios.add(hoverRadio);
            radios.add(clickRadio);
            hoverRadio.setSelected(label.isEditOnHover());
            clickRadio.setSelected(!label.isEditOnHover());
            hoverRadio.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    label.setEditOnHover(hoverRadio.isSelected());
                }
            });
            JPanel radioPanel = new JPanel();
            radioPanel.add(new JLabel("Edit on "));
            radioPanel.add(hoverRadio);
            radioPanel.add(clickRadio);

            // Create and dd a listener for changes in the value of EditableJLabel
            TextChangeListener valueListener = new TextChangeListener() {
                @Override
                public void textChanged(String value, EditableJLabel source) {
                    setTitle(source.getText());
                }
            };
            label.addValueChangedListener(valueListener);
            label.setEditable(false);

            // Add the EditableJLabel to the content pane
            content.add(label);
            content.add(radioPanel);

            pack();
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setVisible(true);
        }
    }

    public static void main(String[] args) {
        new Demo();
    }
}
