import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {
    private final String dataFilename = "data.obj";
    private final ServerSocket serverSocket;
    private final Data data;
    private final List<ClientThread> clients;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public Server() throws IOException, ClassNotFoundException {
        this.serverSocket = new ServerSocket(1234);
        this.clients = new ArrayList<>();
        this.data = readData(this.dataFilename);
    }


    public Server(int serverSocketPort) throws IOException, ClassNotFoundException {
        this.serverSocket = new ServerSocket(serverSocketPort);
        this.clients = new ArrayList<>();
        this.data = readData(this.dataFilename);
    }


    public void addClientThread(ClientThread client) {
        lock.writeLock().lock();
        this.clients.add(client);
        lock.writeLock().unlock();
    }


    public void removeClientThread(ClientThread client) {
        lock.writeLock().lock();
        this.clients.remove(client);
        lock.writeLock().unlock();
    }


    public String getDataFilename() {
        return this.dataFilename;
    }


    public Data getData() {
        return this.data;
    }


    public Data readData(String filename) throws ClassNotFoundException, IOException {
        try {
            Data data;
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);

            data = (Data) in.readObject();

            in.close();
            file.close();

            return data;
        } catch(FileNotFoundException e) {
            System.out.println("Creating new instance of Data...\n");
        }

        return new Data();
    }


    public void saveData() throws IOException {
        FileOutputStream file = new FileOutputStream(this.dataFilename);
        ObjectOutputStream out = new ObjectOutputStream(file);

        synchronized(this.data) {
            out.writeObject(this.data);
        }

        out.close();
        file.close();
    }


    public String getTimeInfo() {
        LocalDateTime date = LocalDateTime.now();
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss SSS"));
    }


    public double calculateInfectedRatio() {
        return this.data.getClients()
                .stream()
                .mapToDouble(Client::getInfectedRatio)
                .average()
                .orElse(0.0);
    }


    public void propagateNewInfectedRatio() {
        double average = this.calculateInfectedRatio();

        this.lock.readLock().lock();
        String timeInfo = this.getTimeInfo();

        this.clients.stream()
                    .filter(ClientThread::isLoggedIn)
                    .forEach(c -> {
                        try {
                            c.sendMessages(Color.BLUE  +  "\r[" + timeInfo + "] " + "Updated infected ratio is " + average);
                            c.displayUserInLine();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
        });
        this.lock.readLock().unlock();
    }


    public void run() throws IOException {
        System.out.println("Server is now running!");

        Socket clientSocket;

        try {
            while((clientSocket = serverSocket.accept()) != null) {
                Thread t = new Thread(new ClientThread(clientSocket, this));
                t.start();
            }
        } catch(SocketException e) {
            if(!serverSocket.isClosed())
                throw e;
        }
    }


    public void close() {
        try {
            System.out.println("Server closing");

            this.serverSocket.close();

            this.lock.readLock().lock();
            for(ClientThread client : this.clients) {
                client.close();
            }
            this.lock.readLock().unlock();

            while(true) {
                this.lock.readLock().lock();
                if(this.clients.isEmpty())
                    break;
                this.lock.readLock().unlock();
            }

            this.saveData();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
