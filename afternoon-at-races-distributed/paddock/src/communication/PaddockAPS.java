package communication;

import messages.PaddockMessage;
import sharedRegionsInterfaces.PaddockInterface;

/**
 * This data type defines the server side thread that effectively provides the
 * service; the communication is based on changing messages with the client
 * via a TCP channel.
 */
public class PaddockAPS extends Thread {

    private ServerCom com;                  // socket de comunicação com o cliente
    private PaddockInterface paddockInt;    // serviço a ser fornecido

    /**
     *  Constructor to initiate variables.
     *
     *     @param com Communication channel.
     *     @param paddockInt Instance of PaddockInterface to provide the service.
     */
    public PaddockAPS (ServerCom com, PaddockInterface paddockInt) {
        if (com == null)
            throw new IllegalArgumentException("Invalid communication socket.");
        if (paddockInt == null)
            throw new IllegalArgumentException("Invalid Paddock interface.");

        this.com = com;
        this.paddockInt = paddockInt;
    }

    /**
     *  Provide the service.
     */
    @Override
    public void run() {
        PaddockMessage inMessage = (PaddockMessage)com.readObject();
        PaddockMessage outMessage = paddockInt.processAndReply(inMessage);
        com.writeObject(outMessage);
    }
}
