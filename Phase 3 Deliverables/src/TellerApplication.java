import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TellerApplication {

    private ClientProfile profile;
    private Account account;
    private ConnectionHandler handler;
    private SessionInfo session;
    private List<AccountSummary> accounts;

    public void setConnectionHandler(ConnectionHandler c){
        this.handler = c;
    }

    public void setSession(SessionInfo s){
        this.session = s;
    }


    public void changeBankInfo() {
        // use SAVE_PROFILE?
    }
    public void deleteProfile(){
        // send mssg of type DELETE_PROFILE
    }
    public void deleteAccount(String id) {
        // send mssg of type DELETE_ACCOUNT
    }
    public void addAccount(String type){
        // send mssg of type CREATE_ACCOUNT
        // remember implement shared accounts 
        // also still need to implement server-side account creation
    }
    public void logOutClientProfile(){
        // send mssg of type LOGOUT_CLIENT
    }
    public void createNewProfile(){
        // step 1:  send mssg of TYPE.CHECK_USERNAME_AVAILABILITY, which just checks if username is available
        //          ( this is to avoid needing to retype all info if username is taken)
        // step 2: if username is good. send mssg of TYPE.CREATE_PROFILE which takes in 
        //          profile information
        // also still need to implement server-side account creation
    }

    public void exit() {
        // send mssg of type LOGOUT_TELLER
    }

    public boolean withdraw(String amount) {
        // check that inputted amount is valid

        try {
			BigDecimal check = new BigDecimal(amount);
            if (check.compareTo(BigDecimal.ZERO) <= 0) {
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
        try {
			BigDecimal check = new BigDecimal(amount);
            if (check.compareTo(BigDecimal.ZERO) <= 0) {
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

    public void loadClientProfile(){
        // create ProfileMessage object to send
        Message profileMessage = new ProfileMessage(Message.TYPE.LOAD_PROFILE, session);
        // send to server via handler
        handler.send(profileMessage);

        // BLOCK and wait for server response
        try{
            Message serverResponse = handler.getMessage();
            if (serverResponse.getType() == Message.TYPE.LOAD_PROFILE && serverResponse instanceof ProfileMessage){
                // cast serverResponse to ProfileMessage
                ProfileMessage msg = (ProfileMessage) serverResponse;
                // create ClientProfile object from server response
                this.profile = new ClientProfile(
                msg.getUsername(),
                msg.getPassword(), 
                msg.getPhone(),
                msg.getAddress(), 
                msg.getLegalName());  
                this.accounts = msg.getSummaries();
            } else if ( serverResponse.getType() == Message.TYPE.FAILURE && serverResponse instanceof FailureMessage){
                // cast to FailureMessage
                FailureMessage msg = (FailureMessage) serverResponse;
                System.out.println("Error: " + msg.getMessage());
            } else {
                System.out.println("Error: unexpected message type received");
            }
        } catch (Exception e) { // ConnectionHandler.getMessage() throws an InterruptedException
            System.out.println("Request interrupted");
        }
    } 

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

    public void exitAccount() {
        Message msg = new AccountMessage(Message.TYPE.EXIT_ACCOUNT, this.session, this.account.getID());
        handler.send(msg);
        // return to clientprofile screen
    }

    // __________________________________________
    // private helper methods ___________________
    // __________________________________________
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
    private boolean refreshAccount(){

        if (account == null) return false;
        // send LOAD_ACCOUNT request
        AccountMessage requestMsg = new AccountMessage(Message.TYPE.LOAD_ACCOUNT, session, account.getID());
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
                    this.account = createCreditLineAccount(msg);
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
        CheckingAccount account = new CheckingAccount();
        return account;
    }
    
    private Account createSavingAccount(AccountMessage msg) {
        SavingAccount account = new SavingAccount();
        return account;
    }
    
    private Account createCreditLineAccount(AccountMessage msg) {
        CreditLine account = new CreditLine(msg.getCreditLimit().toString());
        return account;
    }
}
