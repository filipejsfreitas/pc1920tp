import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class Data implements Serializable {
    // pairs <username, user data>
    private final HashMap<String, Client> clients;

    public Data() {
        this.clients = new HashMap<>();
    }

    public void putClient(String username, Client clientData) {
        synchronized(this.clients) {
            this.clients.put(username, clientData);
        }
    }

    public Client getClient(String username) {
        synchronized(this.clients) {
            return this.clients.get(username);
        }
    }

    public boolean containsClient(String username) {
        synchronized(this.clients) {
            return this.clients.containsKey(username);
        }
    }

    public Collection<Client> getClients() {
        synchronized(this.clients) {
            return this.clients.values();
        }
    }
}
