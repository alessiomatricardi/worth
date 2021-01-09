package worth.client.ui.loggedPanels;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 08/01/21
 */
public class HomePanel extends JPanel {
    private JLabel usernameLabel;
    private JTextField projectNameField;
    private JButton createProjectButton;

    public HomePanel() {
        this.setLayout(new GridLayout(2,1));
        this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // primo elemento del GridLayout
        JPanel firstPanel = new JPanel(new GridLayout(2,1));

        JPanel welcomePanel = new JPanel(new FlowLayout());
        usernameLabel = new JLabel("Welcome!");
        Font font = usernameLabel.getFont();
        int fontSize = font.getSize();
        usernameLabel.setFont(new Font(font.getName(), Font.PLAIN, fontSize * 5));
        welcomePanel.add(usernameLabel);
        firstPanel.add(welcomePanel);

        JPanel infoPanel = new JPanel(new FlowLayout());
        JLabel infoLabel = new JLabel("Create new Project");
        infoLabel.setFont(new Font(font.getName(), Font.PLAIN, fontSize * 4));
        infoPanel.add(infoLabel);
        firstPanel.add(infoPanel);

        // secondo elemento del GridLayout
        JPanel secondPanel = new JPanel(new GridLayout(2,1));

        JPanel projectNamePanel = new JPanel(new FlowLayout());
        JLabel projectNameLabel = new JLabel("Project name ");
        projectNameField = new JTextField();
        projectNameField.setPreferredSize(new Dimension(300, 40));
        projectNamePanel.add(projectNameLabel);
        projectNamePanel.add(projectNameField);
        secondPanel.add(projectNamePanel);

        JPanel createProjectPanel = new JPanel(new FlowLayout());
        createProjectButton = new JButton("Create project");
        createProjectButton.setPreferredSize(new Dimension(300, 40));
        createProjectPanel.add(createProjectButton);
        secondPanel.add(createProjectPanel);

        this.add(firstPanel);
        this.add(secondPanel);
    }

    public void setUsernameLabel(String username) {
        this.usernameLabel.setText("Welcome " + username + "!");
    }

    public JTextField getProjectNameField() {
        return projectNameField;
    }

    public JButton getCreateProjectButton() {
        return createProjectButton;
    }

}
