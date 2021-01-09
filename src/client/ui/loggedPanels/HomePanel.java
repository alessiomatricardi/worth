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
        this.setLayout(new GridLayout(3,1));
        this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        usernameLabel = new JLabel("Welcome");
        Font font = usernameLabel.getFont();
        int fontSize = font.getSize();
        usernameLabel.setFont(new Font(font.getName(), Font.PLAIN, fontSize * 5));
        projectNameField = new JTextField();
        createProjectButton = new JButton("Create project");

        this.add(usernameLabel);
        this.add(projectNameField);
        this.add(createProjectButton);
    }

    public void setUsernameLabel(String username) {
        this.usernameLabel.setText("Welcome " + username + "!");
    }

    public JButton getCreateProjectButton() {
        return createProjectButton;
    }

}
