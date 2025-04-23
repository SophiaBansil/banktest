import java.util.UUID;

public class SessionInfo {
	public enum ROLE {
		CLIENT,
		TELLER
	}
	private String username;
	private String session_id;
	private ROLE role;
	long lastActive;
	
	public SessionInfo() {
		this.username = null;
		this.session_id = null;
		this.role = null;
        this.lastActive = System.currentTimeMillis();
	}
	
	public SessionInfo(String username, ROLE role) {
		this.username = username;
		this.session_id = UUID.randomUUID().toString();
		this.role = role;
        this.lastActive = System.currentTimeMillis();
	}
	public String getUsername() {
		return username;
	}
	public String getSessionID() {
		return session_id;
	}
	public ROLE getRole() {
		return role;
	}
	public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }
    public long getLastActive() {
        return lastActive;
    }
}
