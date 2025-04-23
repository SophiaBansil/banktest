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
    
    // username -> sessionInfo
    private final Map<String, SessionInfo> sessionIDs = new HashMap<>();
    
    // checks and prevent concurrent accounts, profiles, and tellers from being opened
    // username -> lock
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
        if (!sessionIDs.containsKey(msg.getSession().getSessionID())) {
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
    	if (msg instanceof ProfileMessage) {
			switch (msg.getType()) {
				case LOAD_PROFILE:
					handleLoadProfile((ProfileMessage) msg,handler);
					break;
				case SAVE_PROFILE:
					handleSaveProfile((ProfileMessage) msg, handler);
				case DELETE_PROFILE:
					handleSaveProfile((ProfileMessage) msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof AccountMessage) {
			switch (msg.getType()) {
				case LOAD_ACCOUNT:
					handleLoadAccount((AccountMessage) msg,handler);
					break;
				case SAVE_ACCOUNT:
					handleSaveAccount((AccountMessage) msg, handler);
					break;
				case SHARE_ACCOUNT:
					handleShareAccount((AccountMessage) msg, handler);
				default:
					break;
			}
		} else if (msg instanceof LogoutMessage) {
			switch (msg.getType()) {
				case LOGOUT_CLIENT:
					handleClientLogout((LogoutMessage) msg,handler);
					break;
				case LOGOUT_ATM:
					handleATMLogout((LogoutMessage) msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof TransactionMessage) {
			switch (msg.getType()) {
			case TRANSACTION:
				handleTransaction((TransactionMessage) msg,handler);
				break;
			default:
				break;
			}
		} else {
			handler.sendMessage(new FailureMessage("What you doing?"));
		}
	}

    private void handleTransaction(TransactionMessage msg, ClientHandler handler) {
		// TODO Auto-generated method stub
		
	}

	private void handleATMLogout(LogoutMessage msg, ClientHandler handler) {
		// TODO Auto-generated method stub
		
	}

	// delegated method to handle client functions
	private void handleClientMessages(Message msg, ClientHandler handler) {
		if (msg instanceof ProfileMessage) {
			switch (msg.getType()) {
				case LOAD_PROFILE:
					handleLoadProfile((ProfileMessage) msg,handler);
					break;
				case SAVE_PROFILE:
					handleSaveProfile(msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof AccountMessage) {
			switch (msg.getType()) {
				case LOAD_ACCOUNT:
					handleLoadAccount((AccountMessage) msg,handler);
					break;
				case SAVE_ACCOUNT:
					handleSaveAccount((AccountMessage) msg, handler);
					break;
				case SHARE_ACCOUNT:
					handleShareAccount((AccountMessage) msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof LogoutMessage) {
			switch (msg.getType()) {
				case LOGOUT_CLIENT:
					handleLoadProfile((ProfileMessage) msg,handler);
					break;
				case LOGOUT_ATM:
					handleSaveProfile(msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof TransactionMessage) {
			switch (msg.getType()) {
			case TRANSACTION:
				handleLoadProfile((ProfileMessage) msg,handler);
				break;
			default:
				break;
			}
		} else {
			handler.sendMessage(new FailureMessage("What you doing?"));
		}
	}

	private void handleShareAccount(Message msg, ClientHandler handler) {
		// TODO Auto-generated method stub
		
	}

	private void handleSaveProfile(Message msg, ClientHandler handler) {
		// TODO Auto-generated method stub
		
	}

	private void handleLoadProfile(ProfileMessage msg, ClientHandler handler) {
	    String username = msg.getSession().getUsername();

	    // Step 1: Valid client profile exists
	    ClientProfile profile = clientDatabase.get(username);
	    if (profile == null) {
	        handler.sendMessage(new FailureMessage("Invalid Client Profile."));
	        return;
	    }

	    // Step 2: Lock profile access
	    profileLocks.putIfAbsent(username, new ReentrantLock());
	    ReentrantLock lock = profileLocks.get(username);

	    if (!lock.tryLock()) {
	        handler.sendMessage(new FailureMessage("Client profile is currently in use."));
	        return;
	    }
	    
	    // Step 3: Update Session Activity
	    updateLastActive(username);
	    
	    // Step 4: Create a *safe* DTO copy of the profile data
	    ProfileMessage profileMsg = new ProfileMessage(
	        Message.TYPE.LOAD_PROFILE,
	        sessionIDs.get(username),
	        profile.getUsername(),
	        null, // don't send password back
	        profile.getPhone(),
	        profile.getAddress(),
	        profile.getLegalName(),
	        profile.getAccountIDs()
	    );

	    // Step 5: Send the profile info (plus session info) back to the client
	    handler.sendMessage(profileMsg);
	}

	private void handleSaveAccount(Message msg, ClientHandler handler) {
		// TODO Auto-generated method stub
		
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
	    sessionIDs.put(session.getSessionID(), session);
	    

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
	    sessionIDs.put(session.getSessionID(), session);
	    

	    // Send session info back to client
	    handler.sendMessage(new SuccessMessage("Login successful.", session));
	}
	
	private void handleLoadAccount(Message msg, ClientHandler handler) {
		// TODO Auto-generated method stub
		
	}
    
	private void handleClientLogout(Message msg, ClientHandler handler) {
		 // 1) Grab and remove the session
	    SessionInfo session = msg.getSession();
	    if (session != null) {
	        sessionIDs.remove(session.getUsername());
	    }

	    // 2) Unlock profile
	    String username = msg.getSession().getUsername();
	    if (username != null) {
	        ReentrantLock pLock = profileLocks.get(username);
	        if (pLock != null && pLock.isHeldByCurrentThread()) {
	            pLock.unlock();
	        }
	    }

	    // 3) Tell the client it’s logged out—but connection is still open
	    handler.sendMessage(new SuccessMessage("Log Out Successful"));	
	}
	
    // Handles Logout
    private void LogoutClient(Message msg, ClientHandler handler) {
 
    }
    
    private void addClient(ClientHandler handler) {
    	client_list.add(handler);
    }
    
    private void updateLastActive(String username) {
        SessionInfo session = sessionIDs.get(username);
        if (session != null) {
            session.setLastActive(System.currentTimeMillis());
        }
    }
    
    /** Shut down all clients and clear server state (no persistence yet) */
    private void serverShutDown() {
        System.out.println("[Server] Shutting down, notifying clients...");

        // 1. Notify clients and shut them down
//        for (ClientHandler handler : client_list) {
//            try {
//                handler.sendMessage(new Message(Message.TYPE.SHUTDOWN, null));
//            } catch (Exception e) {
//                System.err.println("Failed to notify client: " + e.getMessage());
//            } finally {
//                handler.shutDown(); // Closes socket, stops thread
//            }
//        }

        // 2. Clear client handler list
        client_list.clear();

        // 3. Clear session IDs
        sessionIDs.clear();

        // 4. Release all locks
        accountLocks.clear();
        profileLocks.clear();
        tellerLocks.clear();

        // 5. Optionally clear in-memory databases (if you truly want a full reset)
        // tellerDatabase.clear();
        // clientDatabase.clear();
        // accountDatabase.clear();

        System.out.println("[Server] Shutdown complete. All clients disconnected.");
    }
}
