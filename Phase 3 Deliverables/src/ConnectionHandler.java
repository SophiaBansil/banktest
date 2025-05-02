import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final BlockingQueue<Message> incomingMssg = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> outgoingMssg = new LinkedBlockingQueue<>();
    private Thread reader, writer;
    private volatile boolean running = true;
    private volatile boolean loggedOut = false;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    // communicates with LoginApplication
    private LoginApplication loginApp;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
    }

    // setter for LoginApplication
    public void setLoginApplication(LoginApplication app) {
        this.loginApp = app;
    }

    public void setLoggedOut(boolean status) {
        this.loggedOut = status;
    }

    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            // error handler callback
            if (loginApp != null) {
                loginApp.handleConnectionError("Connection lost. Please restart the app.");
            }
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void readLoop(){
        try{
            while (running){
                // WILL BLOCK
                Message msg = (Message) in.readObject();

                // handle unsolicited server messages
                // handle Message.TYPE.SHUTDOWN
                // handle Message.TYPE.SESSION_TIMEOUT
                /*if (msg.getType() == Message.Type.SHUTDOWN) {
                    System.out.println("Server-wide shutdown. Sorry for the inconvenience. " );
                    running = false;
                    break;
                }*/
                /*if (msg.getType() == Message.Type.SESSION_TIMEOUT) {
                    System.out.println("440 Session Timeout. " );
                    running = false;
                    break;
                }*/

                incomingMssg.put(msg);
            }
            // EOFException thrown when program tries to read the input stream when it has already been closed
	        // SocketException thrown when the connection to the server closes unexpectedly
        }catch (EOFException | SocketException eof) {
            if (running) { 
                if (loggedOut) {
                    System.out.println("You have logged out.");
                } else {
                    System.out.println("Server has disconnected.");
                    if (loginApp != null) {
                        loginApp.handleConnectionError("Server disconnected.");
                    }
                }
            }
            // IOException thrown when i/o messes up
            // ClassNotFoundException from class not found 
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                System.err.println("ReadLoop error: " + e.getMessage());
                 if (loginApp != null) {
                    loginApp.handleConnectionError("ReadLoop error: " + e.getMessage());
                }
            }

        } catch (InterruptedException e) {
            System.out.println("ReadLoop interrupted.");
            Thread.currentThread().interrupt();
       } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                 out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
        }
       }
    }

    private void writeLoop() {
        try {
            while (running) {
                Message msg = outgoingMssg.take(); 
                out.writeObject(msg);
                out.flush();
            }
        } catch (InterruptedException ie) { 
             System.out.println("WriteLoop interrupted.");
             Thread.currentThread().interrupt();
        } catch (IOException e) { 
            if (running) {
                System.err.println("Writeloop error: " + e.getMessage());
                 if (loginApp != null) {
                     loginApp.handleConnectionError("Write error: " + e.getMessage());
                 }
            }
        } finally { // will always run after loop ends, regardless of exceptions
            try {
                if (in != null)
                    in.close();
                if (out != null)
                 out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
        }
        }
    }

    public boolean isRunning() {
        return running;
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

    public void shutDown() {
        if (closed.get() == false) {
            return;
        }
        System.out.println("Now initiating client-side shutdown");
        // set running to false
        running = false;

        if (reader != null) reader.interrupt();
        if (writer != null) writer.interrupt();

        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}