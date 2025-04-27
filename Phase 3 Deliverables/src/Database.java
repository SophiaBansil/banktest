import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

// singleton
public class Database implements Serializable {
    private static volatile Database instance;

    // username -> password
    private final Map<String, String> tellerDatabase = new HashMap<>();
    // username -> clientProfile objects
    private final Map<String, ClientProfile> clientDatabase = new HashMap<>();
    // id -> account objects
    private final Map<String, Account> accountDatabase = new HashMap<>();


    // thread-safe variant of ArrayList in Java
    // best choice if read operation is most frequently used
    private final List<ClientHandler> client_list = new CopyOnWriteArrayList<>();

    // private constructor
    private Database() {
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public void saveDatabase(String filename) {
        try (ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(filename))) {
            s.writeObject(this); // save entire database class to .ser file
        } catch (IOException e) {
            System.err.println("Failed to save to database. Reason: " + e.getMessage());
        }
    }

    public static Database loadDatabase(String filename) {
        try (ObjectInputStream s = new ObjectInputStream(new FileInputStream(filename))) {
            Database d = (Database) s.readObject();
            instance = d;
            return instance;
        } catch (IOException | ClassNotFoundException c) {
            System.err.println("Failed to load in database. Reason: " + c.getMessage());
            return null;
        }
    }

    // protect singleton-ness of singleton upon server startup
    public Object readResolve() {
        return getInstance();
    }

    public Map<String, String> getTellerDatabase() {
        return tellerDatabase;
    }

    public Map<String, ClientProfile> getClientDatabase() {
        return clientDatabase;
    }

    public Map<String, Account> getAccountDatabase() {
        return accountDatabase;
    }

    public List<ClientHandler> getClientList() {
        return client_list;
    }

}
