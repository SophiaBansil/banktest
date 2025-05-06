// LoginGUI.java (Simplified)
import javax.swing.*;
import java.awt.*;

public class LoginGUI extends JFrame implements SessionListener {
    private static final long serialVersionUID = 1L;
	private JTextField userField = new JTextField();
    private JPasswordField passField = new JPasswordField();
    private JRadioButton tellerBtn = new JRadioButton("Teller");
    private JRadioButton clientBtn = new JRadioButton("Client");
    private JButton loginBtn = new JButton("Login");
    private LoginApplication app = new LoginApplication();

    public LoginGUI() {
        super("Login");
        app.setSessionListener(this);
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridLayout(5, 2));
        ButtonGroup group = new ButtonGroup();
        group.add(tellerBtn);
        group.add(clientBtn);
        tellerBtn.setSelected(true);

        add(new JLabel("Username:"));
        add(userField);
        add(new JLabel("Password:"));
        add(passField);
        add(tellerBtn);
        add(clientBtn);
        add(new JLabel());
        add(loginBtn);

        loginBtn.addActionListener(_ -> doLogin());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    private void doLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        boolean isTeller = tellerBtn.isSelected();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty");
            return;
        }

        loginBtn.setEnabled(false);
        app.login(user, pass, isTeller);
    }

    @Override
    public void onLoginSuccess(SessionInfo session, ConnectionHandler handler) {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            if (session.getRole() == SessionInfo.ROLE.TELLER) {
                //new TellerProfileGUI(session, handler);
            	System.out.println("Successfully logged in as teller");
            } else {
                //new ATMProfileGUI(handler, session).display();
            	System.out.println("Successfully logged in as client");
            }
            dispose();
        });
    }

    @Override
    public void onLoginFailure(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message);
            loginBtn.setEnabled(true);
        });
    }
    
    @Override
    public void onServerShutdown() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                "The server is shutting down.",
                "Shutdown",
                JOptionPane.WARNING_MESSAGE);
            dispose();
            System.exit(0);
        });
    }

    @Override
    public void onConnectionLost(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                message,
                "Connection Lost",
                JOptionPane.ERROR_MESSAGE);
            dispose();
            System.exit(1);
        });
    }
}
