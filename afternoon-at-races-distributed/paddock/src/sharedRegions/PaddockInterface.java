package sharedRegions;

import entities.HorseInt;
import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.PaddockMessageTypes;
import messages.PaddockMessage;
import sharedRegions.Paddock;


public class PaddockInterface {

    private int requests;
    private Paddock paddock;

    public PaddockInterface(Paddock paddock) {
        if (paddock == null)
            throw new IllegalArgumentException("Invalid Paddock.");

        this.paddock = paddock;
    }

    public PaddockMessage processAndReply(PaddockMessage inMessage) {
        PaddockMessageTypes mType;

        if ((mType = PaddockMessageTypes.getType(inMessage.getMethod())) == null)
            return new PaddockMessage(PaddockMessageTypes.ERROR);

        switch (mType) {
            case GO_CHECK_HORSES:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS)
                    return new PaddockMessage(PaddockMessageTypes.ERROR);

                ((SpectatorInt) Thread.currentThread()).setID(inMessage.getEntityId());

                paddock.goCheckHorses();
                requests++;
                return new PaddockMessage(PaddockMessageTypes.GO_CHECK_HORSES,
                        inMessage.getEntityId());

            case PROCEED_TO_PADDOCK:
                if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES)
                    return new PaddockMessage(PaddockMessageTypes.ERROR);
                if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new PaddockMessage(PaddockMessageTypes.ERROR);
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    return new PaddockMessage(PaddockMessageTypes.ERROR);

                ((HorseInt) Thread.currentThread()).setRaceID(inMessage.getRaceID());
                ((HorseInt) Thread.currentThread()).setRaceIdx(inMessage.getRaceIdx());
                ((HorseInt) Thread.currentThread()).setID(inMessage.getEntityId());

                paddock.goCheckHorses();
                requests++;
                return new PaddockMessage(PaddockMessageTypes.PROCEED_TO_PADDOCK,
                        inMessage.getEntityId());

            default:
                return new PaddockMessage(PaddockMessageTypes.ERROR);
        }
    }

    public int getRequests() {
        return requests;
    }
}
