import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class CentralServer {
	// username -> password (could make custom teller class)
    private final Map<String, String> tellerDatabase = new HashMap<>();
    // username -> clientProfile objects
    private final Map<String, ClientProfile> clientDatabase = new HashMap<>();
 // id -> account objects
    private final Map<String, Account> accountDatabase = new HashMap<>();
    
    // username -> sessionInfo (includes profile
    private final Map<String, SessionInfo> clientSessionIDs = new HashMap<>();
    // holds active client sessions and sessionInfo for clients
    private final Map<String, SessionInfo> tellerSessionIDs = new HashMap<>();
    
    // checks and prevent concurrent accounts, profiles, and tellers from being opened
    private final ConcurrentMap<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,ReentrantLock> profileLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,ReentrantLock> tellerLocks = new ConcurrentHashMap<>();
    
    // thread-safe variant of ArrayList in Java
    // best choice if read operation is most frequently used
    private final List<ClientHandler> client_list = new CopyOnWriteArrayList<>();

    public CentralServer() {
        // Pretend these were loaded from a file or database
        tellerDatabase.put("alice", "i_<3_RSA");
        tellerDatabase.put("bob", "password12345");
    }
	
    // Runs the actual server
    public static void main(String[] args) {
        CentralServer serverInstance = new CentralServer(); // <== Create instance
        
        // In case the server unexpectedly or forcefully closes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Server] Shutdown initiated.");
            serverInstance.serverShutDown();
        }));

        try (ServerSocket server = new ServerSocket(7777)) {
        	System.out.println("[Server] Server Initiated.");
            server.setReuseAddress(true);
            while (true) {
                Socket client = server.accept();
                System.out.println("[Server] New Client Connected: " + client.getInetAddress().getHostAddress());

                ClientHandler clientSock = new ClientHandler(client, serverInstance);
                serverInstance.addClient(clientSock); // Add to list
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleMessage(Message msg, ClientHandler handler) {
        // Only allow login messages before authentication
        if (!handler.isAuthenticated()) {
            if (msg instanceof LoginMessage) {
               handleLogin((LoginMessage) msg, handler);
            } else {
                handler.sendMessage(new FailureMessage("You must log in first."));
            }
            return;
        }
        // Once authenticated, delegate to role-based handlers
        switch (msg.getSession().getRole()) {
            case CLIENT:
                handleClientMessages(msg, handler);
                break;
            case TELLER:
            	handleTellerMessage(msg, handler);
                break;
            default:
                handler.sendMessage(new FailureMessage("Unknown message type."));
        }
    }
    
    // delegated method to handle teller functions
    private void handleTellerMessage(Message msg, ClientHandler handler) {
		switch (msg.getType()) {
			case LOAD_PROFILE:
				break;
			case SAVE_PROFILE:
				break;
			case DELETE_PROFILE:
				break;
			case LOAD_ACCOUNT:
				break;
			case SAVE_ACCOUNT:
				break;
			case DELETE_ACCOUNT:
				break;
			case SHARE_ACCOUNT:
				break;
			case TRANSACTION:
				break;
			case LOGOUT_TELLER:
				break;
			default:
				handler.sendMessage(new FailureMessage("What You Doing?"));	
		}
	}

    // delegated method to handle client functions
	private void handleClientMessages(Message msg, ClientHandler handler) {
		switch (msg.getType()) {
		case LOAD_PROFILE:
			break;
		case SAVE_PROFILE:
			break;
		case LOAD_ACCOUNT:
			break;
		case SAVE_ACCOUNT:
			break;
		case SHARE_ACCOUNT:
			break;
		case TRANSACTION:
			break;
		case LOGOUT_CLIENT:
			handleClientLogout(msg, handler);
			break;
		case LOGOUT_ATM:
			break;
		default:
			handler.sendMessage(new FailureMessage("What You Doing?"));	
		}
	}


	// delegated method to handle teller and client login
	private void handleLogin(LoginMessage msg, ClientHandler handler) {
		if (msg.getType() == Message.TYPE.LOGIN_CLIENT) {
			handleClientLogin(msg,handler);
    	}
    	else if (msg.getType() == Message.TYPE.LOGIN_TELLER) {
    		handleTellerLogin(msg,handler);
    	}
		
	}

	private void handleClientLogin(LoginMessage msg, ClientHandler handler) {
	    String username = msg.getUsername();
	    String password = msg.getPassword();

	    ClientProfile profile = clientDatabase.get(username);
	    if (profile == null || !profile.getPassword().equals(password)) {
	        handler.sendMessage(new FailureMessage("Invalid credentials."));
	        return;
	    }

	    // Locking this specific profile for login session check
	    profileLocks.putIfAbsent(username, new ReentrantLock());
	    ReentrantLock lock = profileLocks.get(username);

	    if (!lock.tryLock()) {
	        handler.sendMessage(new FailureMessage("Client profile is already in use."));
	        return;
	    }

	    // Create session info and track it
	    SessionInfo session = new SessionInfo(username, SessionInfo.ROLE.CLIENT);
	    clientSessionIDs.put(session.getSessionID(), session);
	    

	    // Send session info back to client
	    handler.sendMessage(new SuccessMessage("Login successful.", session));
	}
	
	private void handleTellerLogin(LoginMessage msg, ClientHandler handler) {
		String username = msg.getUsername();
	    String password = msg.getPassword();
	    
	    if (username == null || tellerDatabase.get(username) != password) {
	        handler.sendMessage(new FailureMessage("Invalid credentials."));
	        return;
	    }

	    // Locking this specific profile for login session check
	    tellerLocks.putIfAbsent(username, new ReentrantLock());
	    ReentrantLock lock = tellerLocks.get(username);

	    if (!lock.tryLock()) {
	        handler.sendMessage(new FailureMessage("Teller profile is already in use."));
	        return;
	    }

	    // Create session info and track it
	    SessionInfo session = new SessionInfo(username, SessionInfo.ROLE.TELLER);
	    tellerSessionIDs.put(session.getSessionID(), session);
	    

	    // Send session info back to client
	    handler.sendMessage(new SuccessMessage("Login successful.", session));
	}
    
    // Handles Logout
    private void LogoutClient(Message msg, ClientHandler handler) {
    	// unlock profile
        String prof = handler.getActiveProfile();
        if (prof != null) {
          ReentrantLock pLock = profileLocks.get(prof);
          if (pLock.isHeldByCurrentThread()) pLock.unlock();
        }

        // unlock any accounts
        for (String acctId : handler.getActiveAccounts()) {
          ReentrantLock aLock = accountLocks.get(acctId);
          if (aLock.isHeldByCurrentThread()) aLock.unlock();
        }

        handler.clearActiveSession();
        handler.sendMessage(success("Logout","Goodbye!"));
        handler.shutDown(); // clean up socket, threads, etc.
    }
    
    private void addClient(ClientHandler handler) {
    	client_list.add(handler);
    }
    
    /** Shut down all clients (e.g. on server exit) */
    private void serverShutDown() {
        System.out.println("[Server] Shutting down, notifying clients...");
        for (ClientHandler h : client_list) {
            h.sendMessage(new Message(
                Message.TYPE.LOGOUT_CLIENT, "Server", "Server is shutting down."
            ));
            h.shutDown();
        }
    }
}
