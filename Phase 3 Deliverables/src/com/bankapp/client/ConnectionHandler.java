package com.bankapp.client;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.bankapp.common.DisconnectMessage;
import com.bankapp.common.Message;
import com.bankapp.common.SessionInfo;
import com.bankapp.common.Message.TYPE;

public class ConnectionHandler implements Runnable {

    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final BlockingQueue<Message> incomingMssg = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> outgoingMssg = new LinkedBlockingQueue<>();
    private Thread reader, writer = null;
    private volatile boolean running = true;
    private volatile boolean loggedOut = false;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    // communicates with LoginApplication
    //private LoginApplication loginApp;
    SessionInfo currentSession;

    public ConnectionHandler(Socket socket) {
        this.clientSocket = socket;
    }


    public void setLoggedOut(boolean status) {
        this.loggedOut = status;
    }

    public void setCurrentSession(SessionInfo s){
        this.currentSession = s;
    }


    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());
            reader = new Thread(this::readLoop);
            writer = new Thread(this::writeLoop);
            reader.start();
            writer.start();
            running = true;
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            // error handler callback
            closed.set(true);
            running = false;
        } 
    }

    private void readLoop() {
        try {
            while (running) {
                Message msg = null;
                // block and read message
                try{
                    msg = (Message) in.readObject();
                } catch(Exception e){
                    if (running) {
                        System.err.println("Unexpected error: " + e.getMessage());
                        shutDown(); 
                    }
                    break;
                }
                // handle unsolicited server messages
                // handle Message.TYPE.SHUTDOWN
                // handle Message.TYPE.SESSION_TIMEOUT
                if (msg.getType() == Message.TYPE.SHUTDOWN) {
                    System.out.println("Server-wide shutdown. Sorry for the inconvenience. " );
                    running = false;
                    break;
                }
                /*
                 * if (msg.getType() == Message.Type.SESSION_TIMEOUT) {
                 * System.out.println("440 Session Timeout. " );
                 * running = false;
                 * break;
                 * }
                 */

                incomingMssg.put(msg);
            }
        } catch (InterruptedException e) {
            System.out.println("ReadLoop interrupted.");
            Thread.currentThread().interrupt();
            running = false;
        } finally {
            shutDown();
        }
    }

    private void writeLoop() {
        try {
            while (running) {
                Message msg = null;
                try{
                    msg = outgoingMssg.take();
                } catch (InterruptedException e) {
                    if(running) System.out.println("WriteLoop interrupted while waiting for message.");
                    Thread.currentThread().interrupt();
                    break; 
                }
                try{
                    synchronized (out) {
                        out.writeObject(msg);
                        out.flush();
                    } 
                }catch (IOException e) {
                    if (running) {
                        System.err.println("Write error: " + e.getMessage());
                        shutDown();
                    }
                    break; 
                }
            }     
        } finally { 
           shutDown();
        }
    }

    // send message of type DISCONNECT to server
    public void sendDisconnect() {
        SessionInfo session = this.currentSession;
        if (session == null) {
             System.err.println("Cannot DISCONNECT without a current session.");
             return;
        }
        if (!isRunning()) {
             System.err.println("Cannot DISCONNECT on inactive/closed connection.");
             return;
        }
        outgoingMssg.offer(new DisconnectMessage(session));
    }


    public boolean isRunning() {
        return running;
    }

    public void send(Message msg) {
        outgoingMssg.offer(msg);
    }

    // this will BLOCK client app until message is received
    //
    public Message getMessage() {
        try {
            return incomingMssg.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    // end session
    public void shutDown() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        System.out.println("Now initiating client-side shutdown");
       
        running = false;

        if (reader != null) {
            reader.interrupt();
        }
        if (writer != null) {
            writer.interrupt();
        }

        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            clientSocket.close();
        } catch (IOException ignored) {
            // ignore so we can shut it down
        }
    }
}