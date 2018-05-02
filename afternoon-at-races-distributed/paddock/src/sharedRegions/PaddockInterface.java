package sharedRegions;

import entities.HorseInt;
import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.PaddockMessageTypes;
import messages.PaddockMessage;

/**
 * Interface of the Paddock server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class PaddockInterface {

    /**
     * Counter that registers the number of Spectators that have already invoked
     * the PROCEED_TO_PADDOCK method. It is useful to close the server socket.
     */
    private int requests;

    /**
     * Instance of the Paddock shared region.
     */
    private Paddock paddock;

    /**
     * Creates a new instance of an interface of the Paddock shared region.
     * @param paddock Instance of the Paddock shared region.
     */
    public PaddockInterface(Paddock paddock) {
        if (paddock == null)
            throw new IllegalArgumentException("Invalid Paddock.");

        this.paddock = paddock;
    }

    /**
     * Method that processes a request coming from the APS, interacts with the
     * shared region and returns a the response to the method invoked in the
     * shared region.
     * @param inMessage The client's incoming message, which contains the
     *                  information and arguments necessary to invoke the
     *                  corresponding method in the shared region.
     * @return The server's outgoing message, with all the information that
     * the invoked method returned and other entity attributes updates.
     */
    public PaddockMessage processAndReply(PaddockMessage inMessage) {
        PaddockMessageTypes mType;

        if ((mType = PaddockMessageTypes.getType(inMessage.getMethod())) == null)
            return new PaddockMessage(PaddockMessageTypes.ERROR);

        switch (mType) {
            case GO_CHECK_HORSES:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS) {
                    inMessage.setErrorMessage("Invalid spectator ID");
                    inMessage.setMethod(PaddockMessageTypes.ERROR);
                    return inMessage;
                }

                ((SpectatorInt) Thread.currentThread()).setID(inMessage.getEntityId());

                paddock.goCheckHorses();
                requests++;
                return new PaddockMessage(PaddockMessageTypes.GO_CHECK_HORSES,
                        inMessage.getEntityId());

            case PROCEED_TO_PADDOCK:
                if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES) {
                    inMessage.setErrorMessage("Invalid race ID");
                    inMessage.setMethod(PaddockMessageTypes.ERROR);
                    return inMessage;
                }
                if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                    inMessage.setErrorMessage("Invalid horse Idx");
                    inMessage.setMethod(PaddockMessageTypes.ERROR);
                    return inMessage;
                }
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES) {
                    inMessage.setErrorMessage("Invalid horse ID");
                    inMessage.setMethod(PaddockMessageTypes.ERROR);
                    return inMessage;
                }

                ((HorseInt) Thread.currentThread()).setRaceID(inMessage.getRaceID());
                ((HorseInt) Thread.currentThread()).setRaceIdx(inMessage.getRaceIdx());
                ((HorseInt) Thread.currentThread()).setID(inMessage.getEntityId());

                paddock.proceedToPaddock();
                requests++;
                return new PaddockMessage(PaddockMessageTypes.PROCEED_TO_PADDOCK,
                        inMessage.getEntityId());

            default:
                return new PaddockMessage(PaddockMessageTypes.ERROR);
        }
    }

    /**
     * Method that returns the counter that registers the number of Spectators
     * that have already invoked the PROCEED_TO_PADDOCK method.
     * @return The counter that registers the number of Spectators
     * that have already invoked the PROCEED_TO_PADDOCK method.
     */
    public int getRequests() {
        return requests;
    }
}
