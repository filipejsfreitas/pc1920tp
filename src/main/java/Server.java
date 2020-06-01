import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private final String dataFilename = "data.obj";
    private final ServerSocket serverSocket;
    private final Data data;
    private final ArrayList<Thread> clients;

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

    public void addClientThread(Thread client) {
        this.clients.add(client);
    }

    public String getDataFilename() {return this.dataFilename;}

    public Data getData() {return this.data;}

    public Data readData(String filename) throws ClassNotFoundException, IOException {
        try {
            Data data;
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);

            data = (Data) in.readObject();

            in.close();
            file.close();

            return data;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Creating new instance of Data...\n");
        }
        return new Data();
    }

    public void saveData() throws IOException {
        FileOutputStream file = new FileOutputStream(this.dataFilename);
        ObjectOutputStream out = new ObjectOutputStream(file);

        synchronized (this.data) {
            out.writeObject(this.data);
        }

        out.close();
        file.close();
    }

//    public void propagateNewInfectedRatio() {
//        int totalInfected = this.data.
//    }


    public void start() throws IOException {
        Socket clientSocket;

        while ((clientSocket = serverSocket.accept()) != null) {
            ClientHandlerThread client = new ClientHandlerThread(clientSocket, this);
            Thread t = new Thread(client);
            this.addClientThread(t);
            t.start();
        }

        this.clients.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
