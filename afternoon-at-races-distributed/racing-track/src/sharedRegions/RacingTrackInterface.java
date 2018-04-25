package sharedRegions;

import entities.HorseInt;
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
                if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
                if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
                if (inMessage.getCurrentPosition() < 0
                        || inMessage.getCurrentStep() < 0)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);

                ((HorseInt) Thread.currentThread()).setRaceID(
                        inMessage.getRaceID());
                ((HorseInt) Thread.currentThread()).setRaceIdx(
                        inMessage.getRaceIdx());
                ((HorseInt) Thread.currentThread()).setID(
                        inMessage.getEntityId());
                ((HorseInt) Thread.currentThread()).setCurrentPosition(
                        inMessage.getCurrentPosition());
                ((HorseInt) Thread.currentThread()).setCurrentStep(
                        inMessage.getCurrentStep());

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
                if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
                if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
                if (inMessage.getCurrentPosition() < 0
                        || inMessage.getCurrentStep() < 0)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);

                ((HorseInt) Thread.currentThread()).setRaceID(
                        inMessage.getRaceID());
                ((HorseInt) Thread.currentThread()).setRaceIdx(
                        inMessage.getRaceIdx());
                ((HorseInt) Thread.currentThread()).setID(
                        inMessage.getEntityId());
                ((HorseInt) Thread.currentThread()).setCurrentPosition(
                        inMessage.getCurrentPosition());
                ((HorseInt) Thread.currentThread()).setCurrentStep(
                        inMessage.getCurrentStep());

                racingTrack.makeAMove(step);
                return new RacingTrackMessage(
                        RacingTrackMessageTypes.MAKE_A_MOVE, inMessage.getEntityId());

            case PROCEED_TO_START_LINE:
                if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
                if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
                if (inMessage.getCurrentPosition() < 0
                        || inMessage.getCurrentStep() < 0)
                    return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);

                ((HorseInt) Thread.currentThread()).setRaceID(
                        inMessage.getRaceID());
                ((HorseInt) Thread.currentThread()).setRaceIdx(
                        inMessage.getRaceIdx());
                ((HorseInt) Thread.currentThread()).setID(
                        inMessage.getEntityId());
                ((HorseInt) Thread.currentThread()).setCurrentPosition(
                        inMessage.getCurrentPosition());
                ((HorseInt) Thread.currentThread()).setCurrentStep(
                        inMessage.getCurrentStep());

                racingTrack.proceedToStartLine();
                return new RacingTrackMessage(
                        RacingTrackMessageTypes.PROCEED_TO_START_LINE,
                        inMessage.getEntityId());

            case START_THE_RACE:
                racingTrack.startTheRace();
                return new RacingTrackMessage(
                        RacingTrackMessageTypes.START_THE_RACE,
                        inMessage.getEntityId());

            default:
                return new RacingTrackMessage(RacingTrackMessageTypes.ERROR);
        }
    }

    public int getRequests() {
        return requests;
    }
}
