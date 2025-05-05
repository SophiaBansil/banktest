
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ATMApplication {
    private ConnectionHandler handler;
    private SessionInfo session;
    private Account account;
    private ClientProfile client;
    private static final BigDecimal ATM_TRANSACTION_LIMIT = new BigDecimal("9999.99");

    public ATMApplication(){
        this.account = null;

    }
    public void setConnectionHandler(ConnectionHandler c){
        this.handler = c;
    }

    public void setSession(SessionInfo s){
        this.session = s;
    }

    public void setClient(ClientProfile c){
        this.client = c;
    }
    
    public Account getAccount(){
        return this.account;
    }

    // getter for connectionhandlerand session not needed; already in clientprofileapp

    public boolean withdraw(String amount) {
        // check that inputted amount is valid
        // cannot enter negative amount
        // cannot withdraw more than $9999 at atm
        try {
			BigDecimal check = new BigDecimal(amount);
            if (check.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
            if (check.compareTo(ATM_TRANSACTION_LIMIT) > 0){
                // print to gui: Please see a Teller for assistance.
                return false;
            }
		} catch (NumberFormatException e) {
			return false;
		}

        Boolean result = performTransaction(amount, Transaction.OPERATION.WITHDRAW);

        if (result != false && this.account != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean deposit(String amount) {
        //check that inputted amount is valid
        // cannot enter negative amount
        // cannot  deposit more than $9999 at atm
        try {
			BigDecimal check = new BigDecimal(amount);
            if (check.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
            if (check.compareTo(ATM_TRANSACTION_LIMIT) > 0){
                // print to gui: Please see a Teller for assistance.
                return false;
            }	
		} catch (NumberFormatException e) {
			return false;
		}

        Boolean result = performTransaction(amount, Transaction.OPERATION.DEPOSIT);

        if (result != false && this.account != null) {
            return true;
        } else {
            return false;
        }
    }

    // loads in all account data once an account is chosen from
    // ClientApplication
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~` BLOCKS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void loadAccount(String accID) {
        Message loadAccMsg = new AccountMessage(Message.TYPE.LOAD_ACCOUNT, this.session, this.client.getUsername(), accID );
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
        Message msg = new AccountMessage(Message.TYPE.EXIT_ACCOUNT, this.session, this.client.getUsername(), this.account.getID());
        handler.send(msg);
        // return to clientprofile screen
    }

    //------------------------
    //private helper methods
    //------------------------

    private boolean performTransaction(String amount, Transaction.OPERATION operation) {
        if (account == null || session == null) {
            System.out.println("No active account session");
            return false;
        }
    
        // make transactionMessage
        TransactionMessage transMsg = new TransactionMessage(
            session,
            amount,
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
            
        } catch (Exception e) {
            System.out.println("Transaction was interrupted");
        }
        
        return false;
    }
    

    
    // method to retrieve account information from Database
    private boolean refreshAccount(){

        if (account == null) return false;
        // send LOAD_ACCOUNT request
        AccountMessage requestMsg = new AccountMessage(Message.TYPE.LOAD_ACCOUNT, session, this.client.getUsername(), account.getID());
        handler.send(requestMsg);
        // BLOCK client to wait for response
        try {
            Message serverResponse = handler.getMessage();
            if (serverResponse instanceof AccountMessage) {
                AccountMessage msg = (AccountMessage) serverResponse;
    
                switch(msg.getAccountType()) {
                    case CHECKING:
                    this.account = createCheckingAccount(msg);
                    break;
                case SAVING:
                    this.account = createSavingAccount(msg);
                    break;
                case CREDIT_LINE:
                    this.account = createCreditAccount(msg);
                    break;
                default:
                    throw new IllegalStateException("Unknown account type");
                }
                return true;
            }else if (serverResponse instanceof FailureMessage) {
                System.out.println("Error: " + ((FailureMessage) serverResponse).getMessage());
            }   
        } catch (Exception e) {
            System.out.println("Account refresh interrupted");
        }
        return false;
    }
    
     
    private Account createCheckingAccount(AccountMessage msg) {
        String id = msg.getID();
        BigDecimal balance = msg.getBalance();
        List<Transaction> history = msg.getTransactionHistory();

        CheckingAccount account = new CheckingAccount(id, balance, history);
        return account;
   }
    
    private Account createSavingAccount(AccountMessage msg) {
        String id = msg.getID();
        BigDecimal balance = msg.getBalance(); 
        List<Transaction> history = msg.getTransactionHistory(); 
        int withdrawCount = msg.getWithdrawCount();
        int withdrawLimit = msg.getWithdrawLimit();

        SavingAccount account = new SavingAccount(msg.getID(), msg.getBalance(), msg.getTransactionHistory(), msg.getWithdrawCount(), msg.getWithdrawLimit());
        return account;
    }
    
    private Account createCreditAccount(AccountMessage msg) {
       CreditLine account = new CreditLine(msg.getID(), msg.getBalance(),msg.getTransactionHistory(), msg.getCreditLimit() );
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

