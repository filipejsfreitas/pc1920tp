package terminal;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class Listener implements Runnable {
    private final Socket socket;
    private final Scanner reader;
    private final ArrayList<String> messages;


    public Listener(ClientTerminal terminal) {
        this.reader = terminal.getSocketReader();
        this.socket = terminal.getSocket();
        this.messages = terminal.getMessages();
    }


    @Override
    public void run() {
        while(this.socket.isConnected()) {
            String a = this.reader.next();
            this.messages.add(a);
        }
    }
}
