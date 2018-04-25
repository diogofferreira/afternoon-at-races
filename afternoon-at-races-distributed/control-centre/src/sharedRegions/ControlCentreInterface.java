package sharedRegions;

import entities.BrokerInt;
import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.ControlCentreMessageTypes;
import messages.ControlCentreMessage;
import sharedRegions.ControlCentre;

public class ControlCentreInterface {

    private int requests;
    private ControlCentre controlCentre;

    public ControlCentreInterface(ControlCentre controlCentre) {
        if (controlCentre == null)
            throw new IllegalArgumentException("Invalid Control Centre.");

        this.requests = 0;
        this.controlCentre = controlCentre;
    }

    public ControlCentreMessage processAndReply(ControlCentreMessage inMessage) {
        ControlCentreMessageTypes mType;

        if ((mType = ControlCentreMessageTypes.getType(inMessage.getMethod())) == null)
            return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

        if (inMessage.getEntityId() < 0)
            return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

        switch (mType) {
            case SUMMON_HORSES_TO_PADDOCK:
                int raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

                controlCentre.summonHorsesToPaddock(inMessage.getRaceId());
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK,
                        inMessage.getEntityId());

            case WAIT_FOR_NEXT_RACE:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS)
                    return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

                ((SpectatorInt) Thread.currentThread()).setID(
                        inMessage.getEntityId());

                boolean isThereARace = controlCentre.waitForNextRace();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE,
                        isThereARace, 0);

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
                        standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

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
                        horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

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

    public int getRequests() {
        return requests;
    }
}
