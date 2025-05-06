package com.bankapp.client;


import java.net.Socket;

import com.bankapp.client.gui.LoginGUI;
import com.bankapp.common.FailureMessage;
import com.bankapp.common.LoginMessage;
import com.bankapp.common.Message;
import com.bankapp.common.SessionInfo;
import com.bankapp.common.SuccessMessage;
import com.bankapp.common.Message.TYPE;

public class LoginApplication {
    private ConnectionHandler handler;  
    private LoginGUI gui;               // Talks to GUI for showing errors or session timeouts
    private ClientProfileApplication clientProApp;
    private ATMApplication ATMApp;
    private TellerApplication tellerApp;

    // Allows the GUI to be connected to the application logic
    public void setGUI(LoginGUI gui) {
        this.gui = gui;
    }

    // Allows the MainApp to provide the handler instance (instead of creating it internally)
    public void setHandler(ConnectionHandler handler) {
        this.handler = handler;
    }

    public ClientProfileApplication getClientProfileApp() {
        return clientProApp;
    }

    public ATMApplication getAtmApp() {
        return ATMApp;
    }

    public TellerApplication getTellerApp() {
        return tellerApp;
    }


    // Sends a login request for a Teller FINISH ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public Message TellerLogin(String user, String pass) {
        establishConnection();
        if (!establishConnection()) {
            return new FailureMessage("CONNECTION_ERROR: Failed to establish connection to the server.");
        }
        Message loginMsg = new LoginMessage(Message.TYPE.LOGIN_TELLER, user, pass);
        handler.send(loginMsg);

        // BLOCK and wait for server response
        try {
            Message serverResponse = handler.getMessage();
            if ( serverResponse instanceof SuccessMessage){
                // cast to successMessage & manage new session ID
                SuccessMessage msg = (SuccessMessage) serverResponse;
                SessionInfo session = msg.getSession();
                handler.setCurrentSession(session);
                
                tellerApp = new TellerApplication();
                tellerApp.setConnectionHandler(handler);
                tellerApp.setSession(session);

            }else if (serverResponse instanceof FailureMessage){
                // cast to FailureMessage
                FailureMessage msg = (FailureMessage) serverResponse;
                System.out.println("Error: " + msg.getMessage());
            } else {
                return new FailureMessage("Unexpected error occurred during login: ");
            }
            return serverResponse;
        } catch (Exception e) {
            return new FailureMessage("CLIENT_ERROR: An error occurred during login: " + e.getMessage());
        }
    }

    // Sends a login request for a Client -- from an ATM
    public Message ClientLogin(String user, String pass) {
        establishConnection();
        if (!establishConnection()) {
            return new FailureMessage("CONNECTION_ERROR: Failed to establish connection to the server.");
        }
        Message loginMsg = new LoginMessage(Message.TYPE.LOGIN_CLIENT, user, pass);
        handler.send(loginMsg);

        // BLOCK and wait for server response
        try {
            Message serverResponse = handler.getMessage();
            if (serverResponse instanceof SuccessMessage){
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

               // gui will call requestprofile


            }else if (serverResponse instanceof FailureMessage){
                // cast to FailureMessage
                FailureMessage msg = (FailureMessage) serverResponse;
                return msg;
            } else {
                return new FailureMessage("Unexpected message type received");
            }
            return serverResponse;
        } catch (Exception e) {
            return new FailureMessage("Unexpected error occurred during login: ");
        }
    }

    // Connects to the server and creates the handler if it's not already set
    private boolean establishConnection() {
        if (handler == null) {
            try {
                Socket socket = new Socket("localhost", 7777);
                handler = new ConnectionHandler(socket);
                new Thread(handler).start();
                return true;
            } catch (Exception e) {
                System.err.println("Failed to connect: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    public void exitApplication() {
        System.out.println(" Exit requested.");
        if (handler != null) {
             handler.shutDown(); 
             handler = null;
        }
        System.out.println("Exiting application.");
        System.exit(0); // Terminate the application
   }
} 
