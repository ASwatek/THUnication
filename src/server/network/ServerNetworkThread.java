package server.network;

import network.NetworkPackage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Handles communication from and to a specific client.
 */
public class ServerNetworkThread extends Thread {

    private ServerNetwork serverNetwork;
    private Socket socket;
    private final int SOCKET_PORT;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public ServerNetworkThread(ServerNetwork serverNetwork, Socket socket) {
        super();

        this.serverNetwork = serverNetwork;
        this.socket = socket;
        this.SOCKET_PORT = socket.getPort();
    }

    /**
     * Sends a message to this client.
     *
     * @param networkPackage
     */
    public void send(NetworkPackage networkPackage) {
        try {
            outputStream.writeObject(networkPackage);
            outputStream.flush();
        } catch (IOException ioe) {
            System.out.println(SOCKET_PORT + " ERROR sending: " + ioe.getMessage());
            serverNetwork.remove(SOCKET_PORT);
            interrupt();
        }
    }

    public void run() {
        System.out.println("Server Thread " + this.getSocketPort() + " running.");

        // Forwards incoming packages to `ServerNetwork`.
        while (!Thread.currentThread().isInterrupted()) {
            try {
                serverNetwork.handle(SOCKET_PORT, (NetworkPackage) inputStream.readObject());
            } catch (IOException | ClassNotFoundException ioe) {
                System.out.println(SOCKET_PORT + " ERROR reading: " + ioe.getMessage());
                serverNetwork.remove(SOCKET_PORT);
                interrupt();
            }
        }
    }

    public void open() throws IOException {
        outputStream = new ObjectOutputStream((socket.getOutputStream()));
        inputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void close() throws IOException {
        if (socket != null) socket.close();
        if (inputStream != null) inputStream.close();
        if (outputStream != null) outputStream.close();
    }

    public int getSocketPort() {
        return this.socket.getPort();
    }
}
