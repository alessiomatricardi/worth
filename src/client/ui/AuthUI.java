package worth.client.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class AuthUI extends JPanel {
    private static final int WIDTH = 400; // larghezza del panel
    private static final int HEIGHT = 160; // altezza del panel

    // componenti Java Swing
    private JPanel usernamePanel;
    private JPanel passwordPanel;
    private JPanel buttonsPanel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JTextField usernameTextField;
    private JTextField passwordTextField;
    private JButton registerButton;
    private JButton loginButton;

    public AuthUI() {
        Dimension dim = new Dimension(WIDTH, HEIGHT);
        this.setPreferredSize(dim);
        this.setLayout(new GridLayout(3, 1));

        usernamePanel = new JPanel();
        passwordPanel = new JPanel();
        buttonsPanel = new JPanel();
        this.add(usernamePanel);
        this.add(passwordPanel);
        this.add(buttonsPanel);

        initComponents();
    }

    private void initComponents() {
        usernameLabel = new JLabel("Username");
        passwordLabel = new JLabel("Password");
        usernameTextField = new JTextField();
        passwordTextField = new JTextField();
        registerButton = new JButton("Register");
        loginButton = new JButton("Login");

        usernamePanel.setLayout(new GridLayout(1,2, 10, 10));
        usernamePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        passwordPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameTextField);

        passwordPanel.setLayout(new GridLayout(1,2, 10, 10));
        passwordPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        passwordPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordTextField);

        buttonsPanel.setLayout(new GridLayout(1,2, 10, 10));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonsPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);
    }

    public JTextField getUsernameTextField() {
        return usernameTextField;
    }

    public JTextField getPasswordTextField() {
        return passwordTextField;
    }

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

}
