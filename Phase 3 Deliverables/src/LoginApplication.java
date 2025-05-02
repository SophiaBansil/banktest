

import java.net.Socket;

public class LoginApplication {
    private ConnectionHandler handler;  
    private LoginGUI gui;               // Talks to GUI for showing errors or session timeouts
    private ClientProfileApplication clientProApp;
    private ATMApplication ATMApp;
    // private TellerApplication tellerApp;
    private Boolean isTeller = false;

    // Allows the GUI to be connected to the application logic
    public void setGUI(LoginGUI gui) {
        this.gui = gui;
    }

    // Allows the MainApp to provide the handler instance (instead of creating it internally)
    public void setHandler(ConnectionHandler handler) {
        this.handler = handler;
    }

    // Called when a connection error happens (server down, socket closed)
    public void handleConnectionError(String message) {
        if (gui != null) {
            gui.showError(message);
        }
    }

    // Called when the server sends a timeout (session expired)
    public void handleSessionTimeout() {
        if (gui != null) {
            gui.showError("Session timed out due to inactivity.");
            gui.Login(); // Restart the login screen
        }
    }

    // Sends a login request for a Teller FINISH ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void TellerLogin(String user, String pass) {
        isTeller = true;
        establishConnection();
        Message loginMsg = new LoginMessage(Message.TYPE.LOGIN_TELLER, user, pass);
        handler.send(loginMsg);

        // BLOCK and wait for server response
        try {
            Message serverResponse = handler.getMessage();
            if (serverResponse.getType() == Message.TYPE.SUCCESS && serverResponse instanceof SuccessMessage){
                // cast to successMessage & manage new session ID
                SuccessMessage msg = (SuccessMessage) serverResponse;
                SessionInfo session = msg.getSession();

                // SET up tellerprofileappp w session parameters
                //tellerApp = new TellerProfileApplication();
                //tellerApp.setConnectionHandler(handler);
                //tellerApp.setSession(session);
                //gui.showTellerHomeScreen(); // FAKE METHOD~~~~~~~~~~~~~

            }else if ( serverResponse.getType() == Message.TYPE.FAILURE && serverResponse instanceof FailureMessage){
                // cast to FailureMessage
                FailureMessage msg = (FailureMessage) serverResponse;
                System.out.println("Error: " + msg.getMessage());
            } else {
                System.out.println("Error: unexpected message type received");
            }
        } catch (InterruptedException e) {
            System.out.println("Login request interrupted");
            if (gui != null) gui.showError("Login process interrupted");
        }
    }

    // Sends a login request for a Client -- from an ATM
    public void ClientLogin(String user, String pass) {
        establishConnection();
        Message loginMsg = new LoginMessage(Message.TYPE.LOGIN_CLIENT, user, pass);
        handler.send(loginMsg);

        // BLOCK and wait for server response
        try {
            Message serverResponse = handler.getMessage();
            if (serverResponse.getType() == Message.TYPE.SUCCESS && serverResponse instanceof SuccessMessage){
                // cast to successMessage & manage new session ID
                SuccessMessage msg = (SuccessMessage) serverResponse;
                SessionInfo session = msg.getSession();

                // set up clientProfileApplication with session parameters
                clientProApp = new ClientProfileApplication();
                ATMApp = new ATMApplication();
                clientProApp.setConnectionHandler(handler);
                clientProApp.setSession(session);
                ATMApp.setConnectionHandler(handler);
                ATMApp.setSession(session);
                clientProApp.setATMApplication(ATMApp);

                clientProApp.requestProfile();

                // ~~~~~transition into correct gui screen later~~~~~~
                // gui.showClientProfileScreen(); // FAKE METHOD~~~~~~~~~~~~~~~

            }else if ( serverResponse.getType() == Message.TYPE.FAILURE && serverResponse instanceof FailureMessage){
                // cast to FailureMessage
                FailureMessage msg = (FailureMessage) serverResponse;
                System.out.println("Error: " + msg.getMessage());
            } else {
                System.out.println("Error: unexpected message type received");
            }
        } catch (InterruptedException e) {
            System.out.println("Login request interrupted");
            if (gui != null) gui.showError("Login process interrupted");
        }
    }

    // Connects to the server and creates the handler if it's not already set
    private void establishConnection() {
        if (handler == null) {
            try {
                Socket socket = new Socket("localhost", 7777);
                handler = new ConnectionHandler(socket);
                new Thread(handler).start();
            } catch (Exception e) {
                System.err.println("Failed to connect: " + e.getMessage());
            }
        }
    }
} 
