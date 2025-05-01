package bankGUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ATMProfileGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private final ATMProfileApplication clientApp;
    private final List<Account> accounts;
    private JComboBox<Account> accountCombo;
    private JLabel idLabel, balanceLabel, sharedLabel;

    public ATMProfileGUI(ATMProfileApplication app) {
        this.clientApp = app;
        // Retrieve the client's accounts from the application logic
        this.accounts = clientApp.getAccounts();
        initLookAndFeel();
        initComponents();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        Color bg = Color.decode("#e0fff6");

        setTitle("Client Profile");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        // Paint background
        getContentPane().setBackground(bg);
        getContentPane().setLayout(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel("Client Profile", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setOpaque(true);
        header.setBackground(bg);
        header.setBorder(new EmptyBorder(10, 0, 10, 0));
        getContentPane().add(header, BorderLayout.NORTH);

        // Left: account selector
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        leftPanel.add(new JLabel("Select Account:"), BorderLayout.NORTH);
        accountCombo = new JComboBox<>(accounts.toArray(new Account[0]));
        accountCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Account) {
                    setText(((Account) value).getID());
                }
                return this;
            }
        });
        accountCombo.addActionListener(e -> updateAccountInfo());
        leftPanel.add(accountCombo, BorderLayout.CENTER);
        getContentPane().add(leftPanel, BorderLayout.WEST);

        // Center: account info display
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setOpaque(false);
        idLabel = new JLabel();
        balanceLabel = new JLabel();
        sharedLabel = new JLabel();
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        balanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sharedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        infoPanel.add(idLabel);
        infoPanel.add(balanceLabel);
        infoPanel.add(sharedLabel);
        getContentPane().add(infoPanel, BorderLayout.CENTER);

        // Bottom: action buttons
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton historyBtn = new JButton("View Account History");
        JButton backBtn = new JButton("Back");
        JButton logoutBtn = new JButton("Log Out");

        depositBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Deposit clicked"));
        withdrawBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Withdraw clicked"));
        historyBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "History clicked"));
        backBtn.addActionListener(e -> {
            dispose();
            // Return to Login GUI
            LoginGUI login = new LoginGUI(clientApp.getLoginApp());
            login.Login();
        });
        logoutBtn.addActionListener(e -> System.exit(0));

        btnPanel.add(depositBtn);
        btnPanel.add(withdrawBtn);
        btnPanel.add(historyBtn);
        btnPanel.add(backBtn);
        btnPanel.add(logoutBtn);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        // Initialize display with first account
        if (!accounts.isEmpty()) {
            accountCombo.setSelectedIndex(0);
            updateAccountInfo();
        }
    }

    private void updateAccountInfo() {
        Account acc = (Account) accountCombo.getSelectedItem();
        if (acc == null) return;
        idLabel.setText("Account ID: " + acc.getID());
        balanceLabel.setText("Balance: " + acc.getBalance().toString());
        sharedLabel.setText("Shared Status: " + (acc.getTransHistory().size() > 1 ? "Shared" : "Single"));
    }

    public void Login() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
