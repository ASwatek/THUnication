package server;

import server.logic.ServerHandler;
import server.network.ServerNetwork;

/**
 * Bootstraps the server side logic.
 */
public class ServerApp {

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        ServerNetwork serverNetwork = new ServerNetwork(port);
        ServerHandler handler = new ServerHandler(serverNetwork);
        serverNetwork.setPackageHandler(handler);
    }
}
