public final class ProfileMessage extends Message {
	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;
	private String phone;
	private String address;
	private String legalName;
	private String[] account_ids;
	
    public ProfileMessage(TYPE type, SessionInfo session, String username, String password, String phone, String address, String legalName) {
        super(type, null); // no session yet for login
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.legalName = legalName;
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
