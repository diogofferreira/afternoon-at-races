package communication;

import messageTypes.ControlCentreMessageTypes;
import messages.ControlCentreMessage;
import sharedRegions.ControlCentre;
import sharedRegionsInterfaces.ControlCentreInterface;

/**
 * This data type defines the server side thread that effectively provides the
 * service; the communication is based on changing messages with the client
 * via a TCP channel.
 */
public class ControlCentreAPS extends Thread {

    private ServerCom com;                  // socket de comunicação com o cliente
    private ControlCentreInterface ccInt;    // serviço a ser fornecido

    /**
     *  Constructor to initiate variables.
     *
     *     @param com Communication channel.
     *     @param controlCentre Instance of ControlCentre to provide the service.
     */
    public ControlCentreAPS (ServerCom com, ControlCentreInterface ccInt) {
        if (com == null)
            throw new IllegalArgumentException("Invalid communication socket.");
        if (ccInt == null)
            throw new IllegalArgumentException("Invalid Control Centre interface.");

        this.com = com;
        this.ccInt = ccInt;
    }

    /**
     *  Provide the service.
     */
    @Override
    public void run() {
        ControlCentreMessage inMessage = (ControlCentreMessage)com.readObject();

        ControlCentreMessage outMessage = null;



        com.writeObject(outMessage);
    }
}
