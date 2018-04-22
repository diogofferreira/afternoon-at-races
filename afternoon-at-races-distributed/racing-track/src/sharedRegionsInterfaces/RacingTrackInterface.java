package sharedRegionsInterfaces;

import main.EventVariables;
import messageTypes.RacingTrackMessageTypes;
import messages.RacingTrackMessage;
import sharedRegions.RacingTrack;

public class RacingTrackInterface {

    private int requests;
    private RacingTrack racingTrack;

    public RacingTrackInterface(RacingTrack racingTrack) {
        if (racingTrack == null)
            throw new IllegalArgumentException("Invalid Stable.");

        this.racingTrack = racingTrack;
    }

    public RacingTrackMessage processAndReply(RacingTrackMessage inMessage) {
        RacingTrackMessageTypes mType;
        int step;
        boolean hasFinishLineBeenCrossed;

        if ((mType = RacingTrackMessageTypes.getType(inMessage.getMethod())) == null)
            return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);

        switch (mType) {
            case HAS_FINISH_LINE_BEEN_CROSSED:
                hasFinishLineBeenCrossed = racingTrack.hasFinishLineBeenCrossed();
                if (hasFinishLineBeenCrossed)
                    requests++;
                return new RacingTrackMessage(
                        RacingTrackMessageTypes.HAS_FINISH_LINE_BEEN_CROSSED,
                        hasFinishLineBeenCrossed, inMessage.getEntityId());

            case MAKE_A_MOVE:
                step = inMessage.getStep();

                if (step < 1 || step > EventVariables.HORSE_MAX_STEP)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);

                racingTrack.makeAMove(step);
                return new RacingTrackMessage(
                        RacingTrackMessageTypes.MAKE_A_MOVE, inMessage.getEntityId());

            default:
                return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
        }
    }

    public int getRequests() {
        return requests;
    }
}
