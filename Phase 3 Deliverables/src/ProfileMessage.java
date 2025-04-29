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
		this.accounts = new ArrayList<>();
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
        super(type, null); // no session yet for login
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.legalName = legalName;
        this.accounts   = List.copyOf(accounts);
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
