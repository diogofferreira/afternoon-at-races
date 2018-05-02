package sharedRegions;

import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.ControlCentreMessageTypes;
import messages.ControlCentreMessage;

/**
 * Interface of the Control Centre server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class ControlCentreInterface {

    /**
     * Counter that registers the number of Spectators that have already invoked
     * the RELAX_A_BIT method. It is useful to close the server socket.
     */
    private int requests;

    /**
     * Instance of the Control Centre shared region.
     */
    private ControlCentre controlCentre;

    /**
     * Creates a new instance of an interface of the Control Centre shared region.
     * @param controlCentre Instance of the Control Centre shared region.
     */
    public ControlCentreInterface(ControlCentre controlCentre) {
        if (controlCentre == null)
            throw new IllegalArgumentException("Invalid Control Centre.");

        this.requests = 0;
        this.controlCentre = controlCentre;
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
    public ControlCentreMessage processAndReply(ControlCentreMessage inMessage) {
        ControlCentreMessageTypes mType;

        if ((mType = ControlCentreMessageTypes.getType(inMessage.getMethod())) == null)
            return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

        if (inMessage.getEntityId() < 0)
            return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

        switch (mType) {
            case OPEN_THE_EVENT:
                controlCentre.openTheEvent();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.OPEN_THE_EVENT,
                        inMessage.getEntityId());

            case SUMMON_HORSES_TO_PADDOCK:
                int raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES) {
                    inMessage.setErrorMessage("Invalid raceID");
                    inMessage.setMethod(ControlCentreMessageTypes.ERROR);
                    return inMessage;
                }

                controlCentre.summonHorsesToPaddock(inMessage.getRaceId());
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK,
                        inMessage.getEntityId());

            case WAIT_FOR_NEXT_RACE:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS) {
                    inMessage.setErrorMessage("Invalid spectator ID");
                    inMessage.setMethod(ControlCentreMessageTypes.ERROR);
                    return inMessage;
                }

                ((SpectatorInt) Thread.currentThread()).setID(
                        inMessage.getEntityId());

                boolean isThereARace = controlCentre.waitForNextRace();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE,
                        isThereARace, inMessage.getEntityId());

            case PROCEED_TO_PADDOCK:
                controlCentre.proceedToPaddock();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.PROCEED_TO_PADDOCK,
                        inMessage.getEntityId());

            case GO_CHECK_HORSES:
                controlCentre.goCheckHorses();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.GO_CHECK_HORSES,
                        inMessage.getEntityId());

            case GO_WATCH_THE_RACE:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS) {
                    inMessage.setErrorMessage("Invalid spectator ID");
                    inMessage.setMethod(ControlCentreMessageTypes.ERROR);
                    return inMessage;
                }

                ((SpectatorInt) Thread.currentThread()).setID(
                        inMessage.getEntityId());
                controlCentre.goWatchTheRace();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.GO_WATCH_THE_RACE,
                        inMessage.getEntityId());

            case START_THE_RACE:
                controlCentre.startTheRace();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.START_THE_RACE,
                        inMessage.getEntityId());

            case FINISH_THE_RACE:
                int[] standings = inMessage.getStandings();

                if (standings == null ||
                        standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                    inMessage.setErrorMessage("Invalid standings array");
                    inMessage.setMethod(ControlCentreMessageTypes.ERROR);
                    return inMessage;
                }

                controlCentre.finishTheRace(standings);
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.FINISH_THE_RACE,
                        inMessage.getEntityId());

            case REPORT_RESULTS:
                int[] winners = controlCentre.reportResults();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.REPORT_RESULTS, winners,
                        inMessage.getEntityId());

            case HAVE_I_WON:
                boolean haveIWon;
                int horseIdx = inMessage.getHorseIdx();

                if (horseIdx < 0 ||
                        horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                    inMessage.setErrorMessage("Invalid horse Idx");
                    inMessage.setMethod(ControlCentreMessageTypes.ERROR);
                    return inMessage;
                }

                haveIWon = controlCentre.haveIWon(horseIdx);
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.HAVE_I_WON, haveIWon,
                        inMessage.getEntityId());

            case CELEBRATE:
                controlCentre.celebrate();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.CELEBRATE,
                        inMessage.getEntityId());

            case RELAX_A_BIT:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS) {
                    inMessage.setErrorMessage("Invalid spectator ID");
                    inMessage.setMethod(ControlCentreMessageTypes.ERROR);
                    return inMessage;
                }

                ((SpectatorInt) Thread.currentThread()).setID(
                        inMessage.getEntityId());

                controlCentre.relaxABit();
                requests++;
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.RELAX_A_BIT,
                        inMessage.getEntityId());

            default:
                return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);
        }
    }

    /**
     * Method that returns the counter that registers the number of Spectators
     * that have already invoked the RELAX_A_BIT method.
     * @return The counter that registers the number of Spectators
     * that have already invoked the RELAX_A_BIT method.
     */
    public int getRequests() {
        return requests;
    }
}
