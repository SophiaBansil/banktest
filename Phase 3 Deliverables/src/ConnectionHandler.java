import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final BlockingQueue<Message> incomingMssg = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> outgoingMssg = new LinkedBlockingQueue<>();
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
                    incomingMssg.offer(msg);
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

    public void send(Message msg) {
        outgoingMssg.offer(msg);
    }

    // this will BLOCK client app until message is received
    // 
    public Message getMessage() throws InterruptedException {
        try {
            return incomingMssg.take();
        } catch (InterruptedException e) {
            return null;
        }
    }
} 