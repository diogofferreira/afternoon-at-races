package sharedRegions;

import main.EventVariables;
import messageTypes.GeneralRepositoryMessageTypes;
import messages.GeneralRepositoryMessage;
import states.BrokerState;
import states.HorseState;
import states.SpectatorState;


public class GeneralRepositoryInterface {

    private GeneralRepository generalRepository;

    private int requests;

    public GeneralRepositoryInterface(GeneralRepository generalRepository) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");

        this.generalRepository = generalRepository;
        this.requests = 0;
    }

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
            return new GeneralRepositoryMessage(GeneralRepositoryMessageTypes.ERROR);

        switch (mType) {
            case INIT_RACE:
                raceID = inMessage.getRaceNumber();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES) {
                    inMessage.setErrorMessage("Invalid race ID");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }

                generalRepository.initRace(raceID);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.INIT_RACE,
                        inMessage.getEntityId());

            case SET_BROKER_STATE:
                brokerState = BrokerState.getType(inMessage.getBrokerState());

                if (brokerState == null) {
                    inMessage.setErrorMessage("Invalid broker state");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }

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

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES) {
                    inMessage.setErrorMessage("Invalid race ID");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }
                if (horseIdx < 0 || horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                    inMessage.setErrorMessage("Invalid horse Idx");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }
                if (horseAgility < 1 || horseAgility > EventVariables.HORSE_MAX_STEP) {
                    inMessage.setErrorMessage("Invalid horse agility");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }

                generalRepository.setHorseAgility(raceID, horseIdx, horseAgility);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSE_AGILITY,
                        inMessage.getEntityId());

            case SET_HORSE_POSITION:
                horseIdx = inMessage.getHorseIdx();
                horsePosition = inMessage.getHorsePosition();
                horseStep = inMessage.getHorseStep();

                if (horseIdx < 0 ||
                        horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                    inMessage.setErrorMessage("Invalid horse Idx");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }
                if (horsePosition < 0) {
                    inMessage.setErrorMessage("Invalid horse position");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }
                if (horseStep < 0) {
                    inMessage.setErrorMessage("Invalid horse step");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }

                generalRepository.setHorsePosition(
                        horseIdx, horsePosition, horseStep);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSE_POSITION,
                        inMessage.getEntityId());

            case SET_HORSES_ODD:
                raceID = inMessage.getRaceNumber();
                horsesOdd = inMessage.getHorsesOdd();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES) {
                    inMessage.setErrorMessage("Invalid race ID");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }
                if (horsesOdd == null ||
                        horsesOdd.length != EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                    inMessage.setErrorMessage("Invalid horse odds array");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }

                generalRepository.setHorsesOdd(raceID, horsesOdd);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSES_ODD,
                        inMessage.getEntityId());

            case SET_HORSES_STANDING:
                standings = inMessage.getStandings();

                if (standings == null ||
                        standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                    inMessage.setErrorMessage("Invalid standings array");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }

                generalRepository.setHorsesStanding(standings);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSES_STANDING,
                        inMessage.getEntityId());

            case SET_HORSE_STATE:
                raceID = inMessage.getRaceNumber();
                horseIdx = inMessage.getHorseIdx();
                horseState = HorseState.getType(inMessage.getHorseState());

                if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES) {
                    inMessage.setErrorMessage("Invalid race ID");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }
                if (horseIdx < 0 ||
                        horseIdx > EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                    inMessage.setErrorMessage("Invalid horse Idx");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }
                if (horseState == null) {
                    inMessage.setErrorMessage("Invalid horse state");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }

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
                        spectatorID >= EventVariables.NUMBER_OF_SPECTATORS) {
                    inMessage.setErrorMessage("Invalid spectator ID");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }

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
                        spectatorID > EventVariables.NUMBER_OF_SPECTATORS) {
                    inMessage.setErrorMessage("Invalid spectator ID");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }
                if (spectatorBet < 0) {
                    inMessage.setErrorMessage("Invalid spectator bet");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }
                if (spectatorBettedHorse < 0 ||
                        spectatorBettedHorse > EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                    inMessage.setErrorMessage("Invalid spectator betted horse");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }

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
                        spectatorID > EventVariables.NUMBER_OF_SPECTATORS) {
                    inMessage.setErrorMessage("Invalid spectator ID");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }
                if (spectatorState == null) {
                    inMessage.setErrorMessage("Invalid spectator state");
                    inMessage.setMethod(GeneralRepositoryMessageTypes.ERROR);
                    return inMessage;
                }

                generalRepository.setSpectatorState(spectatorID, spectatorState);


                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_SPECTATOR_STATE,
                        inMessage.getEntityId());

            default:
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.ERROR);
        }
    }

    public int getRequests() {
        return requests;
    }

}
