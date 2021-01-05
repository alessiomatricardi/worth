package worth.client.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 04/01/21
 */
public class LoggedUI extends JPanel {
    private static final int WIDTH = 1300; // larghezza del panel
    private static final int HEIGHT = 600; // altezza del panel

    public LoggedUI() {
        Dimension dim = new Dimension(WIDTH, HEIGHT);
        this.setPreferredSize(dim);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

}
