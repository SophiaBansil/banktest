import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable {
    // Dependencies
    private final Socket socket; // holds socket to the client
    private final CentralServer server; // holds reference to server

    // I/O Streams
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    // Queues for holding message traffic
    private final BlockingQueue<Message> incoming = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> outgoing = new LinkedBlockingQueue<>();

    // Thread and control flag
    private volatile boolean running; // checks for if clientHandler is running
    private volatile boolean authenticated;
    private Thread reader, writer;

    public ClientHandler(Socket socket, CentralServer server) {
        this.socket = socket;
        this.server = server;
        running = true;
        setAuthenticated(false);
    }

    @Override
    public void run() {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        reader = new Thread(this::readLoop);
        writer = new Thread(this::writeLoop);
        reader.start();
        writer.start();

        // while clientHandler is running, the handler will take messages from client
        // from the incoming queue and hand it to the server
        try {
            while (running) {
                Message msg = incoming.take();
                server.handleMessage(msg, this);
            }
        } catch (InterruptedException ignored) {
        } finally {
            cleanUp();
        }

        // Wait for I/O threads to finish
        joinThreads();
    }

    private void readLoop() {
        try {
            while (running) {
                Message msg = (Message) objectInputStream.readObject();
                incoming.put(msg);
            }
        } catch (EOFException | SocketException eof) {
            // expected when client disconnects
        } catch (Exception e) {
            if (running)
                System.out.println("[Server] Read error: " + e.getMessage());
        } finally {
            running = false;
        }
    }

    private void writeLoop() {
        try {
            while (running) {
                Message msg = outgoing.take();
                objectOutputStream.writeObject(msg);
                objectOutputStream.flush();
            }
        } catch (InterruptedException ie) {
            // thread interrupted
        } catch (IOException e) {
            if (running)
                System.out.println("[Server] Write error: " + e.getMessage());
        } finally {
            running = false;
        }
    }

    // Queue a message to send
    public void sendMessage(Message msg) {
        outgoing.offer(msg);
    }

    // Signal this handler to stop and clean up immediately
    public void shutDown() {
        running = false;
        reader.interrupt();
        writer.interrupt();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        cleanUp();
        System.out.println("[Server] Client Disconnected");
    }

    // Close socket and streams
    private void cleanUp() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void joinThreads() {
        try {
            reader.join();
        } catch (InterruptedException ignored) {
        }
        try {
            writer.join();
        } catch (InterruptedException ignored) {
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
