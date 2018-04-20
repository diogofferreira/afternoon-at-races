package communication;

import messages.StableMessage;
import sharedRegionsInterfaces.StableInterface;

/**
 * This data type defines the server side thread that effectively provides the
 * service; the communication is based on changing messages with the client
 * via a TCP channel.
 */
public class StableAPS extends Thread {

    private ServerCom com;                  // socket de comunicação com o cliente
    private StableInterface stableInt;    // serviço a ser fornecido

    /**
     *  Constructor to initiate variables.
     *
     *     @param com Communication channel.
     *     @param stableInt Instance of StableInterface to provide the service.
     */
    public StableAPS (ServerCom com, StableInterface stableInt) {
        if (com == null)
            throw new IllegalArgumentException("Invalid communication socket.");
        if (stableInt == null)
            throw new IllegalArgumentException("Invalid Stable interface.");

        this.com = com;
        this.stableInt = stableInt;
    }

    /**
     *  Provide the service.
     */
    @Override
    public void run() {
        StableMessage inMessage = (StableMessage)com.readObject();
        StableMessage outMessage = stableInt.processAndReply(inMessage);
        com.writeObject(outMessage);
    }
}
