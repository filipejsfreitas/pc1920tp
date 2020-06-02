package terminal;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

public class ClientTerminal {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 1234;
    private final ArrayList<String> messages;
    private final Socket socket;
    private final Scanner socketReader;
    private final BufferedWriter socketWriter;
    private final Scanner userReader;

    public ClientTerminal() throws IOException {
        this.messages = new ArrayList<>();
        this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        this.socketReader = new Scanner(this.socket.getInputStream()).useDelimiter("\0");
        this.socketWriter = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        this.userReader = new Scanner(System.in);
    }

    public Socket getSocket() {
        return this.socket;
    }

    public Scanner getSocketReader() {
        return this.socketReader;
    }

    public ArrayList<String> getMessages() {
        return this.messages;
    }

    public void start() throws IOException, InterruptedException {
        Thread listener = new Thread(new Listener(this));
        listener.start();

        while (this.socket.isConnected()) {
            while(!this.messages.isEmpty()) {
                String message = this.messages.remove(0);
                System.out.print(message);
            }

            if (System.in.available() > 0) {
                String textToSend = userReader.nextLine() + "\n";
                this.socketWriter.write(textToSend);
                this.socketWriter.flush();
            }
        }

        listener.join();
        this.socket.shutdownOutput();
        this.socket.shutdownInput();
        this.socket.close();
    }
}

