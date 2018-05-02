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
            return new StableMessage(StableMessageTypes.ERROR);

        switch (mType) {
            case GET_RACE_ODDS:
                raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES) {
                    inMessage.setErrorMessage("Invalid race ID");
                    inMessage.setMethod(StableMessageTypes.ERROR);
                    return inMessage;
                }

                odds = stable.getRaceOdds(raceID);
                return new StableMessage(StableMessageTypes.GET_RACE_ODDS,
                        odds, inMessage.getEntityId());

            case SUMMON_HORSES_TO_PADDOCK:
                raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES) {
                    inMessage.setErrorMessage("Invalid race ID");
                    inMessage.setMethod(StableMessageTypes.ERROR);
                    return inMessage;
                }

                stable.summonHorsesToPaddock(raceID);
                return new StableMessage(
                        StableMessageTypes.SUMMON_HORSES_TO_PADDOCK,
                        inMessage.getEntityId());

            case PROCEED_TO_STABLE:
                h = (HorseInt) Thread.currentThread();

                if (inMessage.getAgility() < 0 ||
                        inMessage.getAgility() > EventVariables.HORSE_MAX_STEP) {
                    inMessage.setErrorMessage("Invalid horse agility");
                    inMessage.setMethod(StableMessageTypes.ERROR);
                    return inMessage;
                }
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES) {
                    inMessage.setErrorMessage("Invalid horse ID");
                    inMessage.setMethod(StableMessageTypes.ERROR);
                    return inMessage;
                }

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
                return new StableMessage(StableMessageTypes.ERROR);
        }
    }

    public int getRequests() {
        return requests;
    }
}
