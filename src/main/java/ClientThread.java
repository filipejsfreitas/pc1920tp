import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientThread implements Runnable {
    private Thread thread;

    private final Server server;
    private Client client = null;
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public ClientThread(Socket socket, Server server) throws IOException {
        this.server = server;
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    }

    public void sendMessage(String line) throws IOException {
        try {
            this.writer.write(line + Color.RESET + "\0");
            this.writer.flush();

        }
        catch (SocketException s) {
            if (!this.socket.isClosed() || this.socket.isConnected()) {
                throw s;
            }
        }
    }

    public boolean isLoggedIn() {
        return this.client != null;
    }

    public void sendMessages(String... lines) throws IOException {
        try {
            for(String line : lines) {
                this.writer.write(line + Color.RESET + "\n\0");
            }

            this.writer.flush();
        }
        catch (SocketException s) {
            if (!this.socket.isClosed() || this.socket.isConnected()) {
                throw s;
            }
        }
    }

    public void displayUserInLine() throws IOException {
        this.sendMessage("\r" + Color.PURPLE + this.client.getUsername() + ": ");
    }

    private void sendLoginMessage() throws IOException {
        this.sendMessages(
                Color.YELLOW + "Welcome to the 'Infected-Counter-0-Matic! Do you wish to:",
                Color.YELLOW + "1 - Register",
                Color.YELLOW + "2 - Authenticate"
        );
    }

    private void readLoginOption() throws IOException {
        outer:
        while(true) {
            try {
                int loginOption = Integer.parseInt(this.reader.readLine());

                switch(loginOption) {
                    case 1:
                        this.register();
                        break outer;
                    case 2:
                        this.authenticateUsername();
                        break outer;
                    default:
                        break;
                }
            } catch(NumberFormatException e) {
                this.sendMessages(Color.RED + "Invalid input! Please enter either '1' to Register or '2' to Authenticate.");
            }
        }
    }

    public void verifyPasswords(String username) throws IOException {
        while(true) {
            final String password1, password2;

            this.sendMessage(Color.YELLOW + "Please enter your password: ");
            password1 = this.reader.readLine();

            this.sendMessage(Color.YELLOW + "Please confirm your password: ");
            password2 = this.reader.readLine();

            if(password1.equals(password2)) {
                this.sendMessages(Color.GREEN + "You are now registered and logged in!");

                this.client = new Client(username, password1);
                this.server.getData().putClient(username, this.client);
                break;
            } else {
                this.sendMessages(Color.RED + "The passwords don't match! Please try again.");
            }
        }
    }

    public void register() throws IOException {
        boolean firstTry = true;

        while(true) {
            final String username;

            this.sendMessage(firstTry ? Color.YELLOW + "Please enter your desired username: " : Color.RED + "That username is already taken! Please try another one: ");
            username = this.reader.readLine();

            if(this.server.getData().containsClient(username)) {
                firstTry = false;
            } else {
                this.verifyPasswords(username);
                break;
            }
        }
    }

    public void authenticateUsername() throws IOException {
        boolean firstTry = true;
        String username;

        while(true) {
            this.sendMessage(firstTry ? Color.YELLOW + "Please enter your username: " : Color.RED + "That username is not registered! Try again: ");

            username = this.reader.readLine();

            if(!this.server.getData().containsClient(username)) {
                firstTry = false;
            } else break;
        }

        authenticatePassword(username);
    }

    public void authenticatePassword(String username) throws IOException {
        boolean firstTry = true;

        while(true) {
            String userPassword = this.server.getData().getClient(username).getPassword();

            this.sendMessage(firstTry ? Color.YELLOW + "Please enter your password: " : Color.RED + "Your password is incorrect. Please try again: ");
            String inputPassword = this.reader.readLine();

            if(userPassword.equals(inputPassword)) {
                this.sendMessages(Color.GREEN + "You are now logged in!");

                this.client = this.server.getData().getClient(username);
                break;
            } else {
                firstTry = false;
            }
        }
    }

    public void updateInfectedCount() throws NumberFormatException, IOException {
        try {
            String line = this.reader.readLine();
            if(line == null) {
                this.close();
                return;
            }

            int inputInfectedCount = Integer.parseInt(line);
            if(this.client.updateInfectedContacts(inputInfectedCount)) {
                this.sendMessages(Color.GREEN + "Your infected count has been updated!");
                this.server.propagateNewInfectedRatio();
            } else {
                this.sendMessages(Color.RED + "You must enter a number between 0 and 150!");
            }
        } catch(SocketException e) {
            if(!this.socket.isClosed()) {
                throw e;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch(NumberFormatException n) {
            this.sendMessages(Color.RED + "Please enter a valid integer.");
        }
    }

    @Override
    public void run() {
        this.thread = Thread.currentThread();
        this.server.addClientThread(this);

        try {
            this.sendLoginMessage();
            this.readLoginOption();

            this.sendMessages( Color.BLUE + "Please feel free to update your number of infected contacts anytime you'd like. Just input the new value, followed by ENTER.",
                    Color.BLUE + "[" + server.getTimeInfo() + "] The current infected ratio is " + this.server.calculateInfectedRatio());

            while(!socket.isClosed()) {
                this.displayUserInLine();
                this.updateInfectedCount();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        this.server.removeClientThread(this);
    }

    public void close() throws IOException {
        this.sendMessage("\n"); // Send empty newline
        this.socket.shutdownOutput();
        this.socket.shutdownInput();
        this.socket.close();
    }
}
