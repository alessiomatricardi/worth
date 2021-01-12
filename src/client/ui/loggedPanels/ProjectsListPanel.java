package worth.client.ui.loggedPanels;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by alessiomatricardi on 08/01/21
 *
 * Pannello dove vengono visualizzati tutti i progetti
 */
public class ProjectsListPanel extends JPanel {

    public ProjectsListPanel() {
        this.setLayout(new BorderLayout());
    }

    public void setUI(List<JButton> buttons) {
        JPanel projectsPanel = new JPanel();
        projectsPanel.setLayout(new GridLayout(0, 2, 0, 0));

        // inserisco buttons nel pannello
        // ogni buttons ha altezza 100
        for (JButton button : buttons) {
            Font font = button.getFont();
            button.setFont(new Font(font.getName(), Font.PLAIN, (int)(font.getSize() * 1.5)));
            button.setPreferredSize(new Dimension(300, 100));
            projectsPanel.add(button);
        }

        // container contiene il pannello dei progetti
        JPanel container = new JPanel(new BorderLayout(0,0));
        container.add(projectsPanel, BorderLayout.NORTH);

        // layout scrollabile se necessario
        JScrollPane scrollPane = new JScrollPane(
                container,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        // aumenta velocità dello scorrimento
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // niente bordi
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.removeAll();
        this.add(scrollPane, BorderLayout.CENTER);
    }

}
