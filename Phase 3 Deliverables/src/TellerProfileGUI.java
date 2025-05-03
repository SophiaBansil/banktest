package bankGUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * <h2>Teller Profile GUI (v1.3)</h2>
 * <p>Visual refresh that aligns the header bar with the dark‑green strip used
 * in {@link ATMProfileGUI}.  The header now displays «Bank Application» on the
 * left and «Teller: &lt;name&gt; | Bank Location: &lt;branch&gt;» on the right, all in
 * a solid BRAND_DARK bar.</p>
 */
public class TellerProfileGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    /* --- State ---------------------------------------------------------- */
    private final TellerProfileApplication app;

    /* Palette reused from ATMProfileGUI */
    private static final Color BRAND_DARK  = Color.decode("#00875A");
    private static final Color BRAND_LIGHT = Color.decode("#30C88B");

    /* --- Constructor ---------------------------------------------------- */
    public TellerProfileGUI(TellerProfileApplication app) {
        this.app = app;
        initLookAndFeel();
        initComponents();
    }

    /* --- Look & feel ---------------------------------------------------- */
    private void initLookAndFeel() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignored) {}
    }

    /* --- Components ----------------------------------------------------- */
    private void initComponents() {
        setTitle("Teller | Bank Application");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        /* Gradient background */
        setContentPane(new GradientPanel());
        getContentPane().setLayout(new BorderLayout());

        /* -----------------------------------------------------------------
         *  Header : solid dark‑green bar (mirrors ATMProfileGUI)           */
        JLabel titleLbl = new JLabel("Bank Application", SwingConstants.LEFT);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLbl.setForeground(Color.WHITE);

        String tellerInfoTxt = "Teller: " + app.getTellerName() + " | Bank Location: " + app.getBranch();
        JLabel tellerInfoLbl = new JLabel(tellerInfoTxt, SwingConstants.RIGHT);
        tellerInfoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tellerInfoLbl.setForeground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout(10,0));
        headerPanel.setOpaque(true);
        headerPanel.setBackground(BRAND_DARK);
        headerPanel.setPreferredSize(new Dimension(0, 45));
        headerPanel.setBorder(new EmptyBorder(0, 15, 0, 15));
        headerPanel.add(titleLbl,      BorderLayout.WEST);
        headerPanel.add(tellerInfoLbl, BorderLayout.EAST);

        getContentPane().add(headerPanel, BorderLayout.NORTH);

        /* -----------------------------------------------------------------
         *  Centre prompt + buttons                                         */
        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));

        JLabel q = new JLabel("What would you like to do?", SwingConstants.CENTER);
        q.setAlignmentX(Component.CENTER_ALIGNMENT);
        q.setFont(new Font("Segoe UI", Font.BOLD, 22));
        q.setBorder(new EmptyBorder(30,0,30,0));
        centre.add(q);

        /* Buttons */
        JButton searchBtn  = stylishButton("Search Client Profile");
        JButton createBtn  = stylishButton("Create New Client Profile");
        JButton logoutBtn  = stylishButton("Logout");

        searchBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Search – coming soon!"));
        createBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Create – coming soon!"));
        logoutBtn .addActionListener(e -> {
            dispose();
            LoginApplication loginApp = app.getLoginApp();
            if (loginApp != null) {
                LoginGUI loginGUI = new LoginGUI(loginApp);
                loginApp.setGUI(loginGUI);
                loginGUI.Login();
            }
        });

        /* Icon‑button rows */
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        row1.setOpaque(false);
        row1.add(buildCircleIcon("\uD83D\uDD0D"));
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

    /* --- Helpers -------------------------------------------------------- */
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

    /* --- Launch helpers ------------------------------------------------- */
    /** Call this from non‑Swing threads to show the GUI safely. */
    public void Login() { SwingUtilities.invokeLater(() -> setVisible(true)); }
    /** Deprecated alias kept for parity. */
    public void display() { Login(); }
}
