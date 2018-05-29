package client;

import gui.MainWindow;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client class receives message from the server and send the message to be
 * processed by ClientActionListener.
 */
public class Client {
    private String nameOfDocument;
    private String textOfDocument;
    private int versionOfDocument;
    private Socket socket;
    private int port;
    private String host;
    private PrintWriter out;
    private MainWindow mainWindow;

    public Client(int port, String host, MainWindow main) {
        this.port = port;
        this.host = host;
        mainWindow = main;
    }

    public void start() throws IOException {
        socket = new Socket(host, port);
        mainWindow.openUsernameDialog();
        new ClientActionListener(this, socket).run();
        out = new PrintWriter(socket.getOutputStream());
    }

    public void setMainWindow(MainWindow frame) {
        this.mainWindow = frame;
    }

    public void sendMessageToServer(String message) {
        try {
            out = new PrintWriter(socket.getOutputStream());
            out.write(message + "\n");
            out.flush();
        } catch (IOException e) {
            mainWindow.openErrorView(e.getMessage());
        }
    }

    public void setUsername(String name) {
        System.out.println("setting username");
        mainWindow.setUsername(name);
        mainWindow.switchToWelcomeView();
    }

    public String getDocumentName() {
        return nameOfDocument;
    }

    public String getText() {
        return textOfDocument;
    }

    public int getVersion() {
        return versionOfDocument;
    }

    public Socket getSocket() {
        return socket;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public void updateDocumentName(String name) {
        System.out.println("updating documentName");
        nameOfDocument = name;
    }

    public void updateText(String text) {
        textOfDocument = text;
    }

    public void updateVersion(int newVersion) {
        versionOfDocument = newVersion;
    }
}
