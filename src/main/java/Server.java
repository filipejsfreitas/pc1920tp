import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private final ServerSocket serverSocket;
    private ArrayList<ClientHandlerThread> clients;

    public Server() throws IOException {
        this.serverSocket = new ServerSocket(1234);
        this.clients = new ArrayList<>();
    }

    public Server(int serverSocketPort) throws IOException {
        this.serverSocket = new ServerSocket(serverSocketPort);
        this.clients = new ArrayList<>();
    }

    public void addClient(ClientHandlerThread client) {
        this.clients.add(client);
    }

    public void start() throws IOException {
        Socket clientSocket;

        while ((clientSocket = serverSocket.accept()) != null) {
            ClientHandlerThread newClientHandler = new ClientHandlerThread(clientSocket);
            new Thread(newClientHandler).start();
            this.addClient(newClientHandler);
        }
    }
}
