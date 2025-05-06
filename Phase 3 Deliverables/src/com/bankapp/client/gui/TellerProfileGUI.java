package com.bankapp.client.gui;


import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.bankapp.client.LoginApplication;
import com.bankapp.client.TellerApplication;
import com.bankapp.common.AccountMessage;
import com.bankapp.common.ProfileMessage;

import java.awt.*;


public class TellerProfileGUI extends JFrame {
    private static final long serialVersionUID = 1L;

   
    /*                     ───  State  ─────────────────────────────────── */
    
    private final TellerApplication app;
    private final ProfileMessage           profileMsg; // holds teller info (name, branch, etc.)

    /* Palette reused from ATMProfileGUI */
    private static final Color BRAND_DARK  = Color.decode("#00875A");
    private static final Color BRAND_LIGHT = Color.decode("#30C88B");

    
    /*                     ───  Constructors  ──────────────────────────── */
    

    public TellerProfileGUI(TellerApplication app, ProfileMessage msg) {
        this.app        = app;
        this.profileMsg = msg;
        initLookAndFeel();
        initComponents();
    }

    // Convenience overload for older call‑sites
    public TellerProfileGUI(TellerApplication app) {
        this(app, app.getProfile());
    }

    
    /*                     ───  Look & feel  ───────────────────────────── */
    
    private void initLookAndFeel() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignored) {}
    }

    
    /*                     ───  Components  ────────────────────────────── */
    
    private void initComponents() {
        setTitle("Teller | Bank Application");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        /* Gradient background */
        setContentPane(new GradientPanel());
        getContentPane().setLayout(new BorderLayout());

        /* Header bar -------------------------------------------------- */
        JLabel bankName = new JLabel("Bank Application", SwingConstants.LEFT);
        bankName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        bankName.setForeground(Color.WHITE);

        String tellerName = profileMsg != null ? profileMsg.getLegalName() : app.getTellerName();
        String branch     = profileMsg != null ? app.getBranch()      : app.getBranch();

        JLabel tellerInfo = new JLabel("Teller: " + tellerName + "  |  Bank Location: " + branch,
                                       SwingConstants.RIGHT);
        tellerInfo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tellerInfo.setForeground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout(10,0));
        header.setOpaque(true);
        header.setBackground(BRAND_DARK);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));
        header.add(bankName,  BorderLayout.WEST);
        header.add(tellerInfo, BorderLayout.EAST);
        getContentPane().add(header, BorderLayout.NORTH);

        /* Centre prompt --------------------------------------------- */
        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));

        JLabel q = new JLabel("What would you like to do?", SwingConstants.CENTER);
        q.setAlignmentX(Component.CENTER_ALIGNMENT);
        q.setFont(new Font("Segoe UI", Font.BOLD, 22));
        q.setBorder(new EmptyBorder(10,0,30,0));
        centre.add(q);

        /* Buttons */
        JButton searchBtn  = stylishButton("Search Client Profile");
        JButton createBtn  = stylishButton("Create New Client Profile");
        JButton logoutBtn  = stylishButton("Log Out");

        searchBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Search – coming soon!"));
        createBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Create – coming soon!"));
        logoutBtn .addActionListener(e -> {
            dispose();
            LoginApplication loginApp = app.getLoginApp();
            if (loginApp != null) {
                LoginGUI gui = new LoginGUI(loginApp);
                loginApp.setGUI(gui);
                gui.setVisible(true);
            }
        });

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        row1.setOpaque(false);
        row1.add(buildCircleIcon("\uD83D\uDD0D")); // magnifying glass
        row1.add(searchBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        row2.setOpaque(false);
        row2.add(buildCircleIcon("+"));
        row2.add(createBtn);

        centre.add(row1);
        centre.add(Box.createVerticalStrut(20));
        centre.add(row2);
        centre.add(Box.createVerticalStrut(40));
        centre.add(logoutBtn);

        getContentPane().add(centre, BorderLayout.CENTER);
    }

    
    /*                     ───  Placeholder actions  ───────────────────── */
    

    /** Displays the teller portal – alias kept for legacy calls. */
    public void Login() { SwingUtilities.invokeLater(() -> setVisible(true)); }

    // Updated stubs to use AccountMessage instead of Account
    private void editBankAccount(AccountMessage msg) {}
    private void createNewBankAccount(AccountMessage msg) {}
    private void createNewCreditLine(AccountMessage msg) {}
    private void findClientAccount(String clientID) {}
    private void createNewProfile(ProfileMessage profile) {}
    private void saveChanges() {}
    private void exit() { dispose(); }

    
    /*                     ───  Helpers  ───────────────────────────────── */
    
    private JButton stylishButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(BRAND_DARK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setPreferredSize(new Dimension(260, 55));
        btn.setBorder(new EmptyBorder(5, 15, 5, 15));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(BRAND_LIGHT); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(BRAND_DARK);  }
        });
        return btn;
    }

    private JLabel buildCircleIcon(String symbol) {
        JLabel lbl = new JLabel(symbol, SwingConstants.CENTER) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#99c6e2"));
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lbl.setPreferredSize(new Dimension(100,100));
        lbl.setOpaque(false);
        return lbl;
    }

    /* Soft gradient background */
    private static class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, BRAND_LIGHT, 0, getHeight(), Color.WHITE));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
