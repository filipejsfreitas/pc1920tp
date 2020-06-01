import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Server server = new Server();

        Runtime.getRuntime().addShutdownHook(new Thread(server::close));

        server.run();
    }
}
