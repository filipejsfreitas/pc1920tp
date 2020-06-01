import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Client implements Serializable {
    private static final int CONTACTS = 150;
    private int infectedContacts = 0;
    private final String username;
    private final String password;
    private final ReentrantReadWriteLock lock;

    public Client(String username, String password) {
        this.lock = new ReentrantReadWriteLock(true);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    // Returns true if infected count does not exceed total contacts
    public boolean updateInfectedContacts(int infected) {
        if(infected > CONTACTS || infected < 0) return false;
        else {
            lock.writeLock().lock();
            this.infectedContacts = infected;
            lock.writeLock().unlock();

            return true;
        }
    }

    public double getInfectedRatio() {
        lock.readLock().lock();
        double infected = (double) this.infectedContacts / CONTACTS;
        lock.readLock().unlock();

        return infected;
    }
}
