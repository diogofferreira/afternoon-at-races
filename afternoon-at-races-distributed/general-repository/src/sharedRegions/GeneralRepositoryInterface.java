package sharedRegions;

import main.EventVariables;
import messageTypes.GeneralRepositoryMessageTypes;
import messages.GeneralRepositoryMessage;
import serverStates.GeneralRepositoryClientsState;
import states.BrokerState;
import states.HorseState;
import states.SpectatorState;

import java.util.Arrays;

/**
 * Interface of the General Repository server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class GeneralRepositoryInterface {

    /**
     * Instance of the General Repository shared region.
     */
    private GeneralRepository generalRepository;

    /**
     * Array with the states of active entities.
     */
    private GeneralRepositoryClientsState[] grStates;

    /**
     * Creates a new instance of an interface of the General Repository
     * shared region.
     * @param generalRepository Instance of the General Repository shared region.
     */
    public GeneralRepositoryInterface(GeneralRepository generalRepository) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");

        this.generalRepository = generalRepository;
        this.grStates = new GeneralRepositoryClientsState[
                1 + EventVariables.NUMBER_OF_HORSES +
                        EventVariables.NUMBER_OF_SPECTATORS];
        for (int i = 0; i < this.grStates.length; i++)
            this.grStates[i] = new GeneralRepositoryClientsState();
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
    public GeneralRepositoryMessage processAndReply(GeneralRepositoryMessage inMessage) {
        GeneralRepositoryMessageTypes mType;
        int raceID, horseIdx, horseAgility, horsePosition, horseStep,
                spectatorID, spectatorBet, spectatorBettedHorse, amount;
        BrokerState brokerState;
        double[] horsesOdd;
        int[] standings;
        HorseState horseState;
        SpectatorState spectatorState;

        if ((mType = GeneralRepositoryMessageTypes.getType(inMessage.getMethod())) == null)
            return new GeneralRepositoryMessage(
                    inMessage, "Invalid message type");

        switch (mType) {
            case INIT_RACE:
                raceID = inMessage.getRaceNumber();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid race ID");

                grStates[0].enterMonitor();
                generalRepository.initRace(raceID);
                grStates[0].exitMonitor();

                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.INIT_RACE,
                        inMessage.getEntityId());

            case SET_BROKER_STATE:
                brokerState = BrokerState.getType(inMessage.getBrokerState());

                if (brokerState == null)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid broker state");

                grStates[0].enterMonitor();
                boolean updated = generalRepository.setBrokerState(brokerState);

                // Update counter
                if (updated && brokerState == BrokerState.PLAYING_HOST_AT_THE_BAR)
                    grStates[0].increaseRequests();
                grStates[0].exitMonitor();

                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_BROKER_STATE,
                        inMessage.getEntityId());

            case SET_HORSE_AGILITY:
                raceID = inMessage.getRaceNumber();
                horseIdx = inMessage.getHorseIdx();
                horseAgility = inMessage.getHorseAgility();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid race ID");
                if (horseIdx < 0 || horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid horse Idx");
                if (horseAgility < 1 || horseAgility > EventVariables.HORSE_MAX_STEP)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid horse agility");

                grStates[1 + raceID * EventVariables.NUMBER_OF_HORSES_PER_RACE +
                        horseIdx].enterMonitor();
                generalRepository.setHorseAgility(raceID, horseIdx, horseAgility);
                grStates[1 + raceID * EventVariables.NUMBER_OF_HORSES_PER_RACE +
                        horseIdx].exitMonitor();
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSE_AGILITY,
                        inMessage.getEntityId());

            case SET_HORSE_POSITION:
                raceID = inMessage.getRaceNumber();
                horseIdx = inMessage.getHorseIdx();
                horsePosition = inMessage.getHorsePosition();
                horseStep = inMessage.getHorseStep();

                if (raceID <0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid race ID");
                if (horseIdx < 0 ||
                        horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid horse Idx");
                if (horsePosition < 0)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid horse position");
                if (horseStep < 0)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid horse step");

                grStates[1 + raceID * EventVariables.NUMBER_OF_HORSES_PER_RACE +
                        horseIdx].enterMonitor();
                generalRepository.setHorsePosition(
                        horseIdx, horsePosition, horseStep);
                grStates[1 + raceID * EventVariables.NUMBER_OF_HORSES_PER_RACE +
                        horseIdx].exitMonitor();
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSE_POSITION,
                        inMessage.getEntityId());

            case SET_HORSES_ODD:
                raceID = inMessage.getRaceNumber();
                horsesOdd = inMessage.getHorsesOdd();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid race ID");
                if (horsesOdd == null ||
                        horsesOdd.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid horse odds array");

                grStates[0].enterMonitor();
                generalRepository.setHorsesOdd(raceID, horsesOdd);
                grStates[0].exitMonitor();
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSES_ODD,
                        inMessage.getEntityId());

            case SET_HORSES_STANDING:
                standings = inMessage.getStandings();

                if (standings == null ||
                        standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid standings array");

                grStates[0].enterMonitor();
                generalRepository.setHorsesStanding(standings);
                grStates[0].exitMonitor();
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSES_STANDING,
                        inMessage.getEntityId());

            case SET_HORSE_STATE:
                raceID = inMessage.getRaceNumber();
                horseIdx = inMessage.getHorseIdx();
                horseState = HorseState.getType(inMessage.getHorseState());

                if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid race ID");
                if (horseIdx < 0 ||
                        horseIdx > EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid horse Idx");
                if (horseState == null)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid horse state");

                grStates[1 + raceID * EventVariables.NUMBER_OF_HORSES_PER_RACE +
                        horseIdx].enterMonitor();
                boolean hUpdated = generalRepository.setHorseState(
                        raceID, horseIdx, horseState);

                // Update internal counters
                if (hUpdated && raceID == EventVariables.NUMBER_OF_RACES - 1
                        && horseState == HorseState.AT_THE_STABLE)
                    grStates[1 + raceID * EventVariables.NUMBER_OF_HORSES_PER_RACE +
                            horseIdx].increaseRequests();
                grStates[1 + raceID * EventVariables.NUMBER_OF_HORSES_PER_RACE +
                        horseIdx].exitMonitor();

                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSE_STATE,
                        inMessage.getEntityId());

            case SET_SPECTATOR_GAINS:
                spectatorID = inMessage.getEntityId();
                amount = inMessage.getSpectatorGains();

                if (spectatorID < 0 ||
                        spectatorID >= EventVariables.NUMBER_OF_SPECTATORS)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid spectator ID");

                grStates[1 + EventVariables.NUMBER_OF_HORSES +
                        spectatorID].enterMonitor();
                generalRepository.setSpectatorGains(spectatorID, amount);
                grStates[1 + EventVariables.NUMBER_OF_HORSES +
                        spectatorID].exitMonitor();

                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_SPECTATOR_GAINS,
                        amount,
                        inMessage.getEntityId());

            case SET_SPECTATORS_BET:
                spectatorID = inMessage.getEntityId();
                spectatorBet = inMessage.getSpectatorBet();
                spectatorBettedHorse = inMessage.getSpectatorBettedHorse();

                if (spectatorID < 0 ||
                        spectatorID > EventVariables.NUMBER_OF_SPECTATORS)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid spectator ID");
                if (spectatorBet < 0)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid spectator bet");
                if (spectatorBettedHorse < 0 ||
                        spectatorBettedHorse > EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid spectator betted horse");

                grStates[1 + EventVariables.NUMBER_OF_HORSES +
                        spectatorID].enterMonitor();
                generalRepository.setSpectatorsBet(
                        spectatorID, spectatorBet, spectatorBettedHorse);
                grStates[1 + EventVariables.NUMBER_OF_HORSES +
                        spectatorID].exitMonitor();

                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_SPECTATORS_BET,
                        inMessage.getEntityId());

            case SET_SPECTATOR_STATE:
                spectatorID = inMessage.getEntityId();
                spectatorState = SpectatorState.getType(
                        inMessage.getSpectatorState());

                if (spectatorID < 0 ||
                        spectatorID > EventVariables.NUMBER_OF_SPECTATORS)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid spectator ID");
                if (spectatorState == null)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid spectator state");

                grStates[1 + EventVariables.NUMBER_OF_HORSES +
                        spectatorID].enterMonitor();
                boolean sUpdated = generalRepository.setSpectatorState(
                        spectatorID, spectatorState);

                // Update counter
                if (sUpdated && spectatorState == SpectatorState.CELEBRATING)
                    grStates[1 + EventVariables.NUMBER_OF_HORSES +
                            spectatorID].increaseRequests();

                grStates[1 + EventVariables.NUMBER_OF_HORSES +
                        spectatorID].exitMonitor();

                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_SPECTATOR_STATE,
                        inMessage.getEntityId());

            default:
                return new GeneralRepositoryMessage(
                        inMessage, "Invalid message type");
        }
    }

    public int getRequests() {
        return Arrays.stream(grStates).mapToInt(x -> x.getRequests()).sum();
    }

}
