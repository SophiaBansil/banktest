import java.io.IOException;
import java.net.Socket;

public class LoginApplication {
    private ConnectionHandler handler;

    public ConnectionHandler getHandler() {
        establishConnection();
        return handler;
    }

    public LoginResult login(String user, String pass, boolean isTeller)
            throws IOException, LoginException {
        establishConnection();
        handler.sendMessage(new LoginMessage(
            isTeller ? Message.TYPE.LOGIN_TELLER : Message.TYPE.LOGIN_CLIENT,
            user, pass
        ));
        Message resp = handler.getMessage();
        if (resp instanceof SuccessMessage sm) {
            handler.setCurrentSession(sm.getSession());
            return new LoginResult(sm.getSession());
        } else if (resp instanceof FailureMessage fm) {
            throw new LoginException(fm.getMessage());
        } else {
            throw new LoginException("Unexpected server response");
        }
    }

    private void establishConnection() {
        if (handler == null) {
            try {
                Socket socket = new Socket("localhost", 7777);
                handler = new ConnectionHandler(socket);
                new Thread(handler).start();
            } catch (IOException e) {
                throw new RuntimeException("Connection failed: " + e.getMessage(), e);
            }
        }
    }

    // Helper classes embedded
    public static class LoginResult {
        public final SessionInfo session;
        public LoginResult(SessionInfo session) { this.session = session; }
    }

    public static class LoginException extends Exception {
        private static final long serialVersionUID = 1L;

		public LoginException(String msg) { super(msg); }
    }
}


