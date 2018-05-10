package sharedRegions;

import entities.HorseInt;
import main.EventVariables;
import messageTypes.RacingTrackMessageTypes;
import messages.RacingTrackMessage;
import sharedRegions.RacingTrack;

/**
 * Interface of the Racing Track server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class RacingTrackInterface {

    /**
     * The counter that registers the number of Horse/Jockey pairs that have
     * already invoked the HAS_FINISH_LINE_BEEN_CROSSED method.
     */
    private int requests;

    /**
     * Instance of the Racing Track shared region.
     */
    private RacingTrack racingTrack;

    /**
     * Creates a new instance of an interface of the Racing Track shared region.
     * @param racingTrack Instance of the Racing Track shared region.
     */
    public RacingTrackInterface(RacingTrack racingTrack) {
        if (racingTrack == null)
            throw new IllegalArgumentException("Invalid Stable.");

        this.racingTrack = racingTrack;
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
    public RacingTrackMessage processAndReply(RacingTrackMessage inMessage) {
        RacingTrackMessageTypes mType;
        int step;
        boolean hasFinishLineBeenCrossed;

        if ((mType = RacingTrackMessageTypes.getType(inMessage.getMethod())) == null)
            return new RacingTrackMessage(
                    inMessage, "Invalid message type");

        switch (mType) {
            case HAS_FINISH_LINE_BEEN_CROSSED:
                if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES)
                    return new RacingTrackMessage(
                            inMessage, "Invalid race ID");
                if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new RacingTrackMessage(
                            inMessage, "Invalid horse Idx");
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    return new RacingTrackMessage(
                            inMessage, "Invalid horse ID");
                if (inMessage.getCurrentPosition() < 0
                        || inMessage.getCurrentStep() < 0)
                    return new RacingTrackMessage(
                            inMessage, "Invalid horse position and/or step");

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
                    return new RacingTrackMessage(
                            inMessage, "Invalid step");
                if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES)
                    return new RacingTrackMessage(
                            inMessage, "Invalid race ID");
                if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new RacingTrackMessage(
                            inMessage, "Invalid horse Idx");
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    return new RacingTrackMessage(
                            inMessage, "Invalid horse ID");
                if (inMessage.getCurrentPosition() < 0
                        || inMessage.getCurrentStep() < 0)
                    return new RacingTrackMessage(
                            inMessage, "Invalid horse position and/or step");

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
                    return new RacingTrackMessage(
                            inMessage, "Invalid race ID");
                if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new RacingTrackMessage(
                            inMessage, "Invalid horse Idx");
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    return new RacingTrackMessage(
                            inMessage, "Invalid horse ID");
                if (inMessage.getCurrentPosition() < 0
                        || inMessage.getCurrentStep() < 0)
                    return new RacingTrackMessage(
                            inMessage, "Invalid horse position and/or step");

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
                return new RacingTrackMessage(
                        inMessage, "Invalid message type");
        }
    }

    /**
     * Method that returns the counter that registers the number of Horse/Jockey
     * pairs that have already invoked the HAS_FINISH_LINE_BEEN_CROSSED method.
     * @return The counter that registers the number of Horse/Jockey pairs
     * that have already invoked the HAS_FINISH_LINE_BEEN_CROSSED method.
     */
    public int getRequests() {
        return requests;
    }
}
