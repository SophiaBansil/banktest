package src2.common;
import java.io.Serializable;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class ClientProfile implements Serializable{
	private String username;
	private String password;
	private String phone;
	private String address;
	private String legalName;
	private List<String> accountIds;
	
	public ClientProfile(String username, String password, String phone, String address, String legalName) {
		this.username = username;
		this.password = password;
		this.phone = phone;
		this.address = address;
		this.legalName = legalName;
		// change to list?
		this.accountIds = new ArrayList<>();
	}
	public void addAccountID(String id) {
        if (id != null && !id.trim().isEmpty() && !this.accountIds.contains(id)) {
            this.accountIds.add(id.trim());
            Collections.sort(this.accountIds);
        }
    }
	public void removeAccountID(String id) {
        if (id != null) {
            this.accountIds.remove(id);
			Collections.sort(this.accountIds);
        }
    }
	public String getAccountID(String id) {
        if (id != null && this.accountIds.contains(id)) {
            return id;
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
	public List<String> getAccountIDs() {
		return accountIds;
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
}
