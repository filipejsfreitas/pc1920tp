import org.bson.Document;

import java.io.*;
import java.net.Socket;

public class ClientHandlerThread implements Runnable{
    private final Client client;
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final DatabaseBuddy database;

    public ClientHandlerThread(Socket socket) throws IOException {
        this.client = new Client();
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        this.database = new DatabaseBuddy();
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
            database.insertUser(username, password1);
            this.displayUserInLine();
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

        if (database.existsUser(username)) {
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

        if (!this.database.existsUser(username)) {
            this.authenticateUsername(false);
        }

        authenticatePassword(username, true);
    }

    public void authenticatePassword(String username, boolean firstTry) throws IOException {
        Document userInfo = this.database.getUser(username);
        final String password;

        this.writer.write(firstTry ? "Please enter your password: " : "\u001B[31mYour password is incorrect. Please try again: \u001B[0m");
        this.writer.flush();
        password = this.reader.readLine();

        if (userInfo.get("password").equals(password)) {
            this.writer.write("\u001B[32mYou are now logged in!\u001B[30m\n");
            this.writer.flush();
            this.client.setUsername(username);
            this.displayUserInLine();
        }
    }


    @Override
    public void run() {
        while (!this.socket.isClosed()) {
            try {
                this.loginMessage();
                this.readLoginOption();

                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
