package bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private final LoginApplication loginApp;
    private final CardLayout roleCards = new CardLayout();
    private final JPanel cards = new JPanel(roleCards);
    private JTextField empField, clientField;
    private JPasswordField empPass, clientPass;

    public LoginGUI(LoginApplication app) {
        this.loginApp = app;
        initLookAndFeel();
        initComponents();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        // The color we’ll use everywhere
        Color bg = Color.decode("#e0fff6");

        setTitle("Bank Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        // Paint the whole content pane
        getContentPane().setBackground(bg);

        // ===== Header Bar =====
        JLabel header = new JLabel("Welcome to Smith Banking!", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(Color.BLACK);
        header.setOpaque(true);
        header.setBackground(bg);
        header.setBorder(new EmptyBorder(20, 0, 20, 0));

        // ===== Role Toggle Buttons =====
        JToggleButton tellerBtn = new JToggleButton("Teller");
        JToggleButton clientBtn = new JToggleButton("Client");
        tellerBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clientBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ButtonGroup grp = new ButtonGroup();
        grp.add(tellerBtn);
        grp.add(clientBtn);
        tellerBtn.setSelected(true);

        // === Add custom styling for toggle buttons ===
        Color toggleBg       = Color.decode("#a1d6c7");
        Color toggleSelected = Color.decode("#8ebdb0");
        Color toggleFg       = Color.BLACK;

        // Force Nimbus to respect our custom colors
        tellerBtn.setUI(new BasicToggleButtonUI());
        clientBtn.setUI(new BasicToggleButtonUI());

        for (JToggleButton tb : new JToggleButton[]{tellerBtn, clientBtn}) {
            tb.setOpaque(true);
            tb.setContentAreaFilled(true);
            tb.setBorderPainted(false);
            tb.setBackground(toggleBg);
            tb.setForeground(toggleFg);
        }

        // Change background on selection state
        tellerBtn.addItemListener(e ->
            tellerBtn.setBackground(tellerBtn.isSelected() ? toggleSelected : toggleBg)
        );
        clientBtn.addItemListener(e ->
            clientBtn.setBackground(clientBtn.isSelected() ? toggleSelected : toggleBg)
        );

        JPanel togglePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        togglePanel.setOpaque(false);
        togglePanel.setBorder(new EmptyBorder(15, 50, 15, 50));
        togglePanel.add(tellerBtn);
        togglePanel.add(clientBtn);

        // ===== CardLayout Forms =====
        cards.setOpaque(false);
        cards.add(buildForm("Employee Username:", empField = new JTextField(15),
                            "Password:", empPass = new JPasswordField(15)),
                  "Teller");
        cards.add(buildForm("Bank Username:", clientField = new JTextField(15),
                            "Password:", clientPass = new JPasswordField(15)),
                  "Client");
        tellerBtn.addActionListener(e -> roleCards.show(cards, "Teller"));
        clientBtn.addActionListener(e -> roleCards.show(cards, "Client"));

        // ===== Buttons =====
        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.addActionListener(this::doLogin);

        JButton exitBtn = new JButton("Exit");
        exitBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Goodbye");
            System.exit(0);
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(loginBtn);
        btnPanel.add(exitBtn);

        // Pressing Enter triggers Login
        getRootPane().setDefaultButton(loginBtn);

        // ===== Center Container =====
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(10, 30, 20, 30));
        center.add(togglePanel, BorderLayout.NORTH);
        center.add(cards,       BorderLayout.CENTER);
        center.add(btnPanel,    BorderLayout.SOUTH);

        // ===== Assemble =====
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(center, BorderLayout.CENTER);
    }

    private JPanel buildForm(String lbl1, JTextField tf1,
                             String lbl2, JPasswordField pf2) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        p.add(new JLabel(lbl1), gbc);
        gbc.gridx = 1;
        p.add(tf1, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        p.add(new JLabel(lbl2), gbc);
        gbc.gridx = 1;
        p.add(pf2, gbc);
        return p;
    }

    private void doLogin(ActionEvent e) {
        boolean isTeller = cards.isVisible()
            && ((JToggleButton)((Container)cards.getParent())
               .getComponent(0)).isSelected();

        String user = isTeller
                      ? empField.getText().trim()
                      : clientField.getText().trim();
        String pass = new String(
                          isTeller
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

        if (isTeller) {
            loginApp.TellerLogin(user, pass);
        } else {
            loginApp.ClientLogin(user, pass);
        }
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void Login() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
