import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final String dataFilename = "data.obj";
    private final ServerSocket serverSocket;
    private final Data data;
    private final List<ClientThread> clients;

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
        synchronized(clients) {
            this.clients.add(client);
        }
    }

    public void removeClientThread(ClientThread client) {
        synchronized(clients) {
            this.clients.remove(client);
        }
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

    public void propagateNewInfectedRatio() {
        double average = this.data.getClients()
                .stream()
                .mapToDouble(Client::getInfectedRatio)
                .average()
                .orElse(0.0);

        synchronized(this.clients) {
            this.clients.forEach(c -> {
                try {
                    c.sendMessages("", "Updated count: " + average);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            });
        }
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

            synchronized(this.clients) {
                for(ClientThread client : this.clients) {
                    client.close();
                }
            }

            while(true) {
                synchronized(this.clients) {
                    if(this.clients.isEmpty())
                        break;
                }
            }

            this.saveData();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
