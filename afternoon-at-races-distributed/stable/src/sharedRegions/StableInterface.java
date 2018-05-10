package sharedRegions;

import entities.HorseInt;
import main.EventVariables;
import messageTypes.StableMessageTypes;
import messages.StableMessage;
import sharedRegions.Stable;

/**
 * Interface of the Stable server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class StableInterface {

    /**
     * Counter that registers the number of Horse/Jockey pairs that have already
     * invoked the PROCEED_TO_STABLE method (the total number will be twice the
     * number of pairs) and if the Broker has already invoked the CELEBRATE method.
     * It is useful to close the server socket.
     */
    private int requests;

    /**
     * Instance of the Stable shared region.
     */
    private Stable stable;

    /**
     * Creates a new instance of an interface of the Stable shared region.
     * @param stable Instance of the Stable shared region.
     */
    public StableInterface(Stable stable) {
        if (stable == null)
            throw new IllegalArgumentException("Invalid Stable.");

        this.stable = stable;
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
    public StableMessage processAndReply(StableMessage inMessage) {
        StableMessageTypes mType;
        int raceID;
        double[] odds;
        HorseInt h;

        if ((mType = StableMessageTypes.getType(inMessage.getMethod())) == null)
            return new StableMessage(
                    inMessage, "Invalid message type");

        switch (mType) {
            case GET_RACE_ODDS:
                raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new StableMessage(
                            inMessage, "Invalid race ID");

                odds = stable.getRaceOdds(raceID);
                return new StableMessage(StableMessageTypes.GET_RACE_ODDS,
                        odds, inMessage.getEntityId());

            case SUMMON_HORSES_TO_PADDOCK:
                raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new StableMessage(
                            inMessage, "Invalid race ID");

                stable.summonHorsesToPaddock(raceID);
                return new StableMessage(
                        StableMessageTypes.SUMMON_HORSES_TO_PADDOCK,
                        inMessage.getEntityId());

            case PROCEED_TO_STABLE:
                h = (HorseInt) Thread.currentThread();

                if (inMessage.getAgility() < 0 ||
                        inMessage.getAgility() > EventVariables.HORSE_MAX_STEP)
                    return new StableMessage(
                            inMessage, "Invalid horse agility");
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    return new StableMessage(
                            inMessage, "Invalid horse ID");

                h.setID(inMessage.getEntityId());
                h.setAgility(inMessage.getAgility());
                stable.proceedToStable();
                requests++;
                return new StableMessage(StableMessageTypes.PROCEED_TO_STABLE,
                        h.getRaceID(), h.getRaceIdx(), inMessage.getEntityId());

            case ENTERTAIN_THE_GUESTS:
                stable.entertainTheGuests();
                requests++;
                return new StableMessage(StableMessageTypes.ENTERTAIN_THE_GUESTS,
                        inMessage.getEntityId());

            default:
                return new StableMessage(
                        inMessage, "Invalid message type");
        }
    }

    /**
     * Method that returns the counter that registers the number of Horse/Jockey
     * pairs that have already invoked the PROCEED_TO_STABLE method (the total
     * number will be twice the number of pairs) and if the Broker has already
     * invoked the CELEBRATE method.
     * @return The counter that registers the number of Horse/Jockey pairs that
     * have already invoked the PROCEED_TO_STABLE method (the totalnumber will
     * be twice the number of pairs) and if the Broker has alreadyinvoked
     * the CELEBRATE method.
     */
    public int getRequests() {
        return requests;
    }
}
