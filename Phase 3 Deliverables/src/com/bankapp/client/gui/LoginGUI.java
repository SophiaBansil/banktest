package com.bankapp.client.gui;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicToggleButtonUI;

import com.bankapp.client.ClientProfileApplication;
import com.bankapp.client.LoginApplication;
import com.bankapp.client.TellerApplication;
import com.bankapp.common.FailureMessage;
import com.bankapp.common.Message;
import com.bankapp.common.ProfileMessage;
import com.bankapp.common.SessionInfo;
import com.bankapp.common.SuccessMessage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter; 
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LoginGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    /* ─── State ────────────────────────────────────────────────────────── */
    private final LoginApplication loginApp;

    
    // --- UI Components made fields ---
    private JToggleButton tellerBtn, clientBtn;
    private JTextField empField, clientField;
    private JPasswordField empPass, clientPass;
    private JButton loginBtn, exitBtn;
    private JPanel cardsPanel; // Panel holding login forms
    private JLabel statusLabel; // Label for feedback

    /* ─── Constructor ──────────────────────────────────────────────────── */

    public LoginGUI(LoginApplication app) {
        this.loginApp = app;
        this.loginApp.setGUI(this);
        initLookAndFeel(); 
        initComponents(); 
    }

    /* ─── UI Setup ─────────────────────────────────────────────────────── */

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
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                int choice = JOptionPane.showConfirmDialog(LoginGUI.this, "Are you sure you want to exit the application?", "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    loginApp.exitApplication(); 
                }
        }});

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
            tb.addItemListener(e -> {
                JToggleButton source = (JToggleButton) e.getSource();
                source.setBackground(source.isSelected() ? toggleSel : toggleBg);
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String command = source.getActionCommand();
                    CardLayout cl = (CardLayout) (cardsPanel.getLayout());
                    cl.show(cardsPanel, command); 
                }
           });
        }
        tellerBtn.setActionCommand("Teller");
        clientBtn.setActionCommand("Client");

        JPanel togglePanel = new JPanel(new GridLayout(1,2,10,0));
        togglePanel.setOpaque(false);
        togglePanel.setBorder(new EmptyBorder(15,50,15,50));
        togglePanel.add(tellerBtn);
        togglePanel.add(clientBtn);

        // Forms
        cardsPanel = new JPanel(new CardLayout());
        cardsPanel.setOpaque(false);
        cardsPanel.add(buildForm(
                      "Employee Username:", empField = new JTextField(15),
                      "Password:",             empPass  = new JPasswordField(15)
                  ), "Teller");
        cardsPanel.add(buildForm(
                      "Bank Username:",     clientField = new JTextField(15),
                      "Password:",          clientPass  = new JPasswordField(15)
                  ), "Client");
        tellerBtn.addActionListener(e ->
            ((CardLayout)cardsPanel.getLayout()).show(cardsPanel, "Teller")
        );
        clientBtn.addActionListener(e ->
            ((CardLayout)cardsPanel.getLayout()).show(cardsPanel, "Client")
        );

        // Action buttons
        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.addActionListener(this::executeLoginWorker);
        JButton exitBtn = new JButton("Exit");
        exitBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        exitBtn.addActionListener(e -> loginApp.exitApplication());
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(loginBtn);
        btnPanel.add(exitBtn);

        getRootPane().setDefaultButton(loginBtn);

        // Assemble center
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(10,30,20,30));
        center.add(togglePanel,      BorderLayout.NORTH);
        center.add(cardsPanel,       BorderLayout.CENTER);
        center.add(btnPanel,         BorderLayout.SOUTH);
        getContentPane().add(center, BorderLayout.CENTER);
    }


    // Build 2-row form
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

    /* ─── Event Handling & Workers ─────────────────────────────────────── */

    // Called when you click “Login”
    private void executeLoginWorker(ActionEvent e) {
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
        setLoginInProgress(true);

        // call Swingworker
        LoginWorker worker = new LoginWorker(user, pass, isTeller);
        worker.execute();

        }

        /*~~~~~ Swingworker to handle login in background~~~~~~~~~~~~~~~~~~~~~~~~~*/
        private class LoginWorker extends SwingWorker<Message, Void> {
        private final String username;
        private final String password;
        private final boolean isTellerLogin;

            public LoginWorker(String username, String password, boolean isTellerLogin) {
                this.username = username;
                this.password = password;
                this.isTellerLogin = isTellerLogin;
            }

        @Override
        protected Message doInBackground() throws Exception {
            System.out.println("Starting login for: " + username); // Debug
            if (isTellerLogin) {
                return loginApp.TellerLogin(username, password);
            } else {
                return loginApp.ClientLogin(username, password);
            }
        }

        @Override
        protected void done() {
            System.out.println("Login worker finished."); 
            Message result = null;
            try {
                result = get(); 
                System.out.println("Worker result received: " + (result != null ? result.getType() : "null")); 
                 if (result != null) {
                    handleAuthResult(result);
                 } else {
                    showError("Login failed: No response received from the application.");
                    resetToLoginScreen();
                 }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Login worker interrupted: " + e.getMessage());
                showError("Login process was interrupted.");
                resetToLoginScreen(); 
            } catch (ExecutionException e) {
                System.err.println("[LoginGUI Error] Exception during login execution: " + e.getCause());
                e.getCause().printStackTrace();
                String errorMsg = "An error occurred during login: ";
                if (e.getCause() instanceof IOException) {
                     errorMsg = "Connection error during login. Please check server.";
                } else {
                     errorMsg += e.getCause().getMessage();
                }
                showError(errorMsg);
                resetToLoginScreen();
            } finally {
                 if (isVisible()) { 
                     setLoginInProgress(false);
                 }
            }
        }
    }

    /*~~~~~helper to handle server response~~~~~~~~~~~~~~~~~~~~~~~~*/
    public void handleAuthResult(Message msg) {
        if (msg instanceof SuccessMessage successMsg) {
            setVisible(false); 

            SessionInfo session = successMsg.getSession();
            if (session == null) {
                 showError("Received invalid session from server.");
                 resetToLoginScreen(); 
                 setVisible(true); 
                 return;
            }

            System.out.println("[LoginGUI] Login Success for " + session.getRole() + ": " + session.getUsername());

            /*if (session.getRole() == SessionInfo.ROLE.TELLER) {
                TellerApplication tellerApp = loginApp.getTellerApp();
                if (tellerApp != null) {
                     TellerProfileGUI tellerGui = new TellerProfileGUI(tellerApp); 
                     setVisible(false); 
                     //tellerGui.display();
                     dispose(); 
                } else {
                     showError("Teller application failed to initialize.");
                     resetToLoginScreen();
                     setVisible(true); 
                }

            } else if (session.getRole() == SessionInfo.ROLE.CLIENT) {
                ClientProfileApplication clientApp = loginApp.getClientProfileApp();
                if (clientApp != null) {
                    loadClientProfileAndShowAtmGui(clientApp);
                } else {
                    showError("Login successful but Client application failed to initialize.");
                    resetToLoginScreen();
                }

            } else {
                 showError("Login successful but received unknown user role from server.");
                 resetToLoginScreen();
                 setVisible(true);
            }*/

        } else if (msg instanceof FailureMessage failMsg) {
            // show error msg from centralserver
            JOptionPane.showMessageDialog(
                this,
                failMsg.getMessage(), 
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
            );
            resetToLoginScreen(); 
        } else {
             showError("Received unexpected or null response from server during login.");
             resetToLoginScreen();
        }
    }

   /*  private void loadClientProfileAndShowAtmGui(ClientProfileApplication clientApp) {
        statusLabel.setText("Login successful. Loading profile...");
        statusLabel.setForeground(Color.BLUE);
        setLoginInProgress(true);


        SwingWorker<Message, Void> profileLoader = new SwingWorker<>() {
            protected Message doInBackground() throws Exception {
                return clientApp.requestProfile();
            }
            protected void done() {
               try { 
                Message result = get();
                    if (result instanceof ProfileMessage profileData) {
                        System.out.println("[LoginGUI] Profile loaded successfully, launching ATM GUI."); 
                        ATMProfileGUI atmGui = new ATMProfileGUI(clientApp, profileData);
                        //atmGui.display(); 
                        LoginGUI.this.dispose();
                    } else {
                        // Failed to load profile (requestProfile returned null)
                        showError("Login successful, but failed to load your profile data from the server.");
                        resetToLoginScreen(); 
                        setVisible(true); 
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showError("Profile loading was interrupted.");
                    resetToLoginScreen(); setVisible(true);
                } catch (ExecutionException e) {
                    showError("Error loading profile");
                    resetToLoginScreen(); setVisible(true);
                } finally {
                     if (isVisible()) {
                          setLoginInProgress(false);
                     }
                }
            }
        
        };
        profileLoader.execute();
   }*/

    private void setLoginInProgress(boolean inProgress) {
   
        SwingUtilities.invokeLater(() -> {
            boolean enabled = !inProgress; 
            loginBtn.setEnabled(enabled);
            exitBtn.setEnabled(enabled); 
            clientBtn.setEnabled(enabled);
            empField.setEnabled(enabled);
            clientField.setEnabled(enabled);
            empPass.setEnabled(enabled);
            clientPass.setEnabled(enabled);

            if (inProgress) {
                statusLabel.setText("Attempting login, please wait...");
                statusLabel.setForeground(Color.BLUE);
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); 
            } else {
                setCursor(Cursor.getDefaultCursor()); 
            }
        });
    }

    public void resetToLoginScreen() {
        SwingUtilities.invokeLater(() -> {
             // clear password fields
             empPass.setText("");
             clientPass.setText("");
             if (clientBtn.isSelected()) {
                  clientField.requestFocusInWindow();
             } else {
                  empField.requestFocusInWindow();
             }
             statusLabel.setText("Login failed. Please try again.");
             statusLabel.setForeground(Color.RED);
             setLoginInProgress(false);
        });
   }


    public void showError(String msg) {
        JOptionPane.showMessageDialog(
            this, msg, "Error", JOptionPane.ERROR_MESSAGE
        );
    }

    public void Login() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
