import java.net.Socket;

public class LoginApplication {
    private ConnectionHandler handler;
    private SessionListener sessionListener;

    public void setSessionListener(SessionListener listener) {
        this.sessionListener = listener;
    }

    public void login(String user, String pass, boolean isTeller) {
        establishConnection();
        Message loginMsg = new LoginMessage(
            isTeller ? Message.TYPE.LOGIN_TELLER : Message.TYPE.LOGIN_CLIENT,
            user, pass
        );
        handler.sendMessage(loginMsg);

        try {
            Message response = handler.getMessage();
            if (response instanceof SuccessMessage) {
                SessionInfo session = ((SuccessMessage) response).getSession();

                if (sessionListener != null) {
                    sessionListener.onLoginSuccess(session, handler);
                }
            } else if (response instanceof FailureMessage) {
                if (sessionListener != null) {
                    sessionListener.onLoginFailure(((FailureMessage) response).getMessage());
                }
            } else {
                if (sessionListener != null) {
                    sessionListener.onLoginFailure("Unexpected server response.");
                }
            }
        } catch (Exception e) {
            if (sessionListener != null) {
                sessionListener.onLoginFailure("Login failed: " + e.getMessage());
            }
        }
    }

    private void establishConnection() {
        if (handler == null) {
            try {
                Socket socket = new Socket("localhost", 7777);
                handler = new ConnectionHandler(socket);
                new Thread(handler).start();
            } catch (Exception e) {
                System.err.println("Connection failed: " + e.getMessage());
            }
        }
    }
}  

