package com.bankapp.server;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.bankapp.common.Account;
import com.bankapp.common.AccountMessage;
import com.bankapp.common.AccountSummary;
import com.bankapp.common.CheckingAccount;
import com.bankapp.common.ClientProfile;
import com.bankapp.common.CreditLine;
import com.bankapp.common.DisconnectMessage;
import com.bankapp.common.FailureMessage;
import com.bankapp.common.LoginMessage;
import com.bankapp.common.LogoutMessage;
import com.bankapp.common.Message;
import com.bankapp.common.ProfileMessage;
import com.bankapp.common.SavingAccount;
import com.bankapp.common.SessionInfo;
import com.bankapp.common.ShareAccountMessage;
import com.bankapp.common.ShutDownMessage;
import com.bankapp.common.SuccessMessage;
import com.bankapp.common.Transaction;
import com.bankapp.common.TransactionMessage;
import com.bankapp.common.Message.TYPE;
import com.bankapp.common.SessionInfo.ROLE;
import com.bankapp.common.Transaction.OPERATION;

public class CentralServer {
	// file to hold persistent database data
	// private static final String saveFile = "src/com/bankapp/server/database.ser";
	// // ABSOLUTELY MUST HAVE SAME FILEPATH!!
	// private final Database DB;

	// username -> password
	private final Map<String, String> tellerDatabase;
	// username -> clientProfile objects
	private final Map<String, ClientProfile> clientDatabase;
	// id -> account objects
	private final Map<String, Account> accountDatabase;
	private int nextAccountId;

	// session_ids -> sessionInfo
	private final ConcurrentMap<String, SessionInfo> sessionIDs = new ConcurrentHashMap<>();

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
		/*
		 * Database fooDB = Database.loadDatabase(saveFile);
		 * boolean isFresh = (fooDB == null);
		 * 
		 * // load in database info if needed
		 * if (isFresh) {
		 * System.out.println("Starting with fresh database");
		 * DB = Database.getInstance();
		 * 
		 * } else {
		 * this.DB = fooDB;
		 * }
		 */

		// initialize data structures owned by CentralServer;
		this.tellerDatabase = new ConcurrentHashMap<>(); // DB.getTellerDatabase();
		this.clientDatabase = new ConcurrentHashMap<>(); // DB.getClientDatabase();
		this.accountDatabase = new ConcurrentHashMap<>();// DB.getAccountDatabase();

		// if (isFresh) {

		ClientProfile alice = new ClientProfile(
				"alice", "pass123", "555-0001", "123 Maple St.", "Alice Anderson");

		CheckingAccount aliceChk = new CheckingAccount();
		aliceChk.addTransaction(new Transaction("500.00", Transaction.OPERATION.DEPOSIT));
		String aliceChkId = generateNewAccountId();
		aliceChk.setID(aliceChkId);
		accountDatabase.putIfAbsent(aliceChkId, aliceChk);
		alice.addAccountID(aliceChkId);

		clientDatabase.putIfAbsent("alice", alice);

		ClientProfile bob = new ClientProfile(
				"bob", "password", "555-0002", "456 Oak Ave.", "Bob Brown");
		CheckingAccount bobChk = new CheckingAccount();
		bobChk.addTransaction(new Transaction("1500.00", Transaction.OPERATION.DEPOSIT));
		String bobChkId = generateNewAccountId();
		bobChk.setID(bobChkId);
		accountDatabase.putIfAbsent(bobChkId, bobChk);
		bob.addAccountID(bobChkId);

		clientDatabase.putIfAbsent("bob", bob);

		// teller
		tellerDatabase.putIfAbsent("teller1", "letmein");

