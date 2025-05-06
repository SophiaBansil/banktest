import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicToggleButtonUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List; 


public class LoginGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    // --- Keep loginApp final ---
    private final LoginApplication loginApp;
    private JToggleButton tellerBtn, clientBtn;
    private JTextField empField, clientField;
    private JPasswordField empPass, clientPass;

    public LoginGUI(LoginApplication app) {
        // --- reference to LoginApplication ---
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
        tellerBtn.setBackground(toggleSel);

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
        String user = (isTeller ? empField.getText() : clientField.getText()).trim();
        String pass = new String(isTeller ? empPass.getPassword() : clientPass.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "User ID and Password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Disable button temporarily ---
        JButton loginButton = null;
        if (e.getSource() instanceof JButton) {
            loginButton = (JButton)e.getSource();
            loginButton.setEnabled(false);
        }

        try {
            if (isTeller) {
                // --- Original Teller Logic ---
                loginApp.TellerLogin(user, pass);
                
                JOptionPane.showMessageDialog(this, "Teller login function called.\n(GUI transition may depend on server response handling).", "Teller Login", JOptionPane.INFORMATION_MESSAGE);

            } else {
                // --- Client Logic: Assume success and proceed directly ---
                loginApp.ClientLogin(user, pass); 
               

                // Create NEW instances 
                ClientProfileApplication cApp = new ClientProfileApplication();
                ATMApplication atmApp = new ATMApplication();
                atmApp.setClientProfileApplication(cApp);

             

                // Request profile data using the new cApp instance
                cApp.requestProfile(); 

                // Retrieve data from the new cApp instance
                ClientProfile fetchedProfile = cApp.getProfile();
                List<AccountSummary> fetchedAccounts = cApp.getAccounts();

                // Basic check if data retrieval worked
                if (fetchedProfile == null || fetchedAccounts == null) {
                   showError("Failed to retrieve profile data after login attempt.");
                   // Re-enable button if login failed here
                   if (loginButton != null) loginButton.setEnabled(true);
                   return;
                }

                // Construct ProfileMessage (using a FAKE/PLACEHOLDER session)
                
                SessionInfo placeholderSession = new SessionInfo(user, SessionInfo.ROLE.CLIENT);

                ProfileMessage profileDataMsg = new ProfileMessage(
                    Message.TYPE.LOAD_PROFILE, 
                    placeholderSession,        
                    fetchedProfile.getUsername(),
                    "", // Password not needed
                    fetchedProfile.getPhone(),
                    "", // Address placeholder
                    fetchedProfile.getFirstName() + " " + fetchedProfile.getLastName(),
                    fetchedAccounts
                );

                // Create and display the ATMProfileGUI
                setVisible(false); 
                ATMProfileGUI gui = new ATMProfileGUI(cApp, atmApp, profileDataMsg);
                gui.display(); // Show the next screen

                // LoginGUI is now hidden
                return; // Exit doLogin 
            }
        } catch (Exception ex) {
            // Catch any unexpected exceptions during the process
            showError("An error occurred during login: " + ex.getMessage());
            // Print stack trace for detailed debugging if needed
             ex.printStackTrace();
        } finally {
             // --- Re-enable button only if LoginGUI is still visible ---
             if (loginButton != null && isVisible()) {
                 loginButton.setEnabled(true);
             }
        }
    }

    
    public void handleAuthResult(Message msg) {
        // This code will not run with the current LoginApplication structure.
        SwingUtilities.invokeLater(() -> {
            if (msg == null){
                 System.err.println("handleAuthResult received null message (but wasn't called)");
                 return;
             }

            if (msg.getType() == Message.TYPE.SUCCESS && msg instanceof SuccessMessage) {
                SuccessMessage success = (SuccessMessage) msg;
                SessionInfo session = success.getSession();
                 if (session == null) {
                      System.err.println("handleAuthResult received null session (but wasn't called)");
                      return;
                  }

                // Hide login window
                setVisible(false);

                // launch the next GUI based on role
                if (session.getRole() == SessionInfo.ROLE.TELLER) {
                    
                     System.out.println("handleAuthResult: Teller Login Success (Not Called)");
                } else { // CLIENT ROLE
                    
                     System.out.println("handleAuthResult: Client Login Success (Not Called)");
                }

            } else if (msg.getType() == Message.TYPE.FAILURE && msg instanceof FailureMessage) {
                // FAILURE
                FailureMessage fail = (FailureMessage) msg;
                JOptionPane.showMessageDialog(
                    this,
                    fail.getMessage(),
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
                );
                // Clear password field maybe
                 if (tellerBtn.isSelected()) empPass.setText(""); else clientPass.setText("");
            } else {
                 System.err.println("handleAuthResult received unexpected message type (but wasn't called): " + msg.getClass().getName());
                 JOptionPane.showMessageDialog(this, "Received unexpected server response.", "Login Error", JOptionPane.WARNING_MESSAGE);
             }
        });
    }

    // --- showError and Login methods unchanged ---
    public void showError(String msg) {
        JOptionPane.showMessageDialog(
            this, msg, "Error", JOptionPane.ERROR_MESSAGE
        );
    }

    /** Start off the UI */
    public void Login() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }}
