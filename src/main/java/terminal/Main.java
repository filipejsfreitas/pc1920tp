package terminal;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        ClientTerminal terminal = new ClientTerminal();
        terminal.start();
    }
}
