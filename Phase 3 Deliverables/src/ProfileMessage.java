public final class ProfileMessage extends Message {
	private static final long serialVersionUID = 1L;
	
	private final String username;
	private final String password;
	private final String phone;
	private final String address;
	private final String legalName;
	private final String[] account_ids;
	
	public ProfileMessage(TYPE type, SessionInfo session) {
		super(type, session);
		this.username = "";
		this.password = "";
		this.phone = "";
		this.address = "";
		this.legalName = "";
		this.account_ids = null;
    }
	
    public ProfileMessage(TYPE type, 
    		SessionInfo session, 
    		String username, 
    		String password, 
    		String phone, 
    		String address, 
    		String legalName, 
    		String[] account_ids) {
        super(type, null); // no session yet for login
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.legalName = legalName;
        this.account_ids = account_ids;
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
	public String[] getAccount_ids() {
		return account_ids;
	}
}
