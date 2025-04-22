package bank;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // Important: ObjectOutputStream must be created before ObjectInputStream!
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush(); // flush header

            in = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                // Read incoming message
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
        TYPE type = msg.getType();
        String text = msg.getText();
        String id = msg.getID();

        switch (type) {
            case LOGIN:
                System.out.println("Handling LOGIN for ID: " + id + ", payload: " + text);
                break;
            case SUCCESS:
                System.out.println("Success: " + text);
                break;
            case FAILURE:
                System.out.println("Failure: " + text);
                break;
            case LOAD_PROFILE:
                System.out.println("Loading profile for ID: " + id);
                break;
            case LOAD_ACCOUNT:
                System.out.println("Loading account for ID: " + id);
                break;
            case SAVE_PROFILE:
                System.out.println("Saving profile: " + id);
                break;
            case DELETE_PROFILE:
                System.out.println("Deleting profile: " + id);
                break;
            case DELETE_ACCOUNT:
                System.out.println("Deleting account: " + id);
                break;
            case TRANSACTION:
                System.out.println("Processing transaction: " + text);
                break;
            case LOGOUT_ATM:
            case LOGOUT_CLIENT:
            case LOGOUT_TELLER:
                System.out.println("Logout received for ID: " + id);
                break;
            default:
                System.out.println("Unhandled message type: " + type);
        }
    }

    // Send a Message to the server
    public void send(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}
