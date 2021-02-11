package server.network;

import domain.user.User;
import network.NetworkPackage;
import network.NetworkPackageHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerNetwork implements Runnable {
    /**
     * Contains all connected clients, which includes also not authenticated clients.
     */
    private HashMap<Integer, ServerNetworkThread> connectedClients = new HashMap<>(50);

    /**
     * Contains all authenticated clients.
     * A user can use the app only on one device at the same time.
     */
    private Map<User, Integer> authenticatedClients = new HashMap<>(50);
    private Map<Integer, User> authenticatedClientPorts = new HashMap<>(50);

    private ServerSocket server;
    private Thread thread;

    private NetworkPackageHandler packageHandler;

    public ServerNetwork(int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port);
            System.out.println("ServerNetwork started: " + server);
            start();
        } catch (IOException ioe) {
            System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
        }
    }

    public void run() {
        while (thread != null && !Thread.currentThread().isInterrupted()) {
            try {
                System.out.println("Waiting for a client ...");
                createNetworkThreadForClient(server.accept());
            } catch (IOException ioe) {
                System.out.println("Server accept error: " + ioe);
                stop();
            }
        }
    }

    synchronized void handle(int socketPort, NetworkPackage networkPackage) {
        networkPackage.setSourceId(socketPort);

        if (!authenticatedClientPorts.containsKey(socketPort)) {
            if (networkPackage.getType() == NetworkPackage.Type.REGISTER
                    || networkPackage.getType() == NetworkPackage.Type.LOGIN) {
                // user is not authenticated thus only register and login actions are possible
                packageHandler.handlePackage(networkPackage);
            } else {
                System.out.println("Client " + socketPort + " is not authenticated and sent package of type " + networkPackage.getType());
            }
        } else {
            // client is authenticated, we handle all messages
            packageHandler.handlePackage(networkPackage);
        }
    }

    private void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    private void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    private void createNetworkThreadForClient(Socket socket) {
        System.out.println("Client accepted: " + socket);
        ServerNetworkThread chatServerThread = new ServerNetworkThread(this, socket);
        connectedClients.put(socket.getPort(), chatServerThread);

        try {
            chatServerThread.open();
            chatServerThread.start();

        } catch (IOException ioe) {
            System.out.println("Error opening thread: " + ioe);
        }
    }

    synchronized void remove(int socketPort) {
        ServerNetworkThread toTerminate = connectedClients.get(socketPort);
        System.out.println("Removing client thread " + socketPort);
        connectedClients.remove(socketPort);

        // remove authenticated clients
        User user = this.authenticatedClientPorts.get(socketPort);
        this.authenticatedClients.remove(user);
        this.authenticatedClientPorts.remove(socketPort);

        try {
            toTerminate.close();
        } catch (IOException ioe) {
            System.out.println("Error closing thread: " + ioe);
        }
        toTerminate.interrupt();
    }

    /**
     * Public interface to send data from server to given recipients.
     *
     * @param networkPackage
     * @param recipients
     */
    public void sendPackage(NetworkPackage networkPackage, User[] recipients) {
        for (User recipient : recipients) {
            if (this.authenticatedClients.containsKey(recipient)) {
                int port = this.authenticatedClients.get(recipient);
                this.connectedClients.get(port).send(networkPackage);
            }
        }
    }

    /**
     * Sends a package to the client which is connected with the given socket port.
     * This method is only used to communicate with clients who are not authenticated.
     *
     * @param networkPackage
     * @param socketPort
     */
    public void sendPackage(NetworkPackage networkPackage, int socketPort) {
        this.connectedClients.get(socketPort).send(networkPackage);
    }

    /**
     * Adds the given user and socket port to the authenticated client list.
     *
     * @param user
     * @param socketPort
     * @return true, if user is now authenticated
     */
    public boolean addUserToAuthenticatedClients(User user, int socketPort) {
        if (!authenticatedClients.containsKey(user)) {
            this.authenticatedClients.put(user, socketPort);
            this.authenticatedClientPorts.put(socketPort, user);
            return true;
        } else {
            System.out.println("User " + user.getUsername() + " is already logged in with another device.");
            return false;
        }
    }

    public void setPackageHandler(NetworkPackageHandler packageHandler) {
        this.packageHandler = packageHandler;
    }

    public static void main(String[] args) {
        new ServerNetwork(9001);
    }
}
