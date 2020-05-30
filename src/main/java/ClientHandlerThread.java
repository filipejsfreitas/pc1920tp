import java.io.*;
import java.net.Socket;

public class ClientHandlerThread implements Runnable{
    private final String dataFilename;
    private final Client client;
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final Data data;

    public ClientHandlerThread(Socket socket, Data data, String filename) throws IOException {
        this.dataFilename = filename;
        this.client = new Client();
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        this.data = data;
    }


    private void displayUserInLine() throws IOException {
        this.writer.write("\u001B[35m" + this.client.getUsername() + ": " + "\u001B[0m");
        this.writer.flush();
    }


    private void loginMessage() throws IOException {
        this.writer.write("Welcome to the 'Infected-Counter-O-Matic! Do you wish to:\n");
        this.writer.write("\u001B[33m1- Register\n");
        this.writer.write("\u001B[33m2- Authenticate \u001B[0m\n");
        this.writer.flush();
    }


    private void readLoginOption() throws IOException {
        try {
            int loginOption = Integer.parseInt(this.reader.readLine());
            switch (loginOption) {
                case 1:
                    this.register(true);
                    break;
                case 2:
                    this.authenticateUsername(true);
                    break;
            }

        }
        catch (NumberFormatException e) {
            this.writer.write("\u001B[31mInvalid input! Please enter either '1' to Register or '2' to Authenticate\u001B[0m\n");
            this.writer.flush();
            this.displayUserInLine();
            this.readLoginOption();
        }
    }


    public void saveData(String filename) throws IOException {
        FileOutputStream file = new FileOutputStream(this.dataFilename);
        ObjectOutputStream out = new ObjectOutputStream(file);

        synchronized (this.data) {
            out.writeObject(this.data);
        }

        out.close();
        file.close();
    }


    public void verifyPasswords(String username) throws IOException {
        final String password1, password2;

        this.writer.write("Please enter your password: ");
        this.writer.flush();
        password1 = this.reader.readLine();

        this.writer.write("Please confirm your password: ");
        this.writer.flush();
        password2 = this.reader.readLine();

        if (password1.equals(password2)) {
            this.writer.write("\u001B[32mYou are now registered and logged in!\u001B[0m\n");
            this.writer.flush();
            this.client.setUsername(username);
            data.putUser(username, password1);
            this.displayUserInLine();
            this.saveData("filename");
        }
        else {
            this.writer.write("\u001B[31mThe passwords don't match! Please try again.\u001B[0m\n");
            this.writer.flush();
            this.verifyPasswords(username);
        }
    }

    public void register(boolean firstTry) throws IOException {
        final String username;

        this.writer.write(firstTry ? "Please enter your desired username: " : "\u001B[31mThat username is already taken! Please try another one: \u001B[0m");
        this.writer.flush();
        username = this.reader.readLine();

        if (this.data.containsUser(username)) {
            register(false);
        }
        else {
            this.verifyPasswords(username);
        }
    }

    public void authenticateUsername(boolean firstTry) throws IOException {
        final String username;

        this.writer.write(firstTry ? "Please enter your username: " : "\u001B[31mThat username is not registered! Try again: \u001B[0m");
        this.writer.flush();

        username = this.reader.readLine();

        if (!this.data.containsUser(username)) {
            this.authenticateUsername(false);
        }

        authenticatePassword(username, true);
    }

    public void authenticatePassword(String username, boolean firstTry) throws IOException {
        final String password = this.data.getPassword(username);
        final String inputPassword;

        this.writer.write(firstTry ? "Please enter your password: " : "\u001B[31mYour password is incorrect. Please try again: \u001B[0m");
        this.writer.flush();
        inputPassword = this.reader.readLine();

        if (password.equals(inputPassword)) {
            this.writer.write("\u001B[32mYou are now logged in!\u001B[30m\n");
            this.writer.flush();
            this.client.setUsername(username);
            this.displayUserInLine();
        }
    }


    @Override
    public void run() {
        try {
            this.loginMessage();
            this.readLoginOption();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
