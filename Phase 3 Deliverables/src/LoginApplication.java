package bank;

import java.net.Socket;

public class LoginApplication {
    private ConnectionHandler handler;  // Handles communication with the server
    private LoginGUI gui;               // Talks to GUI for showing errors or session timeouts

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

    // Sends a login request for a Teller
    public void TellerLogin(String user, String pass) {
        establishConnection();
        Message loginMsg = new Message(user, pass, TYPE.LOGIN);
        handler.send(loginMsg);
    }

    // Sends a login request for a Client
    public void ClientLogin(String user, String pass) {
        establishConnection();
        Message loginMsg = new Message(user, pass, TYPE.LOGIN);
        handler.send(loginMsg);
    }

    // Connects to the server and creates the handler if it's not already set
    private void establishConnection() {
        if (handler == null) {
            try {
                Socket socket = new Socket("localhost", 1234);
                handler = new ConnectionHandler(socket);
                new Thread(handler).start();
            } catch (Exception e) {
                System.err.println("Failed to connect: " + e.getMessage());
            }
        }
    }
} 
