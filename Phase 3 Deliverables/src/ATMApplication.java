

public class ATMApplication {
    private ConnectionHandler handler;
    private SessionInfo session;
    private Account account;gi

    public ATMApplication(){
        
    }
    public double withdraw(float num) {

        // 1) create transaction message of type WITHDRAW
        // 2) send to server via connectionHandler; wait for response
        // 3) SUCCESS: return new account balance
        // 4) FAILURE: return -1
        return 0;
    }

    public double deposit(float num) {
        // 1) create transaction message of type WITHDRAW
        // 2) send to server via connectionHandler; wait for response
        // 3) SUCCESS: return new account balance 
        // 4) FAILURE: return -1
        return 0;
    }

    // loads in all account data once an account is chosen from
    // ClientApplication
    public Account loadAccount(int accID) {
        return null;
    }

    public void setConnectionHandler(ConnectionHandler c){
        this.handler = c;
    }

    public void setSession(SessionInfo s){
        this.session = s;
    }


    public void exit() {

    }

}

