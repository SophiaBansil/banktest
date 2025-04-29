import java.io.*;
import java.net.Socket;
import java.math.BigDecimal;

public class ATMApplication {

    private ConnectionHandler handler;

    public void withdraw(BigDecimal num) {
        // create TransactionMessage
        // send to server via connectionhandler

        // make sure it's successful or not
        // return new balance via sending message
    }

    public void deposit(float num) {
        // create TransactionMessage
        // send to server via connectionhandler

        // make sure it's successful or not
        // return new balance via sending message
    }

    public Account loadAccount(Account acc) {}

    public void establishConnection() {}

    // dont think we need this
    // public void saveChanges() {}

    public void exit() {
        // send logout message
    }
}
