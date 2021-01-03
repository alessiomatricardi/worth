package worth.client.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 02/01/21
 */
public class AuthUI {
    private final static String TITLE = "Worth";

    // componenti Java Swing
    private JFrame frame;
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
        frame = new JFrame(TITLE);
        frame.getContentPane().setLayout(new GridLayout(3, 1));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 180);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        usernamePanel = new JPanel();
        passwordPanel = new JPanel();
        buttonsPanel = new JPanel();
        frame.add(usernamePanel);
        frame.add(passwordPanel);
        frame.add(buttonsPanel);

        initComponents();

        frame.setVisible(true);
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

}
