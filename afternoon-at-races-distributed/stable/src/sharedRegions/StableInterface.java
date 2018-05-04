package sharedRegions;

import entities.HorseInt;
import main.EventVariables;
import messageTypes.StableMessageTypes;
import messages.StableMessage;
import sharedRegions.Stable;

public class StableInterface {

    private int requests;
    private Stable stable;

    public StableInterface(Stable stable) {
        if (stable == null)
            throw new IllegalArgumentException("Invalid Stable.");

        this.stable = stable;
    }

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

    public int getRequests() {
        return requests;
    }
}
