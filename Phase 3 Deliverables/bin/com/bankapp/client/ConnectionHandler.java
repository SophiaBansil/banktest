import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionHandler implements Runnable {
    private final Socket client;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final BlockingQueue<Message> incoming = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> outgoing = new LinkedBlockingQueue<>();

    private Thread reader, writer;
    private volatile boolean running = true;
    private volatile boolean loggedOut = false;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Keep session info for SwingWorker and post-login use
    private SessionInfo currentSession;

    public ConnectionHandler(Socket client) {
        this.client = client;
    }

    public void setCurrentSession(SessionInfo session) {
        this.currentSession = session;
    }

    public SessionInfo getCurrentSession() {
        return currentSession;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(client.getOutputStream());
            out.flush();
            in = new ObjectInputStream(client.getInputStream());

            reader = new Thread(this::readLoop, "Connection-Reader");
            writer = new Thread(this::writeLoop, "Connection-Writer");
            reader.start();
            writer.start();

            reader.join();
            writer.join();
        } catch (IOException | InterruptedException e) {
            if (running) {
                System.err.println("[ConnectionHandler] Run error: " + e.getMessage());
            }
        } finally {
            shutDown();
        }
    }

    private void readLoop() {
        try {
            while (running) {
                Message msg = (Message) in.readObject();

                // Handle server shutdown token
                if (msg.getType() == Message.TYPE.SHUTDOWN) {
                    System.out.println("[Client] Server sent SHUTDOWN.");
                    break;
                }

                incoming.put(msg);
            }
        } catch (EOFException | SocketException eof) {
            String text = loggedOut ? "Client logged out." : "Server disconnected.";
            System.out.println("[Client] " + text);
        } catch (Exception e) {
            if (running) {
                System.err.println("[Client] Read error: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            shutDown();
        }
    }

    private void writeLoop() {
        try {
            while (running) {
                Message msg = outgoing.take();
                synchronized (out) {
                    out.writeObject(msg);
                    out.flush();
                }
            }
        } catch (InterruptedException ignored) {
        } catch (IOException e) {
            if (running) {
                System.err.println("[Client] Write error: " + e.getMessage());
            }
        } finally {
            shutDown();
        }
    }

    public void sendMessage(Message msg) {
        if (running) {
            outgoing.offer(msg);
        }
    }

    /**
     * Send a DISCONNECT request to the server, then mark as logged out.
     */
    public void sendDisconnect() {
        if (currentSession == null) {
            System.err.println("Cannot DISCONNECT without a current session.");
            return;
        }
        if (!running) {
            System.err.println("Cannot DISCONNECT on inactive/closed connection.");
            return;
        }
        // Offer a DisconnectMessage to the outgoing queue
        outgoing.offer(new DisconnectMessage(currentSession));
        // Mark this handler as having logged out so that EOF handling is clear
        this.loggedOut = true;
    }

    public Message getMessage() {
        try {
            return incoming.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setLoggedOut(boolean status) {
        this.loggedOut = status;
    }

    public void shutDown() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        running = false;
        if (reader != null) reader.interrupt();
        if (writer != null) writer.interrupt();
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            client.close();
        } catch (IOException ignored) {}
        System.out.println("[ConnectionHandler] Shutdown complete");
    }
}

