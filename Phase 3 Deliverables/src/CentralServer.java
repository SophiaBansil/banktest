import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class CentralServer {
	// file to hold persistent database data
	private static final String saveFile = "database.ser";
	private final Database DB;

	
	  // username -> password 
	 private final Map<String, String> tellerDatabase;
	 // username -> clientProfile objects
	 private final Map<String, ClientProfile> clientDatabase;
	 // id -> account objects
	 private final Map<String, Account> accountDatabase;
	 
	 // username -> sessionInfo
	 private final Map<String, SessionInfo> sessionIDs = new HashMap<>();
	 

	// checks and prevent concurrent accounts, profiles, and tellers from being
	// opened
	// username -> lock
	private final ConcurrentMap<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, ReentrantLock> profileLocks = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, ReentrantLock> tellerLocks = new ConcurrentHashMap<>();

	// thread-safe variant of ArrayList in Java
	// best choice if read operation is most frequently used
	private final List<ClientHandler> client_list = new CopyOnWriteArrayList<>();

	public CentralServer() {
		Database fooDB = Database.loadDatabase(saveFile);
		// load in database info
		Database.loadDatabase(saveFile);
		if (fooDB == null) {
			DB = Database.getInstance();
		} else {
			this.DB = fooDB;
		}

		// initialize data structures owned by CentralServer;
		this.tellerDatabase  = DB.getTellerDatabase();
        this.clientDatabase  = DB.getClientDatabase();
        this.accountDatabase = DB.getAccountDatabase();
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
		// handles disconnects (whether authenticated or not)
		if (msg.getType() == Message.TYPE.DISCONNECT) {
			handleClientDisconnect((DisconnectMessage) msg, handler);
		}
		// Only allow login messages before authentication
		if (msg.getSession() == null) {
			if (msg instanceof LoginMessage) {
				handleLogin((LoginMessage) msg, handler);
			} else {
				handler.sendMessage(new FailureMessage("You must log in first."));
			}
			return;
		}
		if (sessionIDs.get(msg.getSession().getSessionID()) == null) {
			handler.sendMessage(new FailureMessage("Unauthorized Client."));
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

	private void handleClientDisconnect(DisconnectMessage msg, ClientHandler handler) {
		String username = msg.getSession().getUsername();
	    System.out.println("[Server] Client " + username + " has disconnected.");
	    // Release locks, cleanup if needed
	    ReentrantLock profileLock = profileLocks.get(username);
	    if (profileLock != null && profileLock.isHeldByCurrentThread()) {
	        profileLock.unlock();
	        profileLocks.remove(username); // Clean up
	    }

	    ClientProfile profile = clientDatabase.get(username);
	    String[] accountIDs = profile.getAccountIDs();
	    // Handle account lock
	    for (String accountID : accountIDs) {
	        ReentrantLock accountLock = accountLocks.get(accountID);
	        if (accountLock != null && accountLock.isHeldByCurrentThread()) {
	            accountLock.unlock();
	            accountLocks.remove(accountID); // Clean up
	        }
	    }
	    client_list.remove(handler);
	}
	// delegated method to handle teller functions
	private void handleTellerMessage(Message msg, ClientHandler handler) {
		if (msg instanceof ProfileMessage) {
			switch (msg.getType()) {
				case LOAD_PROFILE:
					handleLoadProfile((ProfileMessage) msg, handler);
					break;
				case SAVE_PROFILE:
					handleSaveProfile((ProfileMessage) msg, handler);
					break;
				case CREATE_PROFILE:
					handleCreateProfile((ProfileMessage) msg, handler);
					break;
				case DELETE_PROFILE:
					handleDeleteProfile((ProfileMessage) msg, handler);
					break;
				case EXIT_PROFILE:
					handleExitProfile((ProfileMessage) msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof AccountMessage) {
			switch (msg.getType()) {
				case LOAD_ACCOUNT:
					handleLoadAccount((AccountMessage) msg, handler);
					break;
				case SAVE_ACCOUNT:
					handleSaveAccount((AccountMessage) msg, handler);
					break;
				case DELETE_ACCOUNT:
					handleDeleteAccount((AccountMessage) msg, handler);
					break;
				case CREATE_ACCOUNT:
					handleCreateAccount((AccountMessage) msg, handler);
					break;
				case EXIT_ACCOUNT:
					handleExitAccount((AccountMessage) msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof LogoutMessage) {
			if (msg.getType() == Message.TYPE.LOGOUT_TELLER) {
				handleTellerLogout((LogoutMessage) msg, handler);
			} else {

			}
		} else if (msg instanceof TransactionMessage) {
			switch (msg.getType()) {
				case TRANSACTION:
					handleTransaction((TransactionMessage) msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof ShareAccountMessage) {
			switch (msg.getType()) {
				case SHARE_ACCOUNT:
					handleShareAccount((ShareAccountMessage) msg, handler);
					break;
				default:
					break;
			}
		} else {
			handler.sendMessage(new FailureMessage("What you doing?"));
		}
	}

	// delegated method to handle client functions
	private void handleClientMessages(Message msg, ClientHandler handler) {
		if (msg instanceof ProfileMessage) {
			switch (msg.getType()) {
				case LOAD_PROFILE:
					handleLoadProfile((ProfileMessage) msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof AccountMessage) {
			switch (msg.getType()) {
				case LOAD_ACCOUNT:
					handleLoadAccount((AccountMessage) msg, handler);
					break;
				case SAVE_ACCOUNT:
					handleSaveAccount((AccountMessage) msg, handler);
					break;
				case EXIT_ACCOUNT:
					handleExitAccount((AccountMessage) msg, handler);
				default:
					break;
			}
		} else if (msg instanceof LogoutMessage) {
			switch (msg.getType()) {
				case LOGOUT_CLIENT:
					handleClientLogout((LogoutMessage) msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof TransactionMessage) {
			switch (msg.getType()) {
				case TRANSACTION:
					handleLoadProfile((ProfileMessage) msg, handler);
					break;
				default:
					break;
			}
		} else if (msg instanceof ShareAccountMessage) {
			switch (msg.getType()) {
				case SHARE_ACCOUNT:
					handleShareAccount((ShareAccountMessage) msg, handler);
					break;
				default:
					break;
			}
		} else {
			handler.sendMessage(new FailureMessage("What you doing?"));
		}
	}

	private void handleCreateAccount(AccountMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();
		String username = session.getUsername();
	
		ClientProfile client = clientDatabase.get(username);
		if (client == null) {
			handler.sendMessage(new FailureMessage("Client profile not found."));
			return;
		}
	
		Account newAccount;
		try {
			switch (msg.getAccountType()) {
				case CHECKING:
					newAccount = new CheckingAccount();
					break;
				case SAVING:
					newAccount = new SavingAccount(msg.getWithdrawLimit());
					break;
				case CREDIT_LINE:
					newAccount = new CreditLine(msg.getCreditLimit().toPlainString());
					break;
				default:
					handler.sendMessage(new FailureMessage("Unsupported account type."));
					return;
			}
		} catch (Exception e) {
			handler.sendMessage(new FailureMessage("Failed to create account: " + e.getMessage()));
			return;
		}

		
		accountDatabase.put(newAccount.getID(), newAccount);
		client.addAccountID(newAccount.getID());  // Assuming `ClientProfile` has this method
		accountLocks.putIfAbsent(newAccount.getID(), new ReentrantLock());

		// Step 5: Confirm success
		handler.sendMessage(new SuccessMessage("New account created successfully."));
	}

	private void handleTransaction(TransactionMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();
		String accountID = msg.getAccountID(); 
		String username = session.getUsername();

		if (accountID == null || username == null) {
			handler.sendMessage(new FailureMessage("Missing session information."));
			return;
		}

		BigDecimal amount;
		try {
			amount = new BigDecimal(msg.getAmount());
			if (amount.compareTo(BigDecimal.ZERO) <= 0) {
	            handler.sendMessage(new FailureMessage("Amount must be positive."));
	            return;
	        }
		} catch (NumberFormatException e) {
			handler.sendMessage(new FailureMessage("Invalid amount format."));
			return;
		}

		Transaction.OPERATION operation = Transaction.OPERATION.valueOf(msg.getOperation().name());
		Transaction trans = new Transaction(amount.toPlainString(), operation);

		ReentrantLock lock = accountLocks.get(accountID);
		if (lock == null || !lock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("You are not authorized to edit this account."));
			return;
		}

		try {
			Account account =this.accountDatabase.get(accountID);
			if (account == null) {
				handler.sendMessage(new FailureMessage("Account not found."));
				return;
			}

			try {
				account.addTransaction(trans);
				handler.sendMessage(new SuccessMessage("Transaction applied successfully."));
			} catch (IllegalArgumentException | IllegalStateException e) {
				handler.sendMessage(new FailureMessage(e.getMessage()));
			}

		} finally {
			lock.unlock();
		}
	}

	private void handleExitProfile(ProfileMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();

		String username = session.getUsername();
	    if (username == null) {
	        handler.sendMessage(new FailureMessage("Session is missing username."));
	        return;
	    }

	    ReentrantLock profileLock = profileLocks.get(username);
	    if (profileLock == null) {
	        handler.sendMessage(new FailureMessage("No profile lock found to release."));
	        return;
	    }

	    if (profileLock.isHeldByCurrentThread()) {
	        profileLock.unlock();
	        handler.sendMessage(new SuccessMessage("Profile lock released successfully."));
	    } else {
	        handler.sendMessage(new FailureMessage("Current thread does not own the profile lock."));
	    }
	}

	private void handleDeleteProfile(ProfileMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();

		String username = session.getUsername();
		if (username == null)
			return;

		// Validate credentials
		ClientProfile profile = this.clientDatabase.get(username);
		if (profile == null || !profile.getPassword().equals(msg.getPassword())) {
			handler.sendMessage(new FailureMessage("Invalid credentials."));
			return;
		}

		// Lock profile
		ReentrantLock plock = profileLocks.get(username);
		if (plock == null || !plock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("You are not authorized to delete this account."));
			return;
		}

		try {
			// Remove the profile
			this.clientDatabase.remove(username);
			profileLocks.remove(username); // remove lock from map

			handler.sendMessage(new SuccessMessage("Profile deleted successfully."));
		} finally {
			// Always unlock even if an error occurs
			if (plock.isHeldByCurrentThread()) {
				plock.unlock();
			}
		}
	}

	private void handleSaveProfile(ProfileMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();

		String username = session.getUsername();
		ClientProfile current = this.clientDatabase.get(username);
		if (current == null) {
			handler.sendMessage(new FailureMessage("Profile not found."));
			return;
		}

		// check if locked
		ReentrantLock lock = profileLocks.get(username);
		if (lock == null || !lock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("You do not own the profile lock."));
			return;
		}

		current.setPhone(msg.getPhone());
		current.setAddress(msg.getAddress());
		current.setLegalName(msg.getLegalName());
			current.setUsername(username);
			current.setPassword(msg.getPassword());

		handler.sendMessage(new SuccessMessage("Profile saved successfully."));
	}

	// client requests a profile and server attempts to retrieve using the username
	// inside session id
	// gives the profile to the client and locks that profile from being opened by
	// another client
	private void handleLoadProfile(ProfileMessage msg, ClientHandler handler) {
		String username = msg.getSession().getUsername();

		// Step 1: Valid client profile exists
		ClientProfile profile = this.clientDatabase.get(username);
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

		// Step 4: Build AccountSummary list
        List<AccountSummary> summaries = new ArrayList<>();
        for (String accID : profile.getAccountIDs()) {
            Account acc = accountDatabase.get(accID);
            if (acc != null) {
            	if (acc instanceof CheckingAccount) {
            		summaries.add(new AccountSummary(
            				acc.getID(), 
            				AccountSummary.ACCOUNT_TYPE.CHECKING, 
            				acc.getBalance()));
            	}
            	else if (acc instanceof SavingAccount) {
            		summaries.add(new AccountSummary(
            				acc.getID(), 
            				AccountSummary.ACCOUNT_TYPE.SAVING, 
            				acc.getBalance()));
            	}
            	else {
            		summaries.add(new AccountSummary(
            				acc.getID(), 
            				AccountSummary.ACCOUNT_TYPE.CREDIT_LINE, 
            				acc.getBalance()));
            	}
            }
        }

        // Step 5: Send ProfileMessage with summaries
        ProfileMessage profileMsg = new ProfileMessage(
            Message.TYPE.LOAD_PROFILE,
            sessionIDs.get(username),
            profile.getUsername(),
            profile.getPassword(),
            profile.getPhone(),
            profile.getAddress(),
            profile.getLegalName(),
            summaries
        );
        
		// Step 6: Send the profile info (plus session info) back to the client
		handler.sendMessage(profileMsg);
	}

	private void handleCreateProfile(ProfileMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();
		String username = session.getUsername();

		// Step 1: Check if profile already exists
		if (clientDatabase.containsKey(username)) {
			handler.sendMessage(new FailureMessage("USERNAME TAKEN"));
			return;
		}

		// Step 2: Create a new ClientProfile
		ClientProfile newProfile = new ClientProfile(
				msg.getUsername(),
				msg.getPassword(),
				msg.getPhone(),
				msg.getAddress(),
				msg.getLegalName());

		// Step 3: Save the new profile to the database
		clientDatabase.put(username, newProfile);

		// Step 4: Create a lock for this profile
		profileLocks.putIfAbsent(username, new ReentrantLock());

		// Step 5: Confirm success
		handler.sendMessage(new SuccessMessage("New profile created successfully."));
	}

	// client requests an account by specifying the account id
	private void handleLoadAccount(AccountMessage msg, ClientHandler handler) {
		String username = msg.getSession().getUsername();
		String account_id = msg.getID();

		// Step 1. Check account exists in database
		Account account = this.accountDatabase.get(account_id);
		if (account == null) {
			handler.sendMessage(new FailureMessage("Account not found."));
			return;
		}

		// Step 2: Check client profile owns account
		if (this.clientDatabase.get(username).getAccountID(account_id) == null) {
			handler.sendMessage(new FailureMessage("Unauthorized Account Access."));
			return;
		}

		// Step 3: Check if account is in use
		accountLocks.putIfAbsent(username, new ReentrantLock());
		ReentrantLock lock = accountLocks.get(username);

		if (!lock.tryLock()) {
			handler.sendMessage(new FailureMessage("Account is currently in use."));
			return;
		}

		// Step 4: Update Session Activity
		updateLastActive(username);

		// Step 5: Determine which account instance class it is
		// And create a suitable message to send to client
		AccountMessage accountMsg;
		SessionInfo session = sessionIDs.get(username);

		if (account instanceof CheckingAccount c) {
			accountMsg = new AccountMessage(
					Message.TYPE.LOAD_ACCOUNT,
					sessionIDs.get(username),
					c.getID(),
					c.getBalance(),
					c.getTransHistory());
		} else if (account instanceof SavingAccount s) {
			accountMsg = new AccountMessage(
					Message.TYPE.LOAD_ACCOUNT,
					sessionIDs.get(username),
					s.getID(),
					s.getBalance(),
					s.getTransHistory(),
					s.getWithdrawCount(),
					s.getWithdrawLimit());
		} else if (account instanceof CreditLine l) {
			accountMsg = new AccountMessage(
					Message.TYPE.LOAD_ACCOUNT,
					sessionIDs.get(username),
					l.getID(),
					l.getBalance(),
					l.getTransHistory(),
					l.getCreditLimit());
		} else {
			handler.sendMessage(new FailureMessage("Unsupported account type."));
			lock.unlock();
			return;
		}
		// Step 6: Send Account Information over network
		handler.sendMessage(accountMsg);
	}

	private void handleSaveAccount(AccountMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();
		String username = session.getUsername();
		String accountID = msg.getID();

		// Step 1. Check account exists in database
		Account account = this.accountDatabase.get(accountID);
		if (account == null) {
			handler.sendMessage(new FailureMessage("Account not found."));
			return;
		}

		// Step 2: Check client profile owns account
		if (this.clientDatabase.get(username).getAccountID(accountID) == null) {
			handler.sendMessage(new FailureMessage("Unauthorized Account Access."));
			return;
		}

		// Step 3: Validate session and lock
		ReentrantLock lock = accountLocks.get(accountID);
		if (lock == null || !lock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("Account not locked for editing."));
			return;
		}

		// Step 4: Determine account type and save appropriately
		try {
			switch (msg.getAccountType()) {
				case CHECKING: // redundant but additional functionality could be added later
//					if (!(account instanceof CheckingAccount checking)) {
//						handler.sendMessage(new FailureMessage("Account type mismatch."));
//						return;
//					}
//					break;
				case SAVING:
					if (!(account instanceof SavingAccount saving)) {
						handler.sendMessage(new FailureMessage("Account type mismatch."));
						return;
					}
					saving.setWithdrawCount(msg.getWithdrawCount());
					saving.setWithdrawLimit(msg.getWithdrawLimit());
					break;
				case CREDIT_LINE:
					if (!(account instanceof CreditLine credit)) {
						handler.sendMessage(new FailureMessage("Account type mismatch."));
						return;
					}
					credit.setCreditLimit(msg.getCreditLimit().toString());
					break;
				default:
					handler.sendMessage(new FailureMessage("Unsupported account type."));
					return;
				}
			handler.sendMessage(new SuccessMessage("Account saved successfully."));
		} catch (Exception e) {
			handler.sendMessage(new FailureMessage("Failed to save account: " + e.getMessage()));
		}
	}

	private void handleDeleteAccount(AccountMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();
		String username = session.getUsername();
		String accountID = msg.getID();

		// Step 1. Check account exists in database
		Account account = this.accountDatabase.get(accountID);
		if (account == null) {
			handler.sendMessage(new FailureMessage("Account not found."));
			return;
		}

		// Step 2: Check client profile owns account
		if (this.clientDatabase.get(username).getAccountID(accountID) == null) {
			handler.sendMessage(new FailureMessage("Unauthorized Account Access."));
			return;
		}

		// Step 3: Validate session and lock
		ReentrantLock lock = accountLocks.get(accountID);
		if (lock == null || !lock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("Account not locked for editing."));
			return;
		}

		try {
			// Remove the account
			ClientProfile profile = this.clientDatabase.get(username);
			profile.removeAccountID(accountID);
			this.accountDatabase.remove(accountID);
			accountLocks.remove(accountID); // remove lock from map

			handler.sendMessage(new SuccessMessage("Account deleted successfully."));
		} finally {
			// Always unlock even if an error occurs
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private void handleExitAccount(AccountMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();

		String username = session.getUsername();
		if (username == null)
			return;

		String account_id = msg.getID();
		if (this.clientDatabase.get(username).getAccountID(username) == null)
			return;

		// Unlock the profile if this thread holds the lock
		ReentrantLock accountLock = accountLocks.get(account_id);
		if (accountLock != null && accountLock.isHeldByCurrentThread()) {
			accountLock.unlock();
		}

		handler.sendMessage(new SuccessMessage(
				"Account lock released successfully."));
	}

	private void handleShareAccount(ShareAccountMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();
		if (session == null) {
			handler.sendMessage(new FailureMessage("Not authenticated."));
			return;
		}
		String ownerUsername = session.getUsername();
		String accountId = msg.getSharedAccountID();
		String targetUsername = msg.getTargetProfile();

		// 1) Verify source profile exists
		ClientProfile ownerProfile = this.clientDatabase.get(ownerUsername);
		if (ownerProfile == null) {
			handler.sendMessage(new FailureMessage("Your profile not found."));
			return;
		}

		// 2) Check that the account exists
		Account acct = this.accountDatabase.get(accountId);
		if (acct == null) {
			handler.sendMessage(new FailureMessage("Account does not exist."));
			return;
		}

		// 3) Check the lock—or active-accounts—to ensure this user “owns” it
		ReentrantLock acctLock = accountLocks.get(accountId);
		if (acctLock == null || !acctLock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("You must have the account open before sharing."));
			return;
		}

		// 5) Verify the owner actually has the account in their profile
		if (!(ownerProfile.getAccountID(accountId) == null)) {
			handler.sendMessage(new FailureMessage("You do not own that account."));
			return;
		}

		// 6) Look up the target profile
		ClientProfile targetProfile = this.clientDatabase.get(targetUsername);
		if (targetProfile == null) {
			handler.sendMessage(new FailureMessage("Target user does not exist."));
			return;
		}

		// 7) Add the account ID to the target (no lock needed on their side)
		synchronized (targetProfile) {
			targetProfile.addAccountID(accountId);
		}

		// 8) Success!
		handler.sendMessage(new SuccessMessage(
				"Account " + accountId + " shared with " + targetUsername + "."));

	}

	// delegated method to handle teller and client login
	private void handleLogin(LoginMessage msg, ClientHandler handler) {
		if (msg.getType() == Message.TYPE.LOGIN_CLIENT) {
			handleClientLogin(msg, handler);
		} else if (msg.getType() == Message.TYPE.LOGIN_TELLER) {
			handleTellerLogin(msg, handler);
		}

	}

	private void handleClientLogin(LoginMessage msg, ClientHandler handler) {
		String username = msg.getUsername();
		String password = msg.getPassword();

		ClientProfile profile = this.clientDatabase.get(username);
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

		if (username == null || this.tellerDatabase.get(username) != password) {
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

	private void handleClientLogout(LogoutMessage msg, ClientHandler handler) {
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

	// Handles Logout for Tellers
	private void handleTellerLogout(LogoutMessage msg, ClientHandler handler) {
		// 1) Grab and remove the session
		SessionInfo session = msg.getSession();
		if (session != null) {
			sessionIDs.remove(session.getUsername());
		}
		// 2) Tell the teller it’s logged out—but connection is still open
		handler.sendMessage(new SuccessMessage("Log Out Successful"));
	}

	private void addClient(ClientHandler handler) {
		client_list.add(handler);
	}

	private void updateLastActive(String username) {
		SessionInfo session = this.sessionIDs.get(username);
		if (session != null) {
			session.setLastActive(System.currentTimeMillis());
		}
	}

	/** Shut down all clients and clear server state (no persistence yet) */
	private void serverShutDown() {
		System.out.println("[Server] Shutting down, notifying clients...");

		// 1. Notify clients and shut them down
		// for (ClientHandler handler : client_list) {
		// try {
		// handler.sendMessage(new Message(Message.TYPE.SHUTDOWN, null));
		// } catch (Exception e) {
		// System.err.println("Failed to notify client: " + e.getMessage());
		// } finally {
		// handler.shutDown(); // Closes socket, stops thread
		// }
		// }

		// 2. save and serialize everything in the Database class
		Database.getInstance().saveDatabase(saveFile);

		// 3. Clear client handler list
		client_list.clear();

		// 4. Clear session IDs
		sessionIDs.clear();

		// 5. Release all locks
		accountLocks.clear();
		profileLocks.clear();
		tellerLocks.clear();

		// 6. clear databases
		this.tellerDatabase.clear();
		this.clientDatabase.clear();
		this.accountDatabase.clear();

		System.out.println("[Server] Shutdown complete. All clients disconnected.");
	}
}
