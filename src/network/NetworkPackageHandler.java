package network;

/**
 * Every class which can handle network packages should implement this class.
 */
public interface NetworkPackageHandler {
    /**
     * Processes the network package.
     *
     * @param networkPackage network data
     */
    void handlePackage(NetworkPackage networkPackage);

    /**
     * Called when the network connection is lost. Can be used to clean up old session data.
     */
    void networkConnectionLost();
}
