package communication;

import messageTypes.ControlCentreMessageTypes;
import messages.ControlCentreMessage;
import sharedRegions.ControlCentre;

/**
 * This data type defines the server side thread that effectively provides the
 * service; the communication is based on changing messages with the client
 * via a TCP channel.
 */
public class ControlCentreAPS extends Thread {

    private ServerCom com;                  // socket de comunicação com o cliente
    private ControlCentre controlCentre;    // serviço a ser fornecido

    /**
     *  Constructor to initiate variables.
     *
     *     @param com Communication channel.
     *     @param controlCentre Instance of ControlCentre to provide the service.
     */
    public ControlCentreAPS (ServerCom com, ControlCentre controlCentre) {
        this.com = com;
        this.controlCentre = controlCentre;
    }

    /**
     *  Provide the service.
     */
    @Override
    public void run () {
        ControlCentreMessage inMessage = (ControlCentreMessage)com.readObject();
        ControlCentreMessage outMessage = null;

        switch (ControlCentreMessageTypes.getType(inMessage.getMethod())) {
            case SUMMON_HORSES_TO_PADDOCK:
                controlCentre.summonHorsesToPaddock(inMessage.getRaceId());
                outMessage = new ControlCentreMessage(
                        ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK);
                break;
            default:
                System.out.println("Error"); break;
        }

        com.writeObject(outMessage);
    }
}
