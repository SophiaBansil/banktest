import java.util.ArrayList;
import java.util.List;

public final class ProfileMessage extends Message {
	private static final long serialVersionUID = 1L;
	
	private final String username;
	private final String password;
	private final String phone;
	private final String address;
	private final String legalName;
	private final List<AccountSummary> accounts;
	
	// Constructor for requesting Profile Information
	public ProfileMessage(TYPE type, SessionInfo session) {
		super(type, session);
		this.username = "";
		this.password = "";
		this.phone = "";
		this.address = "";
		this.legalName = "";
		this.accounts = null;
    }
	
	// Constructor for sending Profile Information
    public ProfileMessage(TYPE type, 
    		SessionInfo session, 
    		String username, 
    		String password, 
    		String phone, 
    		String address, 
    		String legalName, 
    		List<AccountSummary> accounts) {
        super(type, session);
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.legalName = legalName;
        this.accounts = List.copyOf(accounts);
    }

	// Constructor for creating new Profile STEP 1: valid username
	public ProfileMessage(SessionInfo session, String usernameToCheck) {
        super(TYPE.CHECK_USERNAME_AVAILABILITY, session);
        this.username = usernameToCheck.trim(); 
        this.password = null;
        this.phone = null;
        this.address = null;
        this.legalName = null;
        this.accounts = null;
    }

	// Constructor for creating new Profile STEP 2: send over new info
	public ProfileMessage(TYPE type, 
			SessionInfo session, 
			String username, 
			String password, 
			String phone, 
			String address, 
			String legalName) {
		super(TYPE.CREATE_PROFILE, session);
		this.username = username.trim();
		this.password = password;
		this.phone = phone;
		this.address = address;
		this.legalName = legalName;
		this.accounts = null;
	}

    public String getUsername() { 
    	return username; 
    }
    public String getPassword() { 
    	return password; 
    }
	public String getPhone() {
		return phone;
	}
	public String getAddress() {
		return address;
	}
	public String getLegalName() {
		return legalName;
	}
	public List<AccountSummary> getSummaries() {
	    return accounts;
	}
}
