package sharedRegionsInterfaces;

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

        switch (mType) {
            case SUMMON_HORSES_TO_PADDOCK:
                int raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

                controlCentre.summonHorsesToPaddock(inMessage.getRaceId());
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK, 0);

            case WAIT_FOR_NEXT_RACE:
                boolean isThereARace = controlCentre.waitForNextRace();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE,
                        isThereARace, 0);

            case PROCEED_TO_PADDOCK:
                controlCentre.proceedToPaddock();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.PROCEED_TO_PADDOCK, 0);

            case GO_CHECK_HORSES:
                controlCentre.goCheckHorses();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.GO_CHECK_HORSES, 0);

            case GO_WATCH_THE_RACE:
                controlCentre.goWatchTheRace();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.GO_WATCH_THE_RACE, 0);

            case START_THE_RACE:
                controlCentre.startTheRace();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.START_THE_RACE, 0);

            case FINISH_THE_RACE:
                int[] standings = inMessage.getStandings();

                if (standings == null ||
                        standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

                controlCentre.finishTheRace(standings);
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.FINISH_THE_RACE, 0);

            case REPORT_RESULTS:
                int[] winners = controlCentre.reportResults();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.REPORT_RESULTS, winners, 0);

            case HAVE_I_WON:
                boolean haveIWon;
                int horseIdx = inMessage.getHorseIdx();

                if (horseIdx < 0 ||
                        horseIdx > EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);

                haveIWon = controlCentre.haveIWon(horseIdx);
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.HAVE_I_WON, haveIWon, 0);

            case CELEBRATE:
                controlCentre.celebrate();
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.CELEBRATE, 0);

            case RELAX_A_BIT:
                controlCentre.relaxABit();
                requests++;
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.RELAX_A_BIT, 0);

            default:
                return new ControlCentreMessage(ControlCentreMessageTypes.ERROR);
        }
    }

    public int getRequests() {
        return requests;
    }
}
