package communication;

import messages.BettingCentreMessage;
import sharedRegionsInterfaces.BettingCentreInterface;

/**
 * This data type defines the server side thread that effectively provides the
 * service; the communication is based on changing messages with the client
 * via a TCP channel.
 */
public class BettingCentreAPS extends Thread {

    private ServerCom com;                  // socket de comunicação com o cliente
    private BettingCentreInterface bcInt;    // serviço a ser fornecido

    /**
     *  Constructor to initiate variables.
     *
     *     @param com Communication channel.
     *     @param bcInt Instance of BettingCentreInterface to provide the service.
     */
    public BettingCentreAPS (ServerCom com, BettingCentreInterface bcInt) {
        if (com == null)
            throw new IllegalArgumentException("Invalid communication socket.");
        if (bcInt == null)
            throw new IllegalArgumentException("Invalid Betting Centre interface.");

        this.com = com;
        this.bcInt = bcInt;
    }

    /**
     *  Provide the service.
     */
    @Override
    public void run() {
        BettingCentreMessage inMessage = (BettingCentreMessage)com.readObject();
        BettingCentreMessage outMessage = bcInt.processAndReply(inMessage);
        com.writeObject(outMessage);
    }
}
