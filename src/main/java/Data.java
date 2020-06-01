import java.io.Serializable;
import java.util.stream.*;
import java.util.HashMap;

public class Data implements Serializable {
    // pairs <username, user data>
    private final HashMap<String,Client> users;

    public Data() {
        this.users = new HashMap<>();
    }

    public void putUser(String username, Client userData) {
        synchronized (this.users) {
            this.users.put(username, userData);
        }
    }

    public Client getUser(String username) {
        synchronized (this.users) {
            return this.users.get(username);
        }
    }

    public boolean containsUser(String username) {
        synchronized (this.users) {
            return this.users.containsKey(username);
        }
    }
}


