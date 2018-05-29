package server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerMain {
	private static final int defaultPort = 4444;

	public static void main(String[] args) {
		int port = defaultPort;

		runServer(port);
	}

	/**
	 * Map field is initialized as a empty map as no clients have established connection with
	 * the server yet.
	 */
	public static void runServer(int port) {
		Map<String, StringBuffer> map = new HashMap<String, StringBuffer>();
		Map<String, Integer> versions = new HashMap<String, Integer>();
		Server server = new Server(port, map, versions);
		server.serve();
	}
}
