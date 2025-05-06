package com.bankapp.client.gui;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.bankapp.client.ClientProfileApplication;
import com.bankapp.client.LoginApplication;
import com.bankapp.client.ATMApplication;
import com.bankapp.common.Account;
import com.bankapp.common.AccountMessage;
import com.bankapp.common.AccountSummary;
import com.bankapp.common.FailureMessage;
import com.bankapp.common.Message;
import com.bankapp.common.ProfileMessage;
import com.bankapp.common.SuccessMessage;
import com.bankapp.common.Transaction;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode; // Import for formatting currency
import java.util.List;
import java.util.concurrent.ExecutionException;


public class ATMProfileGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    /* ─── State ────────────────────────────────────────────────────────── */
    private final ClientProfileApplication profileApp;
    private final ATMApplication           atmApp;
    private final ProfileMessage           profileMsg;
    // Make accounts final if it's only set in constructor
    private final List<AccountSummary>     accounts;

    private JList<AccountSummary> accountList;
    private JLabel idLabel, balanceLabel, sharedLabel;

    /** Keeps the live balance of the account shown in the details pane */
    private BigDecimal currentBalance = BigDecimal.ZERO;
    private String     currentAccountID = null;

    /* Brand palette */
    private static final Color BRAND_DARK  = Color.decode("#00875A");
    private static final Color BRAND_LIGHT = Color.decode("#30C88B");

    private JButton depositBtn;
    private JButton withdrawBtn;
    private JButton historyBtn;
    private JButton logoutBtn;

    /* ─── Constructor ──────────────────────────────────────────────────── */
    public ATMProfileGUI(ClientProfileApplication profileApp,
                         ATMApplication atmApp,
                         ProfileMessage msg) {
        // Null checks are good practice
        if (profileApp == null || atmApp == null || msg == null) {
            throw new IllegalArgumentException("Application components and message cannot be null.");
        }
        this.profileApp = profileApp;
        this.atmApp     = atmApp;
        this.profileMsg = msg;
        this.accounts   = msg.getSummaries(); // Can still be null if ProfileMessage allows it

        if (this.accounts == null) {
             System.err.println("Warning: ProfileMessage contained null account summaries.");
             
        }


        initLookAndFeel();
        initComponents();
    }

    /* ─── UI Setup ─────────────────────────────────────────────────────── */
    private void initLookAndFeel() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignored) {}
    }

    private void initComponents() {
        setTitle("ATM & Profile");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        setContentPane(new GradientPanel());
        getContentPane().setLayout(new BorderLayout(10, 10));

        /* Header (personal info) */
        // Add null checks for profileMsg data
        String fullName = profileMsg.getLegalName() != null ? profileMsg.getLegalName() : "N/A";
        String phone    = profileMsg.getPhone() != null ? profileMsg.getPhone() : "N/A";
        String username = profileMsg.getUsername() != null ? profileMsg.getUsername() : "N/A";


        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel idLabelTop = new JLabel("Bank ID: " + username, SwingConstants.CENTER);
        JLabel phoneLabel = new JLabel("Phone: " + phone, SwingConstants.CENTER);
        Font topFont = new Font("Segoe UI", Font.PLAIN, 16);
        idLabelTop.setFont(topFont);
        phoneLabel.setFont(topFont);
        topPanel.add(idLabelTop);
        topPanel.add(phoneLabel);

        JLabel title = new JLabel("Welcome, " + fullName, SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        logoutBtn = stylishButton("Logout");
        logoutBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        logoutBtn.addActionListener(e -> handleLogout()); // Extracted logout logic

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

        /* Left : AccountSummary list */
        DefaultListModel<AccountSummary> model = new DefaultListModel<>();
        // Check if accounts is not null before iterating
        if (accounts != null) {
            for (AccountSummary a : accounts) {
                 
                 if (a != null) {
                     model.addElement(a);
                 }
            }
        } else {
             // Handle case where accounts list was null from the start
             // Maybe add a placeholder item or disable the list
             // For now, it will just be empty.
         }


        accountList = new JList<>(model);
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setFixedCellHeight(35);
        accountList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        accountList.setCellRenderer(new DefaultListCellRenderer() {
             private static final long serialVersionUID = 1L; // Added serialVersionUID
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object val, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
                 if (val instanceof AccountSummary s) {
                     label.setText(s.getID() != null ? s.getID() : "Invalid Account"); 
                 } else if (val != null) {
                     label.setText(val.toString()); 
                 } else {
                      label.setText(""); 
                  }
                label.setBorder(new EmptyBorder(4, 10, 4, 10));
                return label;
            }
        });
        accountList.addListSelectionListener(e -> {
             if (!e.getValueIsAdjusting()) { 
                 handleAccountSelection(); 
             }
         });


        JScrollPane scroll = new JScrollPane(accountList);
        scroll.setPreferredSize(new Dimension(200, 0));
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BRAND_DARK));
        getContentPane().add(scroll, BorderLayout.WEST);

        /* Centre : account details + actions */
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setOpaque(false);
        idLabel      = stylisedLabel("Account ID:");
        balanceLabel = stylisedLabel("Balance:");
        sharedLabel  = stylisedLabel("Shared Status:");
        infoPanel.add(idLabel);
        infoPanel.add(balanceLabel);
        infoPanel.add(sharedLabel);

        depositBtn  = stylishButton("Deposit");
        withdrawBtn = stylishButton("Withdraw");
        historyBtn  = stylishButton("History");

        depositBtn.addActionListener(e -> handleDeposit()); // Keep existing handlers
        withdrawBtn.addActionListener(e -> handleWithdraw());
        historyBtn.addActionListener(e -> handleHistory()); // Extracted history logic

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

        // Select first account only if the list is not empty
        if (!model.isEmpty()) {
             accountList.setSelectedIndex(0);
         } else {
             // No accounts, clear the details panel
             updateAccountInfo(null);
         }
    }

    // --- Extracted Logic for Account Selection ---
    // Inside ATMProfileGUI.java -> handleAccountSelection()

    private void handleAccountSelection() {
         AccountSummary selectedSummary = accountList.getSelectedValue();
          // Add check here too:
          if (selectedSummary == null) {
              System.out.println("!!! handleAccountSelection: selectedSummary is NULL!");
              updateAccountInfo(null);
              currentAccountID = null;
              return;
          }
          if (selectedSummary.getID() == null) {
               System.out.println("!!! handleAccountSelection: selectedSummary ID is NULL!");
               updateAccountInfo(null);
               currentAccountID = null;
               return;
          }


        currentAccountID = selectedSummary.getID();
        new SwingWorker<Message,Void>() {
        @Override
        protected Message doInBackground() {
            return profileApp.selectAccount(currentAccountID);
        }

         protected void done() {
            try {
                Message m = get();
                if (m instanceof AccountMessage am) {
                    updateAccountInfo(am);
                } else if (m instanceof FailureMessage fm) {
             
                    JOptionPane.showMessageDialog(
                        ATMProfileGUI.this,
                        fm.getMessage(),
                        "Load Account Failed",
                        JOptionPane.ERROR_MESSAGE
                    );
                    updateAccountInfo(null);
                    currentAccountID = null;
                } else {
                    JOptionPane.showMessageDialog(
                        ATMProfileGUI.this,
                        "Unexpected response: " + m.getClass().getSimpleName(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    updateAccountInfo(null);
                    currentAccountID = null;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    ATMProfileGUI.this,
                    "Error loading account: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                updateAccountInfo(null);
                currentAccountID = null;
            } finally {
                accountList.setEnabled(true);
            }
        }
    }.execute();
       

          // DEBUGGING
        /*   System.out.println("\n--- Debug handleAccountSelection ---");
          System.out.println("Selected Summary ID: " + selectedSummary.getID());
          System.out.println("currentAccountID variable: " + currentAccountID);
          System.out.println("atmApp.getAccount() result (loadedAcct): " + (loadedAcct == null ? "NULL" : "Account Object"));
          if (loadedAcct != null) {
              System.out.println("loadedAcct.getID(): " + loadedAcct.getID());
              System.out.println("loadedAcct.getBalance(): " + loadedAcct.getBalance()); // Should be BigDecimal
          }
          // --- END DEBUGGING ---


          if (loadedAcct != null && loadedAcct.getID() != null && loadedAcct.getID().equals(currentAccountID)) {
              AccountMessage displayMsg = new AccountMessage(
                      Message.TYPE.LOAD_ACCOUNT,
                      profileApp.getSession(),
                      loadedAcct.getID(),
                      loadedAcct.getBalance(), // Pass BigDecimal
                      loadedAcct.getTransactionHistory()
              );

              // --- More Debugging ---
               System.out.println("Condition TRUE: Calling updateAccountInfo with data.");
               System.out.println("AccountMessage Balance created: " + displayMsg.getBalance()); // Check conversion back
              // --- End Debugging ---

              updateAccountInfo(displayMsg);

          } else {
              // --- More Debugging ---
               System.out.println("Condition FALSE: Calling updateAccountInfo(null).");
               if (loadedAcct == null) System.out.println("Reason: loadedAcct was null.");
               else if (loadedAcct.getID() == null) System.out.println("Reason: loadedAcct ID was null.");
               else if (!loadedAcct.getID().equals(currentAccountID)) System.out.println("Reason: ID mismatch ('" + loadedAcct.getID() + "' != '" + currentAccountID + "')");
               else System.out.println("Reason: Unknown failure in condition check.");
              // --- End Debugging ---

              updateAccountInfo(null);
              currentAccountID = null;
          }*/
      }

    /* ─── Details refresh ──────────────────────────────────────────────── */
    /** Formats BigDecimal */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "N/A";
        
        return String.format("$%,.2f", amount);
    }

    /** Updates the center panel with account details */
     private void updateAccountInfo(AccountMessage msg) {
        if (msg == null) {
            idLabel.setText("Account ID: N/A");
            balanceLabel.setText("Balance: N/A");
            sharedLabel.setText("Shared: N/A");
            currentBalance = BigDecimal.ZERO;
            currentAccountID = null; // make sure currentAccountID is cleared
        } else {
            idLabel.setText("Account ID: " + msg.getID());
            currentBalance = msg.getBalance(); // Store the precise balance
            // Use formatter for display
            balanceLabel.setText("Balance: " + formatCurrency(currentBalance));

            // Recalculate shared status based on summaries list
            long owners = 0;
            if (accounts != null) { // Check if accounts list exists
                 owners = accounts.stream()
                       .filter(s -> s != null && s.getID() != null && s.getID().equals(msg.getID()))
                       .count();
            }
            sharedLabel.setText("Shared: " + (owners > 1 ? "Yes" : "No"));

            // Keep currentAccountID consistent
            currentAccountID = msg.getID();
        }
    }


    /* ─── Handlers ─────────────────────────────────────────────────────── */

    // --- MODIFIED: handleDeposit (Removed initial check) ---
    private void handleDeposit() {
        // Removed: Check if currentAccountID == null

        String input = JOptionPane.showInputDialog(this, "Enter amount to deposit:", "Deposit", JOptionPane.PLAIN_MESSAGE);

        // User cancelled
        if (input == null) {
            return;
        }

        // Basic validation - ATMApp handles more detailed validation
        if (input.trim().isEmpty()) {
             JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
             return;
         }

        // Validate using ATMApp's logic before confirming
        BigDecimal amountToDeposit;
        try {
            amountToDeposit = new BigDecimal(input);
            if (amountToDeposit.compareTo(BigDecimal.ZERO) <= 0) {
                 JOptionPane.showMessageDialog(this, "Deposit amount must be positive.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                 return;
             }
            
        } catch (NumberFormatException nfe) {
             JOptionPane.showMessageDialog(this, "Invalid number format entered.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // Confirmation 
        int confirmation = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to deposit " + formatCurrency(amountToDeposit) + "?",
            "Confirm Deposit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            // Perform the deposit using ATMApplication
        new SwingWorker<Message,Void>() {
            @Override
            protected Message doInBackground() throws Exception {
                return atmApp.deposit(input);
            }

            @Override
            protected void done() {
                depositBtn.setEnabled(true);
                try {
                    Message msg = get();  // get() returns the Message from doInBackground()
                    if (msg instanceof AccountMessage am) {
                        updateAccountInfo(am);
                        JOptionPane.showMessageDialog(
                        ATMProfileGUI.this,
                        "Successfully deposited " + formatCurrency(amountToDeposit) + ".",
                         "Deposit Successful",
                        JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        String failure = (msg instanceof FailureMessage fm)
                        ? fm.getMessage()
                        : "Unknown error during deposit.";
                        JOptionPane.showMessageDialog(
                        ATMProfileGUI.this,
                        failure,
                        "Deposit Failed",
                        JOptionPane.ERROR_MESSAGE
                        );
                    }
            } catch (InterruptedException | ExecutionException ex) {
                JOptionPane.showMessageDialog(
                  ATMProfileGUI.this,
                  "Error: " + ex.getMessage(),
                  "Error",  
                  JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }.execute();}


        // If NO_OPTION or dialog closed, do nothing.
    }

    // --- MODIFIED: handleWithdraw (Removed initial check) ---
    private void handleWithdraw() {
        // Removed: Check if currentAccountID == null

        String input = JOptionPane.showInputDialog(this, "Enter amount to withdraw:", "Withdrawal", JOptionPane.PLAIN_MESSAGE);

        // User cancelled
        if (input == null) {
            return;
        }

        // Basic validation (non-empty)
         if (input.trim().isEmpty()) {
              JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
              return;
          }

        // Validate using ATMApp's logic before confirming
         BigDecimal amountToWithdraw;
         try {
             amountToWithdraw = new BigDecimal(input);
             if (amountToWithdraw.compareTo(BigDecimal.ZERO) <= 0) {
                  JOptionPane.showMessageDialog(this, "Withdrawal amount must be positive.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                  return;
              }
             // Check against current balance display - provides faster feedback
             // Need to check if currentBalance is valid (not zero if no account selected)
             
             if (currentBalance != null && amountToWithdraw.compareTo(currentBalance) > 0) {
                  JOptionPane.showMessageDialog(this, "Withdrawal amount exceeds current balance.", "Insufficient Funds", JOptionPane.ERROR_MESSAGE);
                  return;
              }
         } catch (NumberFormatException nfe) {
              JOptionPane.showMessageDialog(this, "Invalid number format entered.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
             return;
         }


        // Confirmation Dialog
        int confirmation = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to withdraw " + formatCurrency(amountToWithdraw) + "?",
            "Confirm Withdrawal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            // Perform the withdrawal using ATMApplication
            new SwingWorker<Message,Void>() {
                @Override
                protected Message doInBackground() throws Exception {
                    return atmApp.withdraw(input);
                }
    
                @Override
                protected void done() {
                    withdrawBtn.setEnabled(true);
                    try {
                        Message msg = get();  // get() returns the Message from doInBackground()
                        if (msg instanceof AccountMessage am) {
                            updateAccountInfo(am);
                            JOptionPane.showMessageDialog(
                            ATMProfileGUI.this,
                            "Successfully deposited " + formatCurrency(amountToWithdraw) + ".",
                             "Deposit Successful",
                            JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            String failure = (msg instanceof FailureMessage fm)
                            ? fm.getMessage()
                            : "Unknown error during withdraw.";
                            JOptionPane.showMessageDialog(
                            ATMProfileGUI.this,
                            failure,
                            "Withdraw Failed",
                            JOptionPane.ERROR_MESSAGE
                            );
                        }
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(
                      ATMProfileGUI.this,
                      "Error: " + ex.getMessage(),
                      "Error",  
                      JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();}
    
        // If NO_OPTION or dialog closed, do nothing.
    }

     // --- Extracted History Handling ---
     private void handleHistory() {
        
        historyBtn.setEnabled(false);
        
         JOptionPane.showMessageDialog(this, "Transaction history printed to console.", "History", JOptionPane.INFORMATION_MESSAGE);
        new SwingWorker<Message,Void>() {
            @Override
            protected Message doInBackground() {
                return atmApp.loadTransactionHistory();
            }

            @Override
            protected void done() {
                historyBtn.setEnabled(true);
                try {
                    Message m = get();
                    if (m instanceof AccountMessage am) {
            
                        List<Transaction> history = am.getTransactionHistory();
                        history.forEach(System.out::println);
                        JOptionPane.showMessageDialog(
                            ATMProfileGUI.this,
                            "Transaction history printed to console.",
                            "History",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else if (m instanceof FailureMessage fm) {
                        // Server told us there was no history or some other error
                        JOptionPane.showMessageDialog(
                            ATMProfileGUI.this,
                            fm.getMessage(),
                            "History Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            ATMProfileGUI.this,
                            "Unexpected response: " + m.getClass().getSimpleName(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(
                        ATMProfileGUI.this,
                        "Failed to load history: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
     }

     // --- Extracted Logout Handling ---
     private void handleLogout() {
        // Confirmation dialog for logout
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            logoutBtn.setEnabled(false);
        }

             new SwingWorker<Message,Void>() {
        @Override
        protected Message doInBackground() {
            Message result = new SuccessMessage("Logged out");
            // Tell the ATM side first (if you need to)
            if (atmApp != null) {
                result = atmApp.exit();
            }
            // Then tell the profile side
            if (profileApp != null) {
                result = profileApp.exit();
            }
            return result;
        }

        @Override
        protected void done() {
            try {
                Message resp = get();
                if (resp instanceof FailureMessage fm) {
                    JOptionPane.showMessageDialog(
                        ATMProfileGUI.this,
                        fm.getMessage(),
                        "Logout Failed",
                        JOptionPane.ERROR_MESSAGE
                    );
                    logoutBtn.setEnabled(true);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    ATMProfileGUI.this,
                    "Error during logout: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                logoutBtn.setEnabled(true);
                return;
            }

            //  spin up a fresh Login GUI
            dispose();
            LoginApplication loginApp = new LoginApplication();
            LoginGUI loginGui = new LoginGUI(loginApp);
            loginApp.setGUI(loginGui);
            loginGui.setVisible(true);
        }
    }.execute();

}

    /* ─── Utility Widgets ─────────────────────────────────────────────── */
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
        // Consider disabling border painting if you added a manual border
        // btn.setBorderPainted(false);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(BRAND_LIGHT); }
            public void mouseExited (java.awt.event.MouseEvent evt) { btn.setBackground(BRAND_DARK);  }
        });
        return btn;
    }

    // Inner class GradientPanel 
    private class GradientPanel extends JPanel {
        private static final long serialVersionUID = 1L; 
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, BRAND_LIGHT, 0, getHeight(), Color.WHITE));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /* Launch helper */
    public void display() {
        
        if (SwingUtilities.isEventDispatchThread()) {
             setVisible(true);
         } else {
             SwingUtilities.invokeLater(() -> setVisible(true));
         }
     }

    
} 