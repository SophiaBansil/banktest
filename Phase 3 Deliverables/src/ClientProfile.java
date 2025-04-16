
public class ClientProfile {
	private String username;
	private String password;
	private String phone;
	private String address;
	private String legalName;
	private boolean isOpen;
	private Account[] accounts;
	
	public ClientProfile(String username, String password, String phone, String address, String legalName) {
		this.username = username;
		this.password = password;
		this.phone = phone;
		this.address = address;
		this.legalName = legalName;
	}
	public void addAccount(Account acc) {
		if (accounts[accounts.length - 1] != null) {
			this.accounts = increaseArray(accounts);
		}	
		for (int i = 0; i < accounts.length; i++) {
			if (accounts[i] == null) {
				accounts[i] = acc;
			}
			else if (acc.getID().compareTo(accounts[i].getID()) < 0) {
				for (int k = accounts.length - 1; k > i; k--) {
					accounts[k] = accounts[k-1]; 
				}
				accounts[i] = acc;
				return;
			}
		}
	}
	public void removeAccount(String id) {
		for (int i = 0; i < accounts.length; i++) {
			if (accounts[i] == null) {
				return;
			}
			else if (id.equals(accounts[i].getID())) {
				for (int k = i; k < accounts.length - 1; k++) {
					accounts[k] = accounts[k+1]; 
				}
				accounts[accounts.length - 1] = null;
				return;
			}
		}
	}
	public Account getAccount(String id) {
		for (int i = 0; i < accounts.length; i++) {
			if (accounts[i].getID().equals(id)) {
				return accounts[i];
			}
		}
		return null;
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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("CLIENTPROFILE|");
		sb.append(getUsername()).append("|");
		sb.append(getPassword()).append("|");
		sb.append(getPhone()).append("|");
		sb.append(getAddress()).append("|");
		sb.append(getLegalName());
		
		sb.append("~");
		sb.append(System.lineSeparator());
		
		for (Account acc : accounts) {
			sb.append(acc.toString());
		}
		return sb.toString();
	}
	
	private static Account[] increaseArray(Account[] arr) {
		Account[] newArr = new Account[arr.length + 7];
		//Copy every element in old array into the same index of new array
		for (int i = 0; i < arr.length; i++) {
			newArr[i] = arr[i];
		}
		return newArr;
	}
}
