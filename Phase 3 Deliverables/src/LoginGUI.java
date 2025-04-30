import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private final LoginApplication loginApp;
    private JToggleButton tellerBtn, clientBtn;
    private JTextField empField, clientField;
    private JPasswordField empPass, clientPass;

    public LoginGUI(LoginApplication app) {
        this.loginApp = app;
        initLookAndFeel();
        initComponents();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(
                "javax.swing.plaf.nimbus.NimbusLookAndFeel"
            );
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        Color bg = Color.decode("#e0fff6");

        setTitle("Bank Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        // ----- Layout -----
        getContentPane().setBackground(bg);
        getContentPane().setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel(
            "Welcome to Smith Banking!",
            SwingConstants.CENTER
        );
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setOpaque(true);
        header.setBackground(bg);
        header.setBorder(new EmptyBorder(20, 0, 20, 0));
        getContentPane().add(header, BorderLayout.NORTH);

        // Toggle buttons
        tellerBtn = new JToggleButton("Teller");
        clientBtn = new JToggleButton("Client");
        tellerBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clientBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ButtonGroup grp = new ButtonGroup();
        grp.add(tellerBtn); grp.add(clientBtn);
        tellerBtn.setSelected(true);

        Color toggleBg  = Color.decode("#a1d6c7");
        Color toggleSel = Color.decode("#8ebdb0");
        Color toggleFg  = Color.BLACK;
        tellerBtn.setUI(new BasicToggleButtonUI());
        clientBtn.setUI(new BasicToggleButtonUI());
        for (JToggleButton tb : new JToggleButton[]{tellerBtn, clientBtn}) {
            tb.setOpaque(true);
            tb.setContentAreaFilled(true);
            tb.setBorderPainted(false);
            tb.setBackground(toggleBg);
            tb.setForeground(toggleFg);
            tb.addItemListener(e ->
                tb.setBackground(
                    tb.isSelected() ? toggleSel : toggleBg
                )
            );
        }

        JPanel togglePanel = new JPanel(new GridLayout(1,2,10,0));
        togglePanel.setOpaque(false);
        togglePanel.setBorder(new EmptyBorder(15,50,15,50));
        togglePanel.add(tellerBtn);
        togglePanel.add(clientBtn);

        // Forms
        JPanel cards = new JPanel(new CardLayout());
        cards.setOpaque(false);
        cards.add(buildForm(
                      "Employee Username:", empField = new JTextField(15),
                      "Password:",             empPass  = new JPasswordField(15)
                  ), "Teller");
        cards.add(buildForm(
                      "Bank Username:",     clientField = new JTextField(15),
                      "Password:",          clientPass  = new JPasswordField(15)
                  ), "Client");
        tellerBtn.addActionListener(e ->
            ((CardLayout)cards.getLayout()).show(cards, "Teller")
        );
        clientBtn.addActionListener(e ->
            ((CardLayout)cards.getLayout()).show(cards, "Client")
        );

        // Action buttons
        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.addActionListener(this::doLogin);
        JButton exitBtn = new JButton("Exit");
        exitBtn.addActionListener(e -> System.exit(0));
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(loginBtn);
        btnPanel.add(exitBtn);

        getRootPane().setDefaultButton(loginBtn);

        // Assemble center
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(10,30,20,30));
        center.add(togglePanel, BorderLayout.NORTH);
        center.add(cards,       BorderLayout.CENTER);
        center.add(btnPanel,    BorderLayout.SOUTH);
        getContentPane().add(center, BorderLayout.CENTER);
    }

    // Builds a two‐row form
    private JPanel buildForm(String lbl1, JTextField tf1,
                             String lbl2, JPasswordField pf2) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.gridx=0; gbc.gridy=0; gbc.anchor=GridBagConstraints.WEST;
        p.add(new JLabel(lbl1), gbc);
        gbc.gridx=1; p.add(tf1, gbc);
        gbc.gridx=0; gbc.gridy=1; p.add(new JLabel(lbl2), gbc);
        gbc.gridx=1; p.add(pf2, gbc);
        return p;
    }

    // Called when you click “Login”
    private void doLogin(ActionEvent e) {
        boolean isTeller = tellerBtn.isSelected();
        String user = (isTeller ? empField.getText()
                               : clientField.getText()).trim();
        String pass = new String(isTeller
                                 ? empPass.getPassword()
                                 : clientPass.getPassword()
                                ).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "User ID and Password cannot be empty.",
                "Login Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // send to server
        if (isTeller) {
            loginApp.TellerLogin(user, pass);
        } else {
            loginApp.ClientLogin(user, pass);
        }

        // optionally disable inputs until reply...
    }

    /**
     * This gets called by LoginApplication once the
     * ConnectionHandler sees SUCCESS or FAILURE.
     */
    public void handleAuthResult(Message msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.getType() == Message.TYPE.SUCCESS) {
                // hide login window
                setVisible(false);

                // cast to SuccessMessage
                SuccessMessage success = (SuccessMessage) msg;
                SessionInfo session = success.getSession();

                // launch the next GUI based on role
                if (session.getRole() == SessionInfo.ROLE.TELLER) {
                    TellerProfileApplication tApp =
                        new TellerProfileApplication(loginApp, session);
                    new TellerProfileGUI(tApp).Login();
                } else {
                    ATMProfileApplication cApp =
                        new ATMProfileApplication(loginApp, session);
                    new ATMProfileGUI(cApp).Login();
                }

            } else {
                // FAILURE
                FailureMessage fail = (FailureMessage) msg;
                JOptionPane.showMessageDialog(
                    this,
                    fail.getMessage(),         
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
                );
                
            }
        });
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(
            this, msg, "Error", JOptionPane.ERROR_MESSAGE
        );
    }

    /** Kick off the UI */
    public void Login() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
