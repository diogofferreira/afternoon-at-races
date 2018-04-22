package communication;

import messages.RacingTrackMessage;
import sharedRegionsInterfaces.RacingTrackInterface;

/**
 * This data type defines the server side thread that effectively provides the
 * service; the communication is based on changing messages with the client
 * via a TCP channel.
 */
public class RacingTrackAPS extends Thread {

    private ServerCom com;                  // socket de comunicação com o cliente
    private RacingTrackInterface racingTrackInt;    // serviço a ser fornecido

    /**
     *  Constructor to initiate variables.
     *
     *     @param com Communication channel.
     *     @param racingTrackInt Instance of RacingTrackInterface to provide the service.
     */
    public RacingTrackAPS (ServerCom com, RacingTrackInterface racingTrackInt) {
        if (com == null)
            throw new IllegalArgumentException("Invalid communication socket.");
        if (racingTrackInt == null)
            throw new IllegalArgumentException("Invalid Paddock interface.");

        this.com = com;
        this.racingTrackInt = racingTrackInt;
    }

    /**
     *  Provide the service.
     */
    @Override
    public void run() {
        RacingTrackMessage inMessage = (RacingTrackMessage)com.readObject();
        RacingTrackMessage outMessage = racingTrackInt.processAndReply(inMessage);
        com.writeObject(outMessage);
    }
}
