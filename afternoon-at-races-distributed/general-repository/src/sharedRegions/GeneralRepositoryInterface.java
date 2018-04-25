package sharedRegions;

import main.EventVariables;
import messageTypes.GeneralRepositoryMessageTypes;
import messages.GeneralRepositoryMessage;
import sharedRegions.GeneralRepository;
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

        System.out.println(inMessage.toString());

        if ((mType = GeneralRepositoryMessageTypes.getType(inMessage.getMethod())) == null)
            return new GeneralRepositoryMessage(GeneralRepositoryMessageTypes.ERROR);

        switch (mType) {
            case INIT_RACE:
                raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);

                generalRepository.initRace(raceID);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.INIT_RACE,
                        inMessage.getEntityId());

            case SET_BROKER_STATE:
                brokerState = BrokerState.getType(inMessage.getBrokerState());

                if (brokerState == null)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);

                generalRepository.setBrokerState(brokerState);

                // Update counter
                if (brokerState == BrokerState.PLAYING_HOST_AT_THE_BAR)
                    requests++;

                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_BROKER_STATE,
                        inMessage.getEntityId());

            case SET_HORSE_AGILITY:
                raceID = inMessage.getRaceId();
                horseIdx = inMessage.getHorseIdx();
                horseAgility = inMessage.getHorseAgility();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);
                if (horseIdx < 0 || horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);
                if (horseAgility < 1 || horseAgility > EventVariables.HORSE_MAX_STEP)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);

                generalRepository.setHorseAgility(raceID, horseIdx, horseAgility);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSE_AGILITY,
                        inMessage.getEntityId());

            case SET_HORSE_POSITION:
                horseIdx = inMessage.getHorseIdx();
                horsePosition = inMessage.getHorsePosition();
                horseStep = inMessage.getHorseStep();

                if (horseIdx < 0 || horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);
                if (horsePosition < 0)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);
                if (horseStep < 0)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);

                generalRepository.setHorsePosition(
                        horseIdx, horsePosition, horseStep);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSE_POSITION,
                        inMessage.getEntityId());

            case SET_HORSES_ODD:
                raceID = inMessage.getRaceId();
                horsesOdd = inMessage.getHorsesOdd();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);
                if (horsesOdd == null ||
                        horsesOdd.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);

                generalRepository.setHorsesOdd(raceID, horsesOdd);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSES_ODD,
                        inMessage.getEntityId());

            case SET_HORSES_STANDING:
                standings = inMessage.getStandings();

                if (standings == null ||
                        standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);

                generalRepository.setHorsesStanding(standings);
                return new GeneralRepositoryMessage(
                        GeneralRepositoryMessageTypes.SET_HORSES_STANDING,
                        inMessage.getEntityId());

            case SET_HORSE_STATE:
                raceID = inMessage.getRaceId();
                horseIdx = inMessage.getHorseIdx();
                horseState = HorseState.getType(inMessage.getHorseState());

                if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);
                if (horseIdx < 0 || horseIdx > EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);
                if (horseState == null)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);

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
                amount = inMessage.getSpectatorBet();

                if (spectatorID < 0 ||
                        spectatorID >= EventVariables.NUMBER_OF_SPECTATORS)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);

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
                            GeneralRepositoryMessageTypes.ERROR);
                if (spectatorBet < 0)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);
                if (spectatorBettedHorse < 0 ||
                        spectatorBettedHorse > EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);

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
                            GeneralRepositoryMessageTypes.ERROR);
                if (spectatorState == null)
                    return new GeneralRepositoryMessage(
                            GeneralRepositoryMessageTypes.ERROR);

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
