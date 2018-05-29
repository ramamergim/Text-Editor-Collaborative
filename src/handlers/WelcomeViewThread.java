package handlers;

import client.Client;

/**
 * The WelcomeViewThread makes a new Thread object that sends the input
 * from the WelcomeView to the server.
 */
public class WelcomeViewThread extends Thread {

    private final String message;
    private final Client client;

    /**
     * @param client  the client that is taking the action
     * @param message the message that client wants to send to the server
     */
    public WelcomeViewThread(Client client, String message) {
        this.message = message;
        this.client = client;
    }

    /**
     * Sends the message from the WelcomeView to the server.
     */
    public void run() {
        client.sendMessageToServer(message);
    }
}
