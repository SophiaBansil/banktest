package bank;

import java.net.Socket;

public class LoginApplication {
    private ConnectionHandler handler;

    public void TellerLogin(String user, String pass) {
        establishConnection();

        Message loginMsg = new Message(user, pass, TYPE.LOGIN);
        handler.send(loginMsg);
    }

    public void ClientLogin(String user, String pass) {
        establishConnection();

        Message loginMsg = new Message(user, pass, TYPE.LOGIN);
        handler.send(loginMsg);
    }

    private void establishConnection() {
        if (handler == null) {
            try {
                Socket socket = new Socket("localhost", 1234);
                handler = new ConnectionHandler(socket);
                new Thread(handler).start();
            } catch (Exception e) {
                System.err.println("Failed to connect: " + e.getMessage());
            }
        }
    }
}
