package com.bankapp.client;
import java.util.Collections;
import java.util.List;

import com.bankapp.common.AccountSummary;
import com.bankapp.common.ClientProfile;
import com.bankapp.common.FailureMessage;
import com.bankapp.common.LogoutMessage;
import com.bankapp.common.Message;
import com.bankapp.common.ProfileMessage;
import com.bankapp.common.SessionInfo;
import com.bankapp.common.SuccessMessage;
import com.bankapp.common.Message.TYPE;

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

    public ATMApplication getAtmApplication(){
        return this.atmApp;
    }

    // this sends request to server for clientProfile info
    // ~~~~~~BLOCKS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public Message requestProfile(){
        // create ProfileMessage object to send
        Message profileMessage = new ProfileMessage(Message.TYPE.LOAD_PROFILE, session, session.getUsername());
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
                return msg;
            } else if ( serverResponse.getType() == Message.TYPE.FAILURE && serverResponse instanceof FailureMessage){
                // cast to FailureMessage
                FailureMessage msg = (FailureMessage) serverResponse;
                return msg;
            } else {
                System.out.println("Error: unexpected message type received");
                return new FailureMessage("Error: unexpected message type received");
            }
        } catch (Exception e) {
            System.out.println("Request interrupted");
            return new FailureMessage("Error: request interrupted");
        }
    }




    // this sends all profile info to gui
   /*  public void loadProfile(){

    } */


    public List<AccountSummary> getAccountSummaries(){
     
    if (this.accounts == null) {
        return Collections.emptyList();
    }
    
    return Collections.unmodifiableList(this.accounts);
    }


    //this will open up ATMApplication & corresponding GUI page
    // gui will send in selected ID
    public Message selectAccount(String id) {
        AccountSummary selected = null;
        for (AccountSummary summary : accounts) {
            if (summary.getID().equals(id)) {
                selected = summary;
                break;
            }
        }
        if (atmApp == null) {
            return new FailureMessage("ATM application not initialized.");
        }
        if (selected == null) {
            return new FailureMessage("Account does not exist " + id);
        }
        
        atmApp.setClient(profile);
        return atmApp.loadAccount(selected.getID()); 
        
    }

    //this will return to LoginApplication and Login screen
    public Message exit() {
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
        return new SuccessMessage("shutting down ATM");
    }   
}
