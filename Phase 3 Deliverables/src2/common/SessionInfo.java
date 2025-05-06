package src2.common;
import java.io.Serializable;
import java.util.UUID;

public final class SessionInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public enum ROLE {
		CLIENT,
		TELLER
	}
	private final String username;
	private final String session_id;
	private final ROLE role;
	private long lastActive;
	
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
	public void setLastActive(long timeStamp) {
        this.lastActive = timeStamp;
    }
    public long getLastActive() {
        return lastActive;
    }
}
