import java.io.Serializable;
import java.util.HashMap;

public class Data implements Serializable {
    // pairs <username, password>
    private final HashMap<String,String> users;

    public Data() {
        this.users = new HashMap<>();
    }

    public void putUser(String username, String password) {
        synchronized (this.users) {
            this.users.put(username, password);
        }
    }

    public boolean containsUser(String username) {
        synchronized (this.users) {
            return this.users.containsKey(username);
        }
    }

    public String getPassword(String username) {
        synchronized (this.users) {
            return this.users.get(username);
        }
    }

}


