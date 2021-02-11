package client.network;

import network.NetworkPackage;
import network.NetworkPackageHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientNetwork implements Runnable {
    private Socket socket;
    private Thread thread;
    private ObjectOutputStream streamOut;
    private ObjectInputStream inputStream;
    private NetworkPackageHandler packageHandler;
    private boolean isConnected = false;
    private boolean stopIsIndented = false;

    public ClientNetwork(String serverName, int serverPort) {
        System.out.println("Establishing connection to server. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            this.isConnected = true;
            start();
        } catch (UnknownHostException e) {
            System.out.println("Host unknown: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Unexpected exception: " + e.getMessage());
        }
    }

    public void run() {
        System.out.println("ClientNetwork runs");
        while (thread != null && !Thread.currentThread().isInterrupted()) {
            try {
                Object o = inputStream.readObject();
                this.handle((NetworkPackage) o);
            } catch (IOException | ClassNotFoundException ioe) {
                System.out.println("Listening error: " + ioe.getMessage());
                this.stop();
                if (!stopIsIndented) {
                    this.packageHandler.networkConnectionLost();
                }
            }
        }
    }

    void handle(NetworkPackage networkPackage) {
        try {
            packageHandler.handlePackage(networkPackage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() throws IOException {
        streamOut = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());

        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    private void stop() {
        stop(false);
    }

    public void stop(boolean stopIsIndented) {
        if (stopIsIndented) {
            this.stopIsIndented = true;
        }
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        try {
            if (streamOut != null) {
                streamOut.flush();
                streamOut.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null) socket.close();
        } catch (IOException ioe) {
            System.out.println("Error closing ...");
        }
    }

    /**
     * Public interface to send a package from client side to the server.
     */
    public void sendPackage(NetworkPackage networkPackage) {
        try {
            streamOut.writeObject(networkPackage);
            streamOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPackageHandler(NetworkPackageHandler packageHandler) {
        this.packageHandler = packageHandler;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public static void main(String[] args) {
        new ClientNetwork("localhost", 9001);
    }
}
