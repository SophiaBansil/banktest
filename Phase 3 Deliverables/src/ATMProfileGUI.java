import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * <h2>ATM + Profile GUI (v2 → v2.1)</h2>
 * <p>
 * Same UI as provided, *unchanged except* for a small **Logout** button in the
 * top‑right corner that returns the user to {@code LoginGUI}. No other logic or
 * styling has been modified.
 * </p>
 */
public class ATMProfileGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    /* --- State ------------------------------------------------------------ */
    private final ClientProfileApplication profileApp;
    private final ATMApplication clientApp;
    private final List<Account> accounts;
    private final String username;

    private JList<Account> accountList;
    private JLabel idLabel, balanceLabel, sharedLabel;

    /* Brand palette (green → light‑green) */
    private static final Color BRAND_DARK  = Color.decode("#00875A");
    private static final Color BRAND_LIGHT = Color.decode("#30C88B");

    /* --- Constructors ----------------------------------------------------- */

    /**
     * Signature expected by existing call‑sites: <code>new ATMProfileGUI(app, username)</code>.
     */
    public ATMProfileGUI(ClientProfileApplication app, String username) {
        this.profileApp = app;
        this.username   = username;
        this.accounts   = profileApp.getAccounts();

    public ATMProfileGUI(ATMApplication app) {
        this.clientApp = app;
        // Retrieve the client's accounts from the application logic
        this.accounts = clientApp.getAccounts();
        initLookAndFeel();
        initComponents();
    }

    /* --- UI helpers ------------------------------------------------------- */
    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        /* Frame setup */
        setTitle("ATM & Profile");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        /* Gradient background on the root content‑pane */
        setContentPane(new GradientPanel());
        getContentPane().setLayout(new BorderLayout(10, 10)); // Keep the main layout

        /* --- Top panel : user info -------------------------------------- */
        ClientProfile profile = Database.getInstance().getClientDatabase().get(username);
        String fullName = profile != null ? profile.getName()  : "User";
        String phone    = profile != null ? profile.getPhone() : "N/A";

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.setOpaque(false);
        // Reduced top padding, headerPanel will add its own
        topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        
        JLabel idLabelTop = new JLabel("Bank ID: " + username,  SwingConstants.CENTER);
        JLabel phoneLabel = new JLabel("Phone: "   + phone,     SwingConstants.CENTER);
        Font topFont = new Font("Segoe UI", Font.PLAIN, 16);
        
        idLabelTop.setFont(topFont);
        phoneLabel.setFont(topFont);

        
        topPanel.add(idLabelTop);
        topPanel.add(phoneLabel);
        // Don't add topPanel directly to contentPane yet

        /* --- Header + Logout --------------------------------------------- */
        JLabel title = new JLabel("Welcome, " + fullName, SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JButton logoutBtn = stylishButton("Logout");
        System.out.println("Logout button created"); // Keep for testing if needed
        // logoutBtn.setPreferredSize(new Dimension(110, 35)); // Already set in stylishButton
        logoutBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Optional: Adjust size if needed, but stylishButton sets a default
        // logoutBtn.setPreferredSize(new Dimension(90, 30)); // Removed redundancy

        logoutBtn.addActionListener(e -> {
            dispose();
            // --- Safely get LoginApp/GUI ---
            // Make sure profileApp and getLoginApp() are not null
            if (profileApp != null) {
                LoginApplication loginApp = profileApp.getLoginApp();
                if (loginApp != null) {
                    LoginGUI loginGUI = new LoginGUI(loginApp); // Create new instance
                    loginApp.setGUI(loginGUI); // Update reference in LoginApp
                    loginGUI.setVisible(true);
                } else {
                    System.err.println("Error: LoginApplication is null.");
                    // Optionally show an error dialog to the user
                }
            } else {
                 System.err.println("Error: ClientProfileApplication is null.");
                 // Optionally show an error dialog to the user
            }
            // --- End Safe Handling ---
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(true);
        headerPanel.setBackground(BRAND_DARK);
        headerPanel.setPreferredSize(new Dimension(0, 45)); // Height for the header bar
        // Add padding inside the header bar if desired
        headerPanel.setBorder(new EmptyBorder(0, 10, 0, 10)); // Example padding

        headerPanel.add(title,     BorderLayout.CENTER);
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        // Don't add headerPanel directly to contentPane yet


        /* --- Combine Top Panels --- */
        JPanel overallTopPanel = new JPanel(new BorderLayout());
        overallTopPanel.setOpaque(false); // So gradient background shows through
        // Add the header first to the NORTH (top) position
        overallTopPanel.add(headerPanel, BorderLayout.NORTH);
        // Add the user info panel below the header in the CENTER position
        overallTopPanel.add(topPanel, BorderLayout.CENTER);

        // Add the combined panel to the main content pane's NORTH
        getContentPane().add(overallTopPanel, BorderLayout.NORTH);


        /* --- Left : accounts list (unchanged) ----------------------------- */
        DefaultListModel<Account> model = new DefaultListModel<>();
        // Assuming Account class exists and is accessible
        // Assuming profileApp.getAccounts() returns List<Account>
        if (accounts != null) { // Add null check for safety
             for (Account acc : accounts) model.addElement(acc);
        }

        accountList = new JList<>(model);
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setFixedCellHeight(35);
        accountList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        accountList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L; // Added serialVersionUID
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Account acc) {
                    // Ensure Account has a getID() method returning String
                     setText(acc.getID());
                 } else {
                     setText(value != null ? value.toString() : ""); // Handle non-Account objects
                 }
                setBorder(new EmptyBorder(4, 10, 4, 10));
                return this;
            }
        });
        accountList.addListSelectionListener(e -> {
            // Ensure Account class exists and updateAccountInfo expects Account
            Object selected = accountList.getSelectedValue();
            if (selected instanceof Account) {
                 updateAccountInfo((Account) selected);
             }
         });


        JScrollPane scroll = new JScrollPane(accountList);
        scroll.setPreferredSize(new Dimension(200, 0));
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BRAND_DARK));
        getContentPane().add(scroll, BorderLayout.WEST);


        /* --- Centre : account details & actions (unchanged) --------------- */
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPanel.setOpaque(false);
        idLabel      = stylisedLabel("Account ID:");
        balanceLabel = stylisedLabel("Balance:");
        sharedLabel  = stylisedLabel("Shared Status:"); // Consider if this logic is correct

        infoPanel.add(idLabel);
        infoPanel.add(balanceLabel);
        infoPanel.add(sharedLabel);

        /* Buttons */
        JButton depositBtn  = stylishButton("Deposit");
        JButton withdrawBtn = stylishButton("Withdraw");
        JButton historyBtn  = stylishButton("History");

        depositBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Deposit popup"));
        withdrawBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Withdraw popup"));
        historyBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "History popup"));

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setOpaque(false);
        btnPanel.add(depositBtn);
        btnPanel.add(withdrawBtn);
        btnPanel.add(historyBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));
        center.add(infoPanel, BorderLayout.CENTER);
        center.add(btnPanel, BorderLayout.SOUTH);

        /* Rounded glass‑like card */
        JPanel card = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L; // Added serialVersionUID
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.add(center);

        JPanel cardHolder = new JPanel(new GridBagLayout());
        cardHolder.setOpaque(false);
        cardHolder.add(card);
        getContentPane().add(cardHolder, BorderLayout.CENTER);

        /* Pre‑select first account if there is one */
        if (accounts != null && !accounts.isEmpty()) { // Added null check
             accountList.setSelectedIndex(0);
         } else {
             // Optionally clear or disable the details view if no accounts
             updateAccountInfo(null); // Call with null to potentially clear labels
         }
    }

    // --- updateAccountInfo modification ---
    private void updateAccountInfo(Account acc) {
        if (acc == null) {
            // Clear labels or set default text if no account is selected
            idLabel.setText("Account ID: N/A");
            balanceLabel.setText("Balance: N/A");
            sharedLabel.setText("Shared: N/A");
            // Optionally disable Deposit/Withdraw/History buttons here
            return;
        }
        // Re-enable buttons if they were disabled
        idLabel.setText("Account ID: " + acc.getID());
        // Ensure Account has getBalance() and getTransHistory() methods
        balanceLabel.setText("Balance: " + String.format("%.2f", acc.getBalance())); // Format balance
        // Your 'Shared' logic might need refinement depending on what getTransHistory represents
        sharedLabel.setText("Shared: " + (acc.getTransHistory() != null && acc.getTransHistory().size() > 1 ? "Yes" : "No"));
    }


    /* --- Utility widgets (unchanged) -------------------------------------- */
    private JLabel stylisedLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return lbl;
    }

    private JButton stylishButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(BRAND_DARK);
        btn.setFocusPainted(false);
        // Keep border painted true if you add a border later (like for logout)
        // btn.setBorderPainted(false); // Can be removed if borders are sometimes used
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setPreferredSize(new Dimension(110, 35));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(BRAND_LIGHT); }
            public void mouseExited (java.awt.event.MouseEvent evt) { btn.setBackground(BRAND_DARK);  }
        });
        return btn;
    }


    /* Gradient panel for soft background (unchanged) */
    private class GradientPanel extends JPanel {
        private static final long serialVersionUID = 1L; // Added serialVersionUID
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, BRAND_LIGHT, 0, getHeight(), Color.WHITE));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /* --- Launch helper (unchanged) ---------------------------------------- */
    public void display() { SwingUtilities.invokeLater(() -> setVisible(true)); }
    private void updateAccountInfo() {
        Account acc = (Account) accountCombo.getSelectedItem();
        if (acc == null) return;
        idLabel.setText("Account ID: " + acc.getID());
        balanceLabel.setText("Balance: " + acc.getBalance().toString());
        sharedLabel.setText("Shared Status: " + (acc.getTransactionHistory().size() > 1 ? "Shared" : "Single"));
    }

   
} // End of ATMProfileGUI class