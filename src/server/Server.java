package server;

import handlers.Edit;
import handlers.EditManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Listens for the messages sent over the network between clients.
 * Updates its own states.
 * documentMap - maps document name to its text. All documents are stored in the server
 * serverSocket - socket of the server.
 * threadList - list of threads, each for a client connection
 * editManager - queue of edits
 */
public class Server {
    private final Map<String, StringBuffer> documentMap;
    private final Map<String, Integer> documentVersionMap;
    private ServerSocket serverSocket;
    private ArrayList<ClientConnectionThread> threadList;
    private ArrayList<String> usernameList;
    private final EditManager editManager;

    public Server(int port, Map<String, StringBuffer> documents,
                  Map<String, Integer> version) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server created. Port: " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        documentMap = Collections.synchronizedMap(documents);
        threadList = new ArrayList<>();
        documentVersionMap = Collections.synchronizedMap(version);
        usernameList = new ArrayList<>();
        editManager = new EditManager();
    }

    /**
     * Listening and handling client connections. Never
     * returns unless an exception is thrown (if the main server socket is broken)
     */
    public void serve() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                // handle the client by making a new ClientConnectionThread thread
                // running for that client,
                // also add that thread to the threadList so that the server
                // could send the message to the client
                ClientConnectionThread t = new ClientConnectionThread(socket, this);
                threadList.add(t);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized Map<String, StringBuffer> getDocumentMap() {
        return documentMap;

    }

    public synchronized boolean isUsernameAvailable(String name) {
        return !usernameList.contains(name);
    }

    public synchronized void addUsername(String name) {
        usernameList.add(name);
    }

    public synchronized Map<String, Integer> getDocumentVersionMap() {
        return documentVersionMap;
    }

    /**
     * "document1 document2 ..."
     */
    public synchronized String getAllDocuments() {
        StringBuilder docNames = new StringBuilder();
        for (String key : documentMap.keySet()) {
            docNames.append(" ").append(key);
        }
        return docNames.toString();
    }

    /**
     * Manage the edit made by a client
     * @param offset the position of the edit
     * @return a string that is transformed message with version and offset
     * corrected to match the current document on server
     */
    public synchronized String manageEdit(String documentName, int version,
                                          int offset) {
        return editManager.manageEdit(documentName, version, offset);
    }

    public synchronized boolean isDocumentMapEmpty() {
        return documentMap.isEmpty();
    }

    /**
     * Add the edit to the queue
     */
    public synchronized void logEdit(Edit edit) {
        editManager.logEdit(edit);
    }

    public synchronized void removeThread(ClientConnectionThread t) {
        usernameList.remove(t.getUsername());
        threadList.remove(t);
    }

    /**
     * Creates a new document and adds it to the documentMap and the
     * documentVersionMap with version 1.
     */
    public synchronized void addNewDocument(String documentName) {
        documentMap.put(documentName, new StringBuffer());
        documentVersionMap.put(documentName, 1);
        editManager.createNewlog(documentName);
    }

    /**
     * Updates the version of the specified documentName in the
     * documentVersionMap. If documentName is not yet a key in
     * documentVersionMap, a new key-value pair is added to the map.
     */
    public synchronized void updateVersion(String documentName, int version) {
        documentVersionMap.put(documentName, version);
    }

    public synchronized int getVersion(String documentName) {
        return documentVersionMap.get(documentName);
    }

    /**
     * Deletes text in the specified document from the specified offset to the
     * specified endPosition.
     * @param offset       the starting position of the text going to be deleted
     * @param endPosition  the end position of the text going to be deleted
     */
    public synchronized void delete(String documentName, int offset,
                                    int endPosition) {
        if (offset < 0 || endPosition < 1) {
            throw new RuntimeException("invalid args");
        }
        documentMap.get(documentName).delete(offset, endPosition);
    }

    /**
     * Inserts the text into the specified document at the specified offset
     */
    public synchronized void insert(String documentName, int offset, String text) {
        documentMap.get(documentName).insert(offset, text);
    }

    public synchronized String getDocumentText(String documentName) {
        String document;
        document = documentMap.get(documentName).toString();
        return document;
    }

    /**
     * Returns the length of the specified document
     */
    public synchronized int getDocumentLength(String documentName) {
        return documentMap.get(documentName).length();
    }

    /**
     * Sends a message from every other thread in the threadList except for the
     * thread that originally sent the message (no duplicate messages) and
     * threads that has already closed its on and in (i.e, client disconnects).
     * @param message the String that the server is going to sent to clients
     * @param thread  sending thread
     */
    public void returnMessageToEveryOtherClient(String message,
                                                ClientConnectionThread thread) {
        for (ClientConnectionThread t : threadList) {
            if (!thread.equals(t) && !t.getSocket().isClosed()) {
                // if the thread is still alive and it's not the one that sends
                // the request, send message
                PrintWriter out;
                if (t.getSocket().isConnected()) {
                    synchronized (t) {
                        try {
                            // for those threads, open a printWriter and write
                            // message to its socket.
                            out = new PrintWriter(t.getSocket()
                                    .getOutputStream(), true);
                            out.println(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