		// }
	}

	// Runs the actual server
	public static void main(String[] args) {
		CentralServer serverInstance = new CentralServer(); // <== Create instance

		// In case the server unexpectedly or forcefully closes
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("[Server] Shutdown initiated.");
			serverInstance.serverShutDown();
		}));

		Thread consoleListener = new Thread(() -> {
			Scanner scanner = new Scanner(System.in);
			System.out.println("[Server] Enter 'shutdown' or 'quit' in terminal to stop server.");
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.equalsIgnoreCase("shutdown") ||
						line.equalsIgnoreCase("quit")) {
					System.out.println("[Server] Shutdown command received.");
					serverInstance.serverShutDown();
					System.exit(0);
				}
			}
		});
		consoleListener.setDaemon(true); // won’t block JVM exit
		consoleListener.start();

		try (ServerSocket server = new ServerSocket(7777)) {
			System.out.println("[Server] Server Initiated on port 7777.");
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


	public int getNextAccountId() { 
        return this.nextAccountId;
    }

    // this will be called in handleCreateAccount()
    public void setNextAccountID(int n){
        this.nextAccountId = n;
    }
	// helper for new account creation
	private String generateNewAccountId() {
		int nextNum = getNextAccountId();
		String newId = "ACC" + nextNum;
		setNextAccountID(nextNum + 1);
		return newId;

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
		// update client's activity after receiving a message from them
		updateLastActive(msg.getSession().getUsername());
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
			try {
				if (profileLock.getHoldCount() > 0) {
					profileLock.unlock();
					System.out.println("Released profile lock for: " + username);
				}
			} catch (IllegalMonitorStateException e) {
				System.err.println("Error releasing profile lock for:  " + username + e.getMessage());
			}
			profileLocks.remove(username); // Clean up
		}

		ClientProfile profile = clientDatabase.get(username);
		String[] accountIDs = profile.getAccountIDs().toArray(new String[profile.getAccountIDs().size()]);
		;
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
				case DELETE_PROFILE:
					handleDeleteProfile((ProfileMessage) msg, handler);
					break;
				case EXIT_PROFILE:
					handleExitProfile((ProfileMessage) msg, handler);
					break;
				case CREATE_PROFILE:
					handleCreateProfile((ProfileMessage) msg, handler);
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

	private void handleTransaction(TransactionMessage msg, ClientHandler handler) {
		// 1. Parse amount
		BigDecimal amount;
		BigDecimal foo;
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

		// 2. Acquire the lock
		String accountID = msg.getAccountID();
		ReentrantLock lock = accountLocks.get(accountID);
		try {
			if (lock == null || !lock.isHeldByCurrentThread()) {
				handler.sendMessage(new FailureMessage("You are not authorized to edit this account."));
				return;
			}

			// 3. Load & authorize
			Account account = accountDatabase.get(accountID);
			if (account == null) {
				handler.sendMessage(new FailureMessage("Account not found."));
				return;
			}
			String username = msg.getSession().getUsername();
			if (clientDatabase.get(username).getAccountID(accountID) == null) {
				handler.sendMessage(new FailureMessage("Not authorized for this account."));
				return;
			}

			Transaction.OPERATION operation = Transaction.OPERATION.valueOf(msg.getOperation().name());
			// 4. Attempt the transaction
			if (operation == Transaction.OPERATION.WITHDRAW) {
				foo = amount.negate();
			} else {
				foo = amount;
			}
			Transaction tx = new Transaction(foo.toPlainString(), operation);
			account.addTransaction(tx);

			handler.sendMessage(new SuccessMessage("Transaction applied successfully."));

		} catch (IllegalStateException | IllegalArgumentException e) {
			// Your subclasses throw IllegalStateException for anything from overdraft to
			// credit-limit
			handler.sendMessage(new FailureMessage(e.getMessage()));
		}
	}

	private void handleExitProfile(ProfileMessage msg, ClientHandler handler) {

		String username = msg.getUsername();

		if (username == null) {
			handler.sendMessage(new FailureMessage("Username missing in exit profile request."));
			return;
		}

		boolean released = releaseUserProfileLock(username);

		if (released) {
			handler.sendMessage(new SuccessMessage("Exited profile successfully. Lock released."));
		} else {
			handler.sendMessage(new FailureMessage("Failed to release profile lock (check server logs)."));
		}
	}

	private void handleDeleteProfile(ProfileMessage msg, ClientHandler handler) {

		String username = msg.getUsername();
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
		plock.lock();
		if (plock == null || !plock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("You are not authorized to delete this account."));
			return;
		}

		try {
			// remove from db
			synchronized (clientDatabase) {
				clientDatabase.remove(username);
			}
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

		String username = msg.getUsername();
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
		String username = msg.getUsername();

		// Step 1: Valid client profile exists
		ClientProfile profile = this.clientDatabase.get(username);
		if (profile == null) {
			handler.sendMessage(new FailureMessage("Invalid Client Profile."));
			return;
		}

		// Step 2: Lock profile access
		// computeIfAbsent handles race conditions
		ReentrantLock lock = profileLocks.computeIfAbsent(msg.getUsername(), key -> new ReentrantLock());
		if (!lock.tryLock()) {
			handler.sendMessage(new FailureMessage("Client profile is currently in use."));
			return;
		}

		// Step 3: Update Session Activity
		// updateLastActive(msg.getSession().getUsername());

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
				} else if (acc instanceof SavingAccount) {
					summaries.add(new AccountSummary(
							acc.getID(),
							AccountSummary.ACCOUNT_TYPE.SAVING,
							acc.getBalance()));
				} else {
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
				summaries);

		// Step 6: Send the profile info (plus session info) back to the client
		handler.sendMessage(profileMsg);
	}

	private void handleCreateProfile(ProfileMessage msg, ClientHandler handler) {
		String username = msg.getUsername();

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
		String username = msg.getUsername();
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
		accountLocks.putIfAbsent(account_id, new ReentrantLock());
		ReentrantLock lock = accountLocks.get(account_id);

		if (!lock.tryLock()) {
			handler.sendMessage(new FailureMessage("Account is currently in use."));
			return;
		}

		// Step 4: Update Session Activity
		// updateLastActive(username);

		// Step 5: Determine account type and create appropriate message
		AccountMessage accountMsg;
		SessionInfo session = sessionIDs.get(username); // cache session for reuse

		if (account instanceof CheckingAccount c) {
			accountMsg = new AccountMessage(
					Message.TYPE.LOAD_ACCOUNT,
					session,
					username,
					c.getID(),
					c.getBalance(),
					c.getTransactionHistory());
		} else if (account instanceof SavingAccount s) {
			accountMsg = new AccountMessage(
					Message.TYPE.LOAD_ACCOUNT,
					session,
					username,
					s.getID(),
					s.getBalance(),
					s.getTransactionHistory(),
					s.getWithdrawCount(),
					s.getWithdrawLimit(),
					s.getReset());
		} else if (account instanceof CreditLine l) {
			accountMsg = new AccountMessage(
					Message.TYPE.LOAD_ACCOUNT,
					session,
					username,
					l.getID(),
					l.getBalance(),
					l.getTransactionHistory(),
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
		String username;
		if (session.getRole() == SessionInfo.ROLE.CLIENT) {
			username = session.getUsername();
		} else {
			username = msg.getUsername();
		}
		String accountID = msg.getID();

		// Step 1: Check account exists
		Account account = this.accountDatabase.get(accountID);
		if (account == null) {
			handler.sendMessage(new FailureMessage("Account not found."));
			return;
		}

		// Step 2: Verify client owns this account
		if (this.clientDatabase.get(username).getAccountID(accountID) == null) {
			handler.sendMessage(new FailureMessage("Unauthorized Account Access."));
			return;
		}

		// Step 3: Ensure account is locked by this thread/session
		ReentrantLock lock = accountLocks.get(accountID);
		if (lock == null || !lock.isHeldByCurrentThread()) {
			handler.sendMessage(new FailureMessage("Account not locked for editing."));
			return;
		}

		// Step 4: Save updates based on account type
		try {
			switch (msg.getAccountType()) {
				// case CHECKING: // redundant but additional functionality could be added later
				// if (!(account instanceof CheckingAccount checking)) {
				// handler.sendMessage(new FailureMessage("Account type mismatch."));
				// return;
				// }
				// break;
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

	private void handleCreateAccount(AccountMessage msg, ClientHandler handler) {
		String username = msg.getUsername();

		// Step 1: Check if client profile exists
		ClientProfile client = clientDatabase.get(username);
		if (client == null) {
			handler.sendMessage(new FailureMessage("Client profile not found."));
			return;
		}

		// Step 2: Create the account based on type (ID is generated inside the
		// constructor)
		Account newAccount;
		try {
			switch (msg.getAccountType()) {
				case CHECKING:
					newAccount = new CheckingAccount();
					break;
				case SAVING:
					if (msg.getWithdrawLimit() == 0) {
						newAccount = new SavingAccount(); // withdrawal limit will be set to DEFAULT
					} else {
						newAccount = new SavingAccount(msg.getWithdrawLimit());
					}
					break;
				case CREDIT_LINE:
					// Check for a checking account with at least $1000
					boolean eligible = false;
					for (String id : client.getAccountIDs()) {
						Account acct = accountDatabase.get(id);
						if (acct instanceof CheckingAccount checking &&
								checking.getBalance().compareTo(new BigDecimal("1000.00")) >= 0) {
							eligible = true;
							break;
						}
					}

					if (!eligible) {
						handler.sendMessage(new FailureMessage(
								"Client must have a CHECKING account with at least $1000 to open a CREDIT LINE."));
						return;
					}
					newAccount = new CreditLine(msg.getCreditLimit().toString());
					break;
				default:
					handler.sendMessage(new FailureMessage("Unsupported account type."));
					return;
			}
		} catch (Exception e) {
			handler.sendMessage(new FailureMessage("Failed to create account: " + e.getMessage()));
			return;
		}

		// assign ID
		String newAccountId = generateNewAccountId();
		newAccount.setID(newAccountId);

		// Step 4: Register the account in the system
		accountDatabase.put(newAccount.getID(), newAccount);
		client.addAccountID(newAccount.getID()); // Assuming `ClientProfile` has this method
		accountLocks.putIfAbsent(newAccountId, new ReentrantLock());

		// Step 5: Confirm success
		handler.sendMessage(new SuccessMessage("New account created successfully."));
	}

	private void handleDeleteAccount(AccountMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();
		String username = msg.getUsername();
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
		lock.lock();

		try {
			ClientProfile profile = this.clientDatabase.get(username);
			// lock profile and remove acc from profile
			ReentrantLock profileLock = profileLocks.computeIfAbsent(username, key -> new ReentrantLock());
			try {
				profile.removeAccountID(accountID);
			} finally {
				profileLock.unlock();
			}
			// remove acc from database
			this.accountDatabase.remove(accountID);

			// remove lock from map
			accountLocks.remove(accountID);
			handler.sendMessage(new SuccessMessage("Account deleted successfully."));
		} finally {
			// unlock
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private void handleExitAccount(AccountMessage msg, ClientHandler handler) {
		String username = msg.getUsername(); // Client username
		String accountId = msg.getID(); // Account being exited

		if (accountId == null) {
			handler.sendMessage(new FailureMessage("Account ID missing in exit request."));
			return;
		}

		boolean released = releaseAccountLock(accountId);

		if (released) {
			handler.sendMessage(new SuccessMessage("Exited account successfully. Lock released."));
		} else {
			handler.sendMessage(new FailureMessage("Failed to release account lock (check server logs)."));
		}

	}

	private void handleShareAccount(ShareAccountMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();
		if (session == null) {
			handler.sendMessage(new FailureMessage("Not authenticated."));
			return;
		}
		String ownerUsername = msg.getOwnerProfile();
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
			handler.sendMessage(new FailureMessage("You are not authorized to edit this account."));
			return;
		}

		// 5) Verify the owner actually has the account in their profile
		if (ownerProfile.getAccountID(accountId) == null) {
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
		targetProfile.addAccountID(accountId);

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
		if (profile == null ){
			System.out.printf("[Server] LOGIN FAILED for client '%s'%n", username + "Due to profile == null");
			return;
		}
		if (!profile.getPassword().equals(password)) {
			handler.sendMessage(new FailureMessage("Invalid credentials."));
			System.out.printf("[Server] LOGIN FAILED for client '%s'%n", username + "Due to");
			return;
		}
		// check sessions
		for (SessionInfo existingSession : sessionIDs.values()) {
			if (username.equals(existingSession.getUsername())
					&& existingSession.getRole() == SessionInfo.ROLE.CLIENT) {
				handler.sendMessage(new FailureMessage("User '" + username + "' is already logged in elsewhere."));
				return;
			}
		}

		// Locking this specific profile for login session check
		profileLocks.putIfAbsent(username, new ReentrantLock());
		ReentrantLock lock = profileLocks.get(username);

		// check locks
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

		if (username == null || !password.equals(this.tellerDatabase.get(username))) {
			handler.sendMessage(new FailureMessage("Invalid credentials."));
			System.out.printf("[Server] LOGIN FAILED for Teller '%s'%n", username);
			return;
		}
		for (SessionInfo existingSession : sessionIDs.values()) {
			if (username.equals(existingSession.getUsername())
					&& existingSession.getRole() == SessionInfo.ROLE.TELLER) {
				handler.sendMessage(new FailureMessage("User '" + username + "' is already logged in elsewhere."));
				return;
			}
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
		SessionInfo session = msg.getSession();
		String username = session.getUsername();

		sessionIDs.remove(session.getSessionID());

		// unlock profile
		if (username != null) {
			releaseUserProfileLock(username);
			releaseAllUserAccountLocks(username);
		}

		handler.sendMessage(new SuccessMessage("Log Out Successful"));
		handler.setAuthenticated(false);
	}

	// Handles Logout for Tellers
	private void handleTellerLogout(LogoutMessage msg, ClientHandler handler) {
		SessionInfo session = msg.getSession();
		if (session != null) {
			sessionIDs.remove(session.getSessionID());
		}
		// remove lock on teller profile
		ReentrantLock lock = tellerLocks.get(session.getUsername());
		if (lock != null && lock.isLocked()) {
			try {
				while (lock.isHeldByCurrentThread() && lock.getHoldCount() > 0) {
					lock.unlock();
				}
				if (lock.isLocked()) {
					System.err.println("Teller lock for " + session.getUsername() + " still held after logout attempt");
				} else {
					System.out.println("Released teller lock for teller: " + session.getUsername());
				}
			} catch (IllegalMonitorStateException e) {
				System.err.println("Error releasing teller lock for " + session.getUsername() + e.getMessage());
			}
		}
		handler.setAuthenticated(false);
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

	private boolean releaseUserProfileLock(String username) {
		if (username == null || username.isBlank())
			return false;
		ReentrantLock profileLock = profileLocks.get(username);
		boolean released = false;
		if (profileLock != null && profileLock.isLocked()) {
			try {
				if (profileLock.isHeldByCurrentThread()) {
					profileLock.unlock();
					System.out.println("Released profile lock for user: " + username);
					return true;
				}
			} catch (IllegalMonitorStateException e) {
				System.err.println("Error releasing lock for " + username + e.getMessage());
			}
		}
		return released;
	}

	private void releaseAllUserAccountLocks(String username) {
		ClientProfile profile = clientDatabase.get(username);
		if (profile != null) {
			List<String> accountIDs = new ArrayList<>(profile.getAccountIDs());
			for (String accountID : accountIDs) {
				releaseAccountLock(accountID);
			}
		}
	}

	private boolean releaseAccountLock(String accountId) {
		if (accountId == null || accountId.isBlank())
			return false;
		ReentrantLock accountLock = accountLocks.get(accountId);
		boolean released = false;
		if (accountLock != null && accountLock.isLocked()) {
			try {
				if (accountLock.isHeldByCurrentThread()) {
					accountLock.unlock();
					System.out.println("Released account lock for: " + accountId);
					return true;
				}
			} catch (IllegalMonitorStateException e) {
				System.err.println("Error releasing account lock for " + accountId + e.getMessage());
			}
		}
		return released;
	}

	/** Shut down all clients, save databases, and clear server state */
	private void serverShutDown() {
		System.out.println("[Server] Shutting down, notifying clients...");

		// 1. Notify clients and shut them down
		for (ClientHandler handler : client_list) {
			try {
				handler.sendMessage(new ShutDownMessage());
			} catch (Exception e) {
				System.err.println("Failed to notify client: " + e.getMessage());
			} finally {
				handler.shutDown(); // Closes socket, stops thread
			}
		}

		// 2. save and serialize everything in the Database class
		// Database.getInstance().saveDatabase(saveFile);

		// 3. Clear client handler list
		client_list.clear();

		// 4. Clear session IDs
		sessionIDs.clear();

		// 5. Release all locks
		accountLocks.clear();
		profileLocks.clear();
		tellerLocks.clear();

		System.out.println("[Server] Shutdown complete. All clients disconnected.");
	}
}
