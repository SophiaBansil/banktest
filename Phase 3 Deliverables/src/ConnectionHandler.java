package bank;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
 // communicates with LoginApplication 
    private LoginApplication loginApp; 

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
    }
    
  //setter for LoginApplication
    public void setLoginApplication(LoginApplication app) { 
        this.loginApp = app;
    }

    
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Object obj = in.readObject();
                if (obj instanceof Message) {
                    Message msg = (Message) obj;
                    handleMessage(msg);
                } else {
                    System.out.println("Received unknown object.");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection error: " + e.getMessage());
            //error handler callback
            if (loginApp != null) {
                loginApp.handleConnectionError("Connection lost. Please restart the app.");
            }
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void handleMessage(Message msg) {
        Message.TYPE type = msg.getType();  // use Message.TYPE
        SessionInfo session = msg.getSession();  // session might be null for some messages

        switch (type) {
            case LOGIN_CLIENT:
            case LOGIN_TELLER:
                System.out.println("Handling LOGIN request");
                break;
            case SUCCESS:
                System.out.println("Success: Session for " + (session != null ? session.getUsername() : "Unknown"));
                if (loginApp != null) {
                    loginApp.handleServerMessage(msg);
                }
                break;
            case FAILURE:
                System.out.println("Failure received");
                if (loginApp != null) {
                    loginApp.handleServerMessage(msg);
                }
                break;
            case LOAD_PROFILE:
                System.out.println("Loading profile");
                break;
            case LOAD_ACCOUNT:
                System.out.println("Loading account");
                break;
            case SAVE_PROFILE:
                System.out.println("Saving profile");
                break;
            case DELETE_PROFILE:
                System.out.println("Deleting profile");
                break;
            case DELETE_ACCOUNT:
                System.out.println("Deleting account");
                break;
            case TRANSACTION:
                System.out.println("Processing transaction");
                break;
            case LOGOUT_ATM:
            case LOGOUT_CLIENT:
            case LOGOUT_TELLER:
                System.out.println("Logout received");
                if (type == Message.TYPE.LOGOUT_CLIENT && loginApp != null) {
                    loginApp.handleSessionTimeout();
                }
                break;
            case SHUTDOWN:
                System.out.println("Server is shutting down");
                break;
            default:
                System.out.println("Unhandled message type: " + type);
        }
    }

    public void send(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
} 