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
        this.writer.write(line + Color.RESET);
        this.writer.flush();
    }

    public void sendMessages(String... lines) throws IOException {
        for(String line : lines) {
            this.writer.write(line + Color.RESET + "\n");
        }

        this.writer.flush();
    }

    private void displayUserInLine() throws IOException {
        this.sendMessage(Color.PURPLE + this.client.getUsername() + ": ");
    }

    private void sendLoginMessage() throws IOException {
        this.sendMessages(
                "Welcome to the 'Infected-Counter-0-Matic! Do you wish to:",
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

            this.sendMessage("Please enter your password: ");
            password1 = this.reader.readLine();

            this.sendMessage("Please confirm your password: ");
            password2 = this.reader.readLine();

            if(password1.equals(password2)) {
                this.sendMessages(Color.GREEN + "You are now registered and logged in!");

                this.client = new Client(username, password1);
                this.server.getData().putClient(username, this.client);
                this.server.saveData();
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

            this.sendMessage(firstTry ? "Please enter your desired username: " : Color.RED + "That username is already taken! Please try another one: ");
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
            this.sendMessage(firstTry ? "Please enter your username: " : Color.RED + "That username is not registered! Try again: ");

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

            this.sendMessage(firstTry ? "Please enter your password: " : Color.RED + "Your password is incorrect. Please try again: ");
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
            int inputInfectedCount = Integer.parseInt(line);
            if(this.client.updateInfectedContacts(inputInfectedCount)) {
                this.sendMessages("Your infected count has been updated!");
                this.server.propagateNewInfectedRatio();
            } else {
                this.sendMessages("You cannot specify a number of infected contacts greater than the number of contacts themselves!");
            }
        } catch(SocketException e) {
            if(!this.socket.isClosed()) {
                throw e;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch(NumberFormatException n) {
            this.sendMessages("Please enter a valid integer.");
        }
    }

    @Override
    public void run() {
        this.thread = Thread.currentThread();
        this.server.addClientThread(this);

        try {
            this.sendLoginMessage();
            this.readLoginOption();

            this.sendMessages("Please feel free to update your number of infected contacts anytime you'd like. Just input the new value, followed by ENTER.");

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
        this.socket.close();
    }
}
