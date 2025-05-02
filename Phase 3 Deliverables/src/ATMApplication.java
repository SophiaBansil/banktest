import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ATMApplication {
    private ConnectionHandler handler;
    private SessionInfo session;
    private Account account;
    // private ATMGUI gui;

    public ATMApplication(){
        this.account = null;

    }
    public void setConnectionHandler(ConnectionHandler c){
        this.handler = c;
    }

    public void setSession(SessionInfo s){
        this.session = s;
    }

    public void withdraw(float num) {
        performTransaction(num, Transaction.OPERATION.WITHDRAW);
        
    }

    public void deposit(float num) {
        performTransaction(num, Transaction.OPERATION.DEPOSIT);
    }

    // loads in all account data once an account is chosen from
    // ClientApplication
    public void loadAccount(String accID) {
        Message loadAccMsg = new AccountMessage(Message.TYPE.LOAD_ACCOUNT, session, accID );
        handler.send(loadAccMsg);
    
        try {
            if (refreshAccount()) {
                // RELAY ACCOUNT TO GUI
                //gui.displayAccountDetails(account);
            }
        } catch (Exception e) {
            System.out.println("Account loading interrupted.");
        }
    }

    public void loadTransactionHistory(){
        //RELAY TRANSACTION HIST TO GUI
        if (account == null || account.getTransactionHistory() == null) {
            //gui.showError("No transaction history available");
            return;
        }
       
         try {
            // sort transactions by date
            List<Transaction> transactions = new ArrayList<>(account.getTransactionHistory());
            transactions.sort((t1, t2) -> t2.getCreated().compareTo(t1.getCreated()));

            // do more formatting and build new list
            List<String> formatted = new ArrayList<>();
            for (Transaction t : transactions) {
                String formattedEntry = String.format("%s, %s, %s",
                    t.getDate(),
                    formatOperation(t.getOperation()), 
                    formatAmount(t)  
                );
                formatted.add(formattedEntry);
            }

            // relay to GUI
            // gui.displayTransactionHistory(formatted);

            } catch (Exception e) {
             // gui.showError("Error formatting transactions: " + e.getMessage());
            }
    }



    public void exit() {
    // remember to send message of type EXIT_ACCOUNT to server
    }

    //------------------------
    //private helper methods
    //------------------------

    private boolean performTransaction(double amount, Transaction.OPERATION operation) {
        if (account == null || session == null) {
            System.out.println("No active account session");
            return false;
        }
    
        // make transactionMessage
        TransactionMessage transMsg = new TransactionMessage(
            session,
            String.valueOf(amount),
            operation,
            account.getID());
        //send it to the server
        handler.send(transMsg);
    
        // BLOCK and wait for server response
        try {
            Message response = handler.getMessage();
            
            if (response instanceof SuccessMessage) {
                // transaction was a success, so refresh local account data
                boolean refresh = refreshAccount();
                return refresh;
            } else if (response instanceof FailureMessage) {
                System.out.println("Transaction failed: " + 
                    ((FailureMessage) response).getMessage());
                return false;
            }
            
        } catch (InterruptedException e) {
            System.out.println("Transaction was interrupted");
        }
        
        return false;
    }
    

    
    // method to retrieve account information from Database
    private boolean refreshAccount(){

        if (account == null) return false;
        // BLOCK client to wait for response
        try {
            Message serverResponse = handler.getMessage();
            if (serverResponse instanceof AccountMessage) {
                AccountMessage msg = (AccountMessage) serverResponse;
                // Reuse your existing account creation logic
                switch(msg.getAccountType()) {
                    case CHECKING:
                    this.account = createCheckingAccount(msg);
                    break;
                case SAVING:
                    this.account = createSavingAccount(msg);
                    break;
                case CREDIT_LINE:
                    this.account = createCreditLineAccount(msg);
                    break;
                default:
                    throw new IllegalStateException("Unknown account type");
                }
                return true;
            }else if (serverResponse instanceof FailureMessage) {
                System.out.println("Error: " + ((FailureMessage) serverResponse).getMessage());
            }   
        } catch (InterruptedException e) {
            System.out.println("Account refresh interrupted");
        }
        return false;
    }
    
     
    private Account createCheckingAccount(AccountMessage msg) {
        CheckingAccount account = new CheckingAccount();
        return account;
    }
    
    private Account createSavingAccount(AccountMessage msg) {
        SavingAccount account = new SavingAccount(msg.getWithdrawalLimit());
        return account;
    }
    
    private Account createCreditLineAccount(AccountMessage msg) {
        CreditLine account = new CreditLine(msg.getCreditLimit().toString());
        return account;
    }

    public String formatAmount(Transaction t) {
        String sign = (t.getOperation() == Transaction.OPERATION.DEPOSIT) ? "+" : "-";
        return String.format("%s$%.2f", sign, t.getAmount().abs());
    }

    private String formatOperation(Transaction.OPERATION op) {
        if (op == null) return "Unknown";
        return op.name().charAt(0) + op.name().substring(1).toLowerCase();
    }

}

