import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
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

	/*
	 * // username -> password (could make custom teller class)
	 * private final Map<String, String> tellerDatabase = new HashMap<>();
	 * // username -> clientProfile objects
	 * private final Map<String, ClientProfile> clientDatabase = new HashMap<>();
	 * // id -> account objects
	 * private final Map<String, Account> accountDatabase = new HashMap<>();
	 * 
	 * // username -> sessionInfo
	 * private final Map<String, SessionInfo> sessionIDs = new HashMap<>();
	 */

	// checks and prevent concurrent accounts, profiles, and tellers from being
	// opened
	// username -> lock
	/*private final ConcurrentMap<String, ReentrantLock> accountLocks = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, ReentrantLock> profileLocks = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, ReentrantLock> tellerLocks = new ConcurrentHashMap<>();
*/
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
		if (!DB.getSessionIDs().containsKey(msg.getSession().getSessionID())) {
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
					handleLoadProfile((ProfileMessage) msg, handler);
					break;
				case SAVE_PROFILE:
					handleSaveProfile((ProfileMessage) msg, handler);
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
				case SAVE_PROFILE:
					handleSaveProfile((ProfileMessage) msg, handler);
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

	private void handleTransaction(TransactionMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();
		String accountID = msg.getAccountID(); // Assuming you meant account ID, not amount
		String username = session.getUsername();

		if (accountID == null || username == null) {
			handler.sendMessage(new FailureMessage("Missing session information."));
			return;
		}

		BigDecimal amount;
		try {
			amount = new BigDecimal(msg.getAmount());
		} catch (NumberFormatException e) {
			handler.sendMessage(new FailureMessage("Invalid amount format."));
			return;
		}

		Transaction.OPERATION operation = Transaction.OPERATION.valueOf(msg.getOperation().name());
		Transaction trans = new Transaction(amount.toPlainString(), operation);

		ReentrantLock lock = DB.getAccountLocks().get(accountID);
		if (lock == null || !lock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("You are not authorized to edit this account."));
			return;
		}

		try {
			Account account = DB.getAccountDatabase().get(accountID);
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
		if (username == null)
			return;

		// Unlock the profile if this thread holds the lock
		ReentrantLock profileLock = DB.getProfileLocks().get(username);
		if (profileLock != null && profileLock.isHeldByCurrentThread()) {
			profileLock.unlock();
		}

		handler.sendMessage(new SuccessMessage(
				"Profile lock released successfully."));
	}

	private void handleDeleteProfile(ProfileMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();

		String username = session.getUsername();
		if (username == null)
			return;

		// Validate credentials
		ClientProfile profile = DB.getClientDatabase().get(username);
		if (profile == null || !profile.getPassword().equals(msg.getPassword())) {
			handler.sendMessage(new FailureMessage("Invalid credentials."));
			return;
		}

		// Lock profile
		ReentrantLock plock = DB.getProfileLocks().get(username);
		if (plock == null || !plock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("You are not authorized to delete this account."));
			return;
		}

		try {
			// Remove the profile
			DB.getClientDatabase().remove(username);
			DB.getProfileLocks().remove(username); // remove lock from map

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
		ClientProfile current = DB.getClientDatabase().get(username);
		if (current == null) {
			handler.sendMessage(new FailureMessage("Profile not found."));
			return;
		}

		// Check the lock (this ensures only one active editor)
		ReentrantLock lock = DB.getProfileLocks().get(username);
		if (lock == null || !lock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("You do not own the profile lock."));
			return;
		}

		// At this point, safe to update
		current.setPhone(msg.getPhone());
		current.setAddress(msg.getAddress());
		current.setLegalName(msg.getLegalName());
		// You can add password change logic if needed
		if (session.getRole() == SessionInfo.ROLE.TELLER) {
			current.setUsername(username);
			current.setPassword(msg.getPassword());
		}

		handler.sendMessage(new SuccessMessage("Profile saved successfully."));
	}

	// client requests a profile and server attempts to retrieve using the username
	// inside session id
	// gives the profile to the client and locks that profile from being opened by
	// another client
	private void handleLoadProfile(ProfileMessage msg, ClientHandler handler) {
		String username = msg.getSession().getUsername();

		// Step 1: Valid client profile exists
		ClientProfile profile = DB.getClientDatabase().get(username);
		if (profile == null) {
			handler.sendMessage(new FailureMessage("Invalid Client Profile."));
			return;
		}

		// Step 2: Lock profile access
		DB.getProfileLocks().putIfAbsent(username, new ReentrantLock());
		ReentrantLock lock = DB.getProfileLocks().get(username);

		if (!lock.tryLock()) {
			handler.sendMessage(new FailureMessage("Client profile is currently in use."));
			return;
		}

		// Step 3: Update Session Activity
		updateLastActive(username);

		// Step 4: Create a *safe* DTO copy of the profile data
		ProfileMessage profileMsg = new ProfileMessage(
				Message.TYPE.LOAD_PROFILE,
				DB.getSessionIDs().get(username),
				profile.getUsername(),
				null, // don't send password back
				profile.getPhone(),
				profile.getAddress(),
				profile.getLegalName(),
				profile.getAccountIDs());

		// Step 5: Send the profile info (plus session info) back to the client
		handler.sendMessage(profileMsg);
	}

	// client requests an account by specifying the account id
	private void handleLoadAccount(AccountMessage msg, ClientHandler handler) {
		String username = msg.getSession().getUsername();
		String account_id = msg.getID();

		// Step 1. Check account exists in database
		Account account = DB.getAccountDatabase().get(account_id);
		if (account == null) {
			handler.sendMessage(new FailureMessage("Account not found."));
			return;
		}

		// Step 2: Check client profile owns account
		if (DB.getClientDatabase().get(username).getAccountID(account_id) == null) {
			handler.sendMessage(new FailureMessage("Unauthorized Account Access."));
			return;
		}

		// Step 3: Check if account is in use
		DB.getAccountLocks().putIfAbsent(username, new ReentrantLock());
		ReentrantLock lock = DB.getAccountLocks().get(username);

		if (!lock.tryLock()) {
			handler.sendMessage(new FailureMessage("Account is currently in use."));
			return;
		}

		// Step 4: Update Session Activity
		updateLastActive(username);

		// Step 5: Determine which account instance class it is
		// And create a suitable message to send to client
		AccountMessage accountMsg;
		if (account instanceof CheckingAccount c) {
			accountMsg = new AccountMessage(
					Message.TYPE.LOAD_ACCOUNT,
					DB.getSessionIDs().get(username),
					c.getID(),
					c.getBalance(),
					c.getTransHistory());
		} else if (account instanceof SavingAccount s) {
			accountMsg = new AccountMessage(
					Message.TYPE.LOAD_ACCOUNT,
					DB.getSessionIDs().get(username),
					s.getID(),
					s.getBalance(),
					s.getTransHistory(),
					s.getWithdrawCount(),
					s.getWithdrawLimit());
		} else if (account instanceof CreditLine l) {
			accountMsg = new AccountMessage(
					Message.TYPE.LOAD_ACCOUNT,
					DB.getSessionIDs().get(username),
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
		Account account = DB.getAccountDatabase().get(accountID);
		if (account == null) {
			handler.sendMessage(new FailureMessage("Account not found."));
			return;
		}

		// Step 2: Check client profile owns account
		if (DB.getClientDatabase().get(username).getAccountID(accountID) == null) {
			handler.sendMessage(new FailureMessage("Unauthorized Account Access."));
			return;
		}

		// Step 3: Validate session and lock
		ReentrantLock lock = DB.getAccountLocks().get(accountID);
		if (lock == null || !lock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("Account not locked for editing."));
			return;
		}

		// Step 4: Determine account type and save appropriately
		try {
			switch (msg.getAccountType()) {
				case CHECKING:
					CheckingAccount checking = (CheckingAccount) DB.getAccountDatabase().get(accountID);
					if (checking == null) {
						handler.sendMessage(new FailureMessage("Account not found."));
						return;
					}
					break;

				case SAVING:
					SavingAccount saving = (SavingAccount) DB.getAccountDatabase().get(accountID);
					if (saving == null) {
						handler.sendMessage(new FailureMessage("Account not found."));
						return;
					}
					saving.setWithdrawLimit(msg.getWithdrawalLimit());
					break;

				case CREDIT_LINE:
					CreditLine credit = (CreditLine) DB.getAccountDatabase().get(accountID);
					if (credit == null) {
						handler.sendMessage(new FailureMessage("Account not found."));
						return;
					}
					credit.setCreditLimit(msg.getCreditLimit().toString());
					break;
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
		Account account = DB.getAccountDatabase().get(accountID);
		if (account == null) {
			handler.sendMessage(new FailureMessage("Account not found."));
			return;
		}

		// Step 2: Check client profile owns account
		if (DB.getClientDatabase().get(username).getAccountID(accountID) == null) {
			handler.sendMessage(new FailureMessage("Unauthorized Account Access."));
			return;
		}

		// Step 3: Validate session and lock
		ReentrantLock lock = DB.getAccountLocks().get(accountID);
		if (lock == null || !lock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("Account not locked for editing."));
			return;
		}

		try {
			// Remove the account
			ClientProfile profile = DB.getClientDatabase().get(username);
			profile.removeAccountID(accountID);
			DB.getAccountDatabase().remove(accountID);
			DB.getAccountLocks().remove(accountID); // remove lock from map

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
		if (DB.getClientDatabase().get(username).getAccountID(username) == null)
			return;

		// Unlock the profile if this thread holds the lock
		ReentrantLock accountLock = DB.getAccountLocks().get(account_id);
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
		ClientProfile ownerProfile = DB.getClientDatabase().get(ownerUsername);
		if (ownerProfile == null) {
			handler.sendMessage(new FailureMessage("Your profile not found."));
			return;
		}

		// 2) Check that the account exists
		Account acct = DB.getAccountDatabase().get(accountId);
		if (acct == null) {
			handler.sendMessage(new FailureMessage("Account does not exist."));
			return;
		}

		// 3) Check the lock—or active-accounts—to ensure this user “owns” it
		ReentrantLock acctLock = DB.getAccountLocks().get(accountId);
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
		ClientProfile targetProfile = DB.getClientDatabase().get(targetUsername);
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

		ClientProfile profile = DB.getClientDatabase().get(username);
		if (profile == null || !profile.getPassword().equals(password)) {
			handler.sendMessage(new FailureMessage("Invalid credentials."));
			return;
		}

		// Locking this specific profile for login session check
		DB.getProfileLocks().putIfAbsent(username, new ReentrantLock());
		ReentrantLock lock = DB.getProfileLocks().get(username);

		if (!lock.tryLock()) {
			handler.sendMessage(new FailureMessage("Client profile is already in use."));
			return;
		}

		// Create session info and track it
		SessionInfo session = new SessionInfo(username, SessionInfo.ROLE.CLIENT);
		DB.getSessionIDs().put(session.getSessionID(), session);

		// Send session info back to client
		handler.sendMessage(new SuccessMessage("Login successful.", session));
	}

	private void handleTellerLogin(LoginMessage msg, ClientHandler handler) {
		String username = msg.getUsername();
		String password = msg.getPassword();

		if (username == null || DB.getTellerDatabase().get(username) != password) {
			handler.sendMessage(new FailureMessage("Invalid credentials."));
			return;
		}

		// Locking this specific profile for login session check
		DB.getTellerLocks().putIfAbsent(username, new ReentrantLock());
		ReentrantLock lock = DB.getTellerLocks().get(username);

		if (!lock.tryLock()) {
			handler.sendMessage(new FailureMessage("Teller profile is already in use."));
			return;
		}

		// Create session info and track it
		SessionInfo session = new SessionInfo(username, SessionInfo.ROLE.TELLER);
		DB.getSessionIDs().put(session.getSessionID(), session);

		// Send session info back to client
		handler.sendMessage(new SuccessMessage("Login successful.", session));
	}

	private void handleClientLogout(LogoutMessage msg, ClientHandler handler) {
		// 1) Grab and remove the session
		SessionInfo session = msg.getSession();
		if (session != null) {
			DB.getSessionIDs().remove(session.getUsername());
		}

		// 2) Unlock profile
		String username = msg.getSession().getUsername();
		if (username != null) {
			ReentrantLock pLock = DB.getProfileLocks().get(username);
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
			DB.getSessionIDs().remove(session.getUsername());
		}
		// 2) Tell the teller it’s logged out—but connection is still open
		handler.sendMessage(new SuccessMessage("Log Out Successful"));
	}

	private void addClient(ClientHandler handler) {
		client_list.add(handler);
	}

	private void updateLastActive(String username) {
		SessionInfo session = DB.getSessionIDs().get(username);
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
		DB.getSessionIDs().clear();

		// 5. Release all locks
		DB.getAccountLocks().clear();
		DB.getProfileLocks().clear();
		DB.getTellerLocks().clear();

		// 6. Optionally clear in-memory databases (if you truly want a full reset)
		// tellerDatabase.clear();
		// clientDatabase.clear();
		// accountDatabase.clear();

		System.out.println("[Server] Shutdown complete. All clients disconnected.");
	}
}
