public class ClientProfile {
	private String username;
	private String password;
	private String phone;
	private String address;
	private String legalName;
	private String[] account_ids;
	
	public ClientProfile(String username, String password, String phone, String address, String legalName) {
		this.username = username;
		this.password = password;
		this.phone = phone;
		this.address = address;
		this.legalName = legalName;
	}
	public void addAccountID(String id) {
		if (account_ids[account_ids.length - 1] != null) {
			this.account_ids = increaseArray(account_ids);
		}	
		for (int i = 0; i < account_ids.length; i++) {
			if (account_ids[i] == null) {
				account_ids[i] = id;
			}
			else if (id.compareTo(account_ids[i]) < 0) {
				for (int k = account_ids.length - 1; k > i; k--) {
					account_ids[k] = account_ids[k-1]; 
				}
				account_ids[i] = id;
				return;
			}
		}
	}
	public void removeAccountID(String id) {
		for (int i = 0; i < account_ids.length; i++) {
			if (account_ids[i] == null) {
				return;
			}
			else if (id.equals(account_ids[i])) {
				for (int k = i; k < account_ids.length - 1; k++) {
					account_ids[k] = account_ids[k+1]; 
				}
				account_ids[account_ids.length - 1] = null;
				return;
			}
		}
	}
	public String getAccountID(String id) {
		for (int i = 0; i < account_ids.length; i++) {
			if (account_ids[i].equals(id)) {
				return account_ids[i];
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
	public String[] getAccountIDs() {
		return account_ids;
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
	private static String[] increaseArray(String[] arr) {
		String[] newArr = new String[arr.length + 7];
		//Copy every element in old array into the same index of new array
		for (int i = 0; i < arr.length; i++) {
			newArr[i] = arr[i];
		}
		return newArr;
	}
}
