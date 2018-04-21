package sharedRegionsInterfaces;

import messageTypes.PaddockMessageTypes;
import messages.PaddockMessage;
import sharedRegions.Paddock;


public class PaddockInterface {

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
                paddock.goCheckHorses();
                return new PaddockMessage(PaddockMessageTypes.GO_CHECK_HORSES,
                        inMessage.getEntityId());

            case PROCEED_TO_PADDOCK:
                paddock.goCheckHorses();
                return new PaddockMessage(PaddockMessageTypes.PROCEED_TO_PADDOCK,
                        inMessage.getEntityId());

            default:
                return new PaddockMessage(PaddockMessageTypes.ERROR);
        }
    }
}
