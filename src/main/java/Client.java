import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Client implements Serializable{
    private static final int CONTACTS = 150;
    private int infectedContacts = 0;
    private String username;
    private String password;
    private ReentrantReadWriteLock lock;


    public Client(String username, String password) {
        this.lock = new ReentrantReadWriteLock(true);
        this.username = username;
        this.password = password;
    }


    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {this.password = password;}

    public String getPassword() {return this.password;}

    // Returns true if infected count does not exceed total contacts
    public boolean updateInfectedContacts(int infected) {
        if (this.infectedContacts > CONTACTS) return false;
        else {
            this.infectedContacts = infected;
            return true;
        }
    }

}