package src2.client;
import java.util.List;

import src2.common.AccountSummary;
import src2.common.ClientProfile;
import src2.common.FailureMessage;
import src2.common.LogoutMessage;
import src2.common.Message;
import src2.common.ProfileMessage;
import src2.common.SessionInfo;
import src2.common.SuccessMessage;
import src2.common.Message.TYPE;

import java.lang.InterruptedException;

public class ClientProfileApplication {

    private ClientProfile profile;
    private ConnectionHandler handler;
    private SessionInfo session;
    private ATMApplication atmApp;
    private List<AccountSummary> accounts;
 
    public void setATMApplication(ATMApplication atmApp) {
        this.atmApp = atmApp;
    }

    public void setConnectionHandler(ConnectionHandler c){
        this.handler = c;
    }

    public void setSession(SessionInfo s){
        this.session = s;
    }

    // this sends request to server for clientProfile info
    // ~~~~~~BLOCKS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void requestProfile(){
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




    // this sends all profile info to gui
   /*  public void loadProfile(){

    } */

    // this sends all accountsummary.java info to gui for account thumbnails
    /*public void loadAccounts(){
     
    }*/


    //this will open up ATMApplication & corresponding GUI page
    // gui will send in selected ID
    public void selectAccount(String id) {
        AccountSummary selected = null;
        for (AccountSummary summary : accounts) {
            if (summary.getID().equals(id)) {
                selected = summary;
                break;
            }
        }

        if (atmApp != null && selected != null) {
            atmApp.setClient(profile);
            atmApp.loadAccount(selected.getID()); 
        }
    }

    //this will return to LoginApplication and Login screen
    public void exit() {
        // client-side log out
        Message logoutMsg = new LogoutMessage(
            Message.TYPE.LOGOUT_ATM, 
            this.session
        );
        handler.send(logoutMsg);

        Message msg = handler.getMessage();
        if (msg instanceof SuccessMessage) {
            System.out.println("Logged out successfully: " + ((SuccessMessage) msg).getMessage());

        }

        handler.setLoggedOut(true);
        handler.shutDown();
    }   
}
