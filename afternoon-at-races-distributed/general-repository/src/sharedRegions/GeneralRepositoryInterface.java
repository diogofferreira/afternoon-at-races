package sharedRegions;

import main.EventVariables;
import messageTypes.GeneralRepositoryMessageTypes;
import messages.GeneralRepositoryMessage;
import states.BrokerState;
import states.HorseState;
import states.SpectatorState;

/**
 * Interface of the General Repository server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class GeneralRepositoryInterface {

    /**
     * Counter that registers the number of Spectators that have already invoked
     * the SET_SPECTATOR method when the update state corresponds to CELEBRATING.
     * It is useful to close the server socket.
     */
    private int requests;

    /**
     * Instance of the General Repository shared region.
     */
    private GeneralRepository generalRepository;

    /**
     * Creates a new instance of an interface of the General Repository
     * shared region.
     * @param generalRepository Instance of the General Repository shared region.
     */
    public GeneralRepositoryInterface(GeneralRepository generalRepository) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");

        this.generalRepository = generalRepository;
        this.requests = 0;
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

                generalRepository.initRace(raceID);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.INIT_RACE,
                        inMessage.getEntityId());

            case SET_BROKER_STATE:
                brokerState = BrokerState.getType(inMessage.getBrokerState());

                if (brokerState == null)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid broker state");

                generalRepository.setBrokerState(brokerState);

                // Update counter
                if (brokerState == BrokerState.PLAYING_HOST_AT_THE_BAR)
                    requests++;

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

                generalRepository.setHorseAgility(raceID, horseIdx, horseAgility);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSE_AGILITY,
                        inMessage.getEntityId());

            case SET_HORSE_POSITION:
                horseIdx = inMessage.getHorseIdx();
                horsePosition = inMessage.getHorsePosition();
                horseStep = inMessage.getHorseStep();

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

                generalRepository.setHorsePosition(
                        horseIdx, horsePosition, horseStep);
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

                generalRepository.setHorsesOdd(raceID, horsesOdd);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSES_ODD,
                        inMessage.getEntityId());

            case SET_HORSES_STANDING:
                standings = inMessage.getStandings();

                if (standings == null ||
                        standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid standings array");

                generalRepository.setHorsesStanding(standings);
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

                generalRepository.setHorseState(raceID, horseIdx, horseState);

                // Update internal counters
                if (raceID == EventVariables.NUMBER_OF_RACES - 1
                        && horseState == HorseState.AT_THE_STABLE)
                    requests++;

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

                generalRepository.setSpectatorGains(spectatorID, amount);
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

                generalRepository.setSpectatorsBet(
                        spectatorID, spectatorBet, spectatorBettedHorse);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_SPECTATORS_BET,
                        inMessage.getEntityId());

            case SET_SPECTATOR_STATE:
                spectatorID = inMessage.getEntityId();
                spectatorState = SpectatorState.getType(
                        inMessage.getSpectatorState());

                // Update counter
                if (spectatorState == SpectatorState.CELEBRATING)
                    requests++;

                if (spectatorID < 0 ||
                        spectatorID > EventVariables.NUMBER_OF_SPECTATORS)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid spectator ID");
                if (spectatorState == null)
                    return new GeneralRepositoryMessage(
                            inMessage, "Invalid spectator state");

                generalRepository.setSpectatorState(spectatorID, spectatorState);


                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_SPECTATOR_STATE,
                        inMessage.getEntityId());

            default:
                return new GeneralRepositoryMessage(
                        inMessage, "Invalid message type");
        }
    }

    public int getRequests() {
        return requests;
    }

}
