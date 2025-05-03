package bankGUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;


public class ATMProfileGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    
    /*                     ───  State  ─────────────────────────────────── */
    
    private final ClientProfileApplication profileApp;
    private final ProfileMessage           profileMsg;   // holds account summaries & personal info
    private final List<AccountSummary>     accounts;     // list shown on the left

    private JList<AccountSummary> accountList;
    private JLabel idLabel, balanceLabel, sharedLabel;

    /* Brand palette (green → light‑green) */
    private static final Color BRAND_DARK  = Color.decode("#00875A");
    private static final Color BRAND_LIGHT = Color.decode("#30C88B");

   
    /*                     ───  Constructor  ───────────────────────────── */

    
    public ATMProfileGUI(ClientProfileApplication app, ProfileMessage msg) {
        this.profileApp = app;
        this.profileMsg = msg;
        this.accounts   = msg.getSummaries();

        initLookAndFeel();
        initComponents();
    }
    
    public ATMProfileGUI(ClientProfileApplication app) {
        this(app, null);         
    }

   
    /*                     ───  UI Helpers  ────────────────────────────── */
  
    private void initLookAndFeel() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignored) {}
    }

    private void initComponents() {
        /* Frame setup (unchanged) */
        setTitle("ATM & Profile");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        /* Gradient background */
        setContentPane(new GradientPanel());
        getContentPane().setLayout(new BorderLayout(10, 10));

       
        /*  Header (personal info now pulled from ProfileMessage)         */
        
        String fullName = profileMsg.getLegalName();
        String phone    = profileMsg.getPhone();
        String username = profileMsg.getUsername();

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel idLabelTop = new JLabel("Bank ID: " + username, SwingConstants.CENTER);
        JLabel phoneLabel = new JLabel("Phone: "   + phone,    SwingConstants.CENTER);
        Font topFont = new Font("Segoe UI", Font.PLAIN, 16);
        idLabelTop.setFont(topFont);
        phoneLabel.setFont(topFont);
        topPanel.add(idLabelTop);
        topPanel.add(phoneLabel);

        JLabel title = new JLabel("Welcome, " + fullName, SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JButton logoutBtn = stylishButton("Logout");
        logoutBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        logoutBtn.addActionListener(e -> {
            dispose();
            if (profileApp != null) {
                LoginApplication loginApp = profileApp.getLoginApp();
                if (loginApp != null) {
                    LoginGUI gui = new LoginGUI(loginApp);
                    loginApp.setGUI(gui);
                    gui.setVisible(true);
                }
            }
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(true);
        headerPanel.setBackground(BRAND_DARK);
        headerPanel.setPreferredSize(new Dimension(0, 45));
        headerPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        headerPanel.add(title,     BorderLayout.CENTER);
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        JPanel overallTopPanel = new JPanel(new BorderLayout());
        overallTopPanel.setOpaque(false);
        overallTopPanel.add(headerPanel, BorderLayout.NORTH);
        overallTopPanel.add(topPanel,    BorderLayout.CENTER);
        getContentPane().add(overallTopPanel, BorderLayout.NORTH);

       
        /*  Left : AccountSummary list                                    */
        
        DefaultListModel<AccountSummary> model = new DefaultListModel<>();
        if (accounts != null) {
            for (AccountSummary a : accounts) model.addElement(a);
        }

        accountList = new JList<>(model);
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setFixedCellHeight(35);
        accountList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        accountList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object val,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
                if (val instanceof AccountSummary s) setText(s.getID());
                setBorder(new EmptyBorder(4, 10, 4, 10));
                return this;
            }
        });
        accountList.addListSelectionListener(e -> {
            AccountSummary sel = accountList.getSelectedValue();
            if (sel != null) {
                // Request a full AccountMessage for the selected account
                AccountMessage msg = profileApp.loadAccount(sel.getID()); // synchronous helper
                updateAccountInfo(msg);
            } else {
                updateAccountInfo(null);
            }
        });

        JScrollPane scroll = new JScrollPane(accountList);
        scroll.setPreferredSize(new Dimension(200, 0));
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BRAND_DARK));
        getContentPane().add(scroll, BorderLayout.WEST);

       
        /*  Center : account details + actions                            */
       
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPanel.setOpaque(false);
        idLabel      = stylisedLabel("Account ID:");
        balanceLabel = stylisedLabel("Balance:");
        sharedLabel  = stylisedLabel("Shared Status:");
        infoPanel.add(idLabel);
        infoPanel.add(balanceLabel);
        infoPanel.add(sharedLabel);

        JButton depositBtn  = stylishButton("Deposit");
        JButton withdrawBtn = stylishButton("Withdraw");
        JButton historyBtn  = stylishButton("History");
        // Wire these later once AccountMessage actions are in place
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
        center.add(btnPanel,  BorderLayout.SOUTH);

        JPanel card = new JPanel(new BorderLayout()) {
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

        /* Auto‑select first account */
        if (!accounts.isEmpty()) accountList.setSelectedIndex(0);
    }

   
    /*                     ───  Details refresh  ───────────────────────── */
    private void updateAccountInfo(AccountMessage msg) {
        if (msg == null) {
            idLabel.setText("Account ID: N/A");
            balanceLabel.setText("Balance: N/A");
            sharedLabel.setText("Shared: N/A");
            return;
        }
        idLabel.setText("Account ID: " + msg.getID());
        balanceLabel.setText("Balance: " + msg.getBalance());
        /* Shared logic: if this accountID appears in more than one profile summary */
        long owners = profileMsg.getSummaries().stream()
                .filter(s -> s.getID().equals(msg.getID()))
                .count();
        sharedLabel.setText("Shared: " + (owners > 1 ? "Yes" : "No"));
    }

    
    /*                     ───  Utility widgets  ───────────────────────── */
   
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
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setPreferredSize(new Dimension(110, 35));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(BRAND_LIGHT); }
            public void mouseExited (java.awt.event.MouseEvent evt) { btn.setBackground(BRAND_DARK);  }
        });
        return btn;
    }

    /* Gradient panel */
    private class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, BRAND_LIGHT, 0, getHeight(), Color.WHITE));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /* Launch helper */
    public void display() { SwingUtilities.invokeLater(() -> setVisible(true)); }
}
