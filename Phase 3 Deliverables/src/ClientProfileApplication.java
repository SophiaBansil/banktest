import java.util.List;

public class ClientProfileApplication {

    private ClientProfile profile;
    private ConnectionHandler handler;
    private SessionInfo session;
    private List<AccountSummary> accounts;
    // private ClientProfileGUI gui;

    /*public void setGUI(ClientProfileGUI g){
        this.gui = g;
    }*/
    public void setConnectionHandler(ConnectionHandler c){
        this.handler = c;
    }

    public void setSession(SessionInfo s){
        this.session = s;
    }

    // this sends request to server for clientProfile info
    public void requestProfile(){
        // create ProfileMessage object to send
        Message profileMessage = new ProfileMessage(Message.TYPE.LOAD_PROFILE, session);
        // send to server via handler
        handler.send(profileMessage);
    }

    public void handleProfileMssgResponse(ProfileMessage msg){
        // create ClientProfile object from message received
        this.profile = new ClientProfile(
            msg.getUsername(),
            msg.getPassword(), 
            msg.getPhone(),
            msg.getAddress(), 
            msg.getLegalName());  

        this.accounts = msg.getSummaries();

        // relay info to GUI
        /*loadProfile();
        loadAccounts();*/
     }

    // this sends all profile info to gui
   /*  public void loadProfile(){
        if (gui != null && profile != null) {
            gui.displayProfile(profile); 
        }
    } */

    // this sends all accountsummary.java info to gui for account thumbnails
    /*public void loadAccounts(){
        if (gui != null && accounts != null) {
            gui.displayAccounts(accounts); 
        }
    }*/


    //this will open up ATMApplication & corresponding GUI page
    public void selectAccount(String id) {}

    //this will return to LoginApplication and Login screen
    public void exit() {}   
}
