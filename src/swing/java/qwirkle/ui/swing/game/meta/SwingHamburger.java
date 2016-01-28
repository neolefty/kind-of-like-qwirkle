package qwirkle.ui.swing.game.meta;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleColor;
import qwirkle.ui.event.MetaGameMenuOpen;
import qwirkle.ui.swing.util.SwingKitty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Hamburger button that triggers the meta-game menu.
 *  Listen on the event bus for {@link MetaGameMenuOpen} events. */
public class SwingHamburger extends JPanel {
    // proportions of the hamburger: panel margin, thickness of each layer, and space between layers
    private static final double MARGIN = 1, LAYER = 2, BETWEEN = 1.25;
    private static final int N = 3; // number of layers

    public SwingHamburger(final EventBus bus) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                bus.post(new MetaGameMenuOpen());
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int height = getHeight(), width = getWidth();
        double sum = MARGIN * 2 + LAYER * N + BETWEEN * (N - 1);
        double ratio = height / sum;
        int layer = (int) (ratio * LAYER), between = (int) (ratio * BETWEEN),
            margin = (int) (ratio * MARGIN);

        int centerY = height / 2;
        // go up from the center N/2 layers and (N-1)/2 betweens.
        int startX = centerY - ((int) ((((double) N) / 2) * layer + (((double) N - 1) / 2) * between));
        for (int i = 0; i < N; ++i) {
            int top = startX + i * (layer + between);
            g2.fillRoundRect(margin, top, width - (margin * 2), layer, layer, layer);
        }
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Hamburger");
        EventBus bus = new EventBus();
        frame.setContentPane(new SwingHamburger(bus));
        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        SwingKitty.setColors(frame, QwirkleColor.GREY_3, QwirkleColor.BLACK);

        bus.register(new Object() {
            @Subscribe public void clicked(MetaGameMenuOpen event) {
                frame.setTitle("Clicked!");
                new Thread() {
                    @Override
                    public void run() {
                        try { Thread.sleep(1250); } catch (InterruptedException ignored) {}
                        frame.setTitle("Hamburger");
                    }
                }.start();
            }
        });
    }
}
