package bankGUI;

import javax.swing.*;
import java.awt.*;


public class TellerProfileGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private final TellerProfileApplication tellerApp;
    private final LoginApplication loginApp;
    private final SessionInfo session;

    public TellerProfileGUI(TellerProfileApplication app) {
        this.tellerApp = app;
        this.loginApp = app.getLoginApp();
        this.session = app.getSession();
        initUI();
    }

    private void initUI() {
        setTitle("Teller Profile");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        // Welcome label with the Teller's real name (from session)
        JLabel welcomeLabel = new JLabel("Welcome, " + session.getUsername() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Teller ID label
        JLabel idLabel = new JLabel("ID: " + session.getSessionID(), SwingConstants.CENTER);
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            // Navigate back to login screen
            LoginGUI login = new LoginGUI(loginApp);
            login.Login();
        });

        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.add(welcomeLabel);
        centerPanel.add(idLabel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(logoutBtn, BorderLayout.SOUTH);
    }

    public void Login() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
