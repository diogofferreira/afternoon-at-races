package communication;

import messages.GeneralRepositoryMessage;
import sharedRegionsInterfaces.GeneralRepositoryInterface;

/**
 * This data type defines the server side thread that effectively provides the
 * service; the communication is based on changing messages with the client
 * via a TCP channel.
 */
public class GeneralRepositoryAPS extends Thread {

    private ServerCom com;                  // socket de comunicação com o cliente
    private GeneralRepositoryInterface grInt;    // serviço a ser fornecido

    /**
     *  Constructor to initiate variables.
     *
     *     @param com Communication channel.
     *     @param grInt Instance of GeneralRepositoryInterface to provide the service.
     */
    public GeneralRepositoryAPS (ServerCom com, GeneralRepositoryInterface grInt) {
        if (com == null)
            throw new IllegalArgumentException("Invalid communication socket.");
        if (grInt == null)
            throw new IllegalArgumentException("Invalid General Repository interface.");

        this.com = com;
        this.grInt = grInt;
    }

    /**
     *  Provide the service.
     */
    @Override
    public void run() {
        GeneralRepositoryMessage inMessage = (GeneralRepositoryMessage)com.readObject();
        GeneralRepositoryMessage outMessage = grInt.processAndReply(inMessage);
        com.writeObject(outMessage);
    }
}
