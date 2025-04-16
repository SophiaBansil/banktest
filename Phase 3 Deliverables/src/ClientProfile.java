
public class ClientProfile {
	private String username;
	private String password;
	private String phone;
	private String address;
	private String legalName;
	private boolean isOpen;
	private Account[] accounts;
	
	public ClientProfile() {
		
	}
	public void addAccount(Account acc) {
		
	}
	public void removeAccount(String id) {
		
	}
	public void getAccount(String id) {
		
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
	public boolean isOpen() {
		return isOpen;
	}
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public void setLegalName(String legalName) {
		this.legalName = legalName;
	}
	public void setStatus(boolean status) {
		setOpen(status);
	}
}
