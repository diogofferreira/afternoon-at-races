package messages;

import messageTypes.GeneralRepositoryMessageTypes;

import java.io.Serializable;
import java.util.Arrays;

public class GeneralRepositoryMessage implements Serializable {

    private static final long serialVersionUID = 1003L;

    // method type
    private int method;

    // arguments
    private int brokerState;
    private int spectatorState;
    private int horseState;
    private int raceNumber;
    private int horseIdx;
    private int horseAgility;
    private int horsePosition;
    private int horseStep;
    private int spectatorBet;
    private int spectatorBettedHorse;
    private int[] standings;
    private double[] horsesOdd;

    // entity id
    private int entityId;


    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes error) {
        this.method = error.getId();
    }

    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int stateOrAmountOrRaceNumber, int entityId) {
        this.method = method.getId();
        switch (method) {
            case SET_BROKER_STATE:
                this.brokerState = stateOrAmountOrRaceNumber;
                break;
            case SET_SPECTATOR_STATE:
                this.spectatorState = stateOrAmountOrRaceNumber;
                break;
            case SET_SPECTATORS_BET:
                this.spectatorBet = stateOrAmountOrRaceNumber;
                break;
            case INIT_RACE:
                this.raceNumber = stateOrAmountOrRaceNumber;
                break;
        }
        this.entityId = entityId;
    }

    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int raceIdOrHorsePosition, int horseIdx,
                                    int stateOrAgilityOrStep,
                                    int entityId) {
        this.method = method.getId();
        this.horseIdx = horseIdx;
        switch (method) {
            case SET_HORSE_STATE:
                this.horseState = stateOrAgilityOrStep;
                this.raceNumber = raceIdOrHorsePosition;
                break;
            case SET_HORSE_AGILITY:
                this.horseAgility = stateOrAgilityOrStep;
                this.raceNumber = raceIdOrHorsePosition;
                break;
            case SET_HORSE_POSITION:
                this.horseStep = stateOrAgilityOrStep;
                this.horsePosition = raceIdOrHorsePosition;
        }
        this.entityId = entityId;
    }

    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int spectatorBet, int bettedHorse, int entityId) {
        this.method = method.getId();
        this.spectatorBettedHorse = bettedHorse;
        this.spectatorBet = spectatorBet;
        this.entityId = entityId;
    }

    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int raceId, double[] horsesOdd, int entityId) {
        this.method = method.getId();
        this.raceNumber = raceId;
        this.horsesOdd = horsesOdd;
        this.entityId = entityId;
    }

    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int[] standings, int entityId) {
        this.method = method.getId();
        this.standings = standings;
        this.entityId = entityId;
    }

    public int getBrokerState() {
        return brokerState;
    }

    public int getSpectatorState() {
        return spectatorState;
    }

    public int getHorseState() {
        return horseState;
    }

    public int getMethod() {
        return method;
    }


    public int getRaceNumber() {
        return raceNumber;
    }

    public int getHorseIdx() {
        return horseIdx;
    }

    public int getHorseAgility() {
        return horseAgility;
    }

    public int getHorsePosition() {
        return horsePosition;
    }

    public int getHorseStep() {
        return horseStep;
    }

    public int getSpectatorBet() {
        return spectatorBet;
    }

    public int getSpectatorBettedHorse() {
        return spectatorBettedHorse;
    }

    public int[] getStandings() {
        return standings;
    }

    public double[] getHorsesOdd() {
        return horsesOdd;
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public String toString() {
        return "GeneralRepositoryMessage{" +
                "method=" + method +
                ", brokerState=" + brokerState +
                ", spectatorState=" + spectatorState +
                ", horseState=" + horseState +
                ", raceNumber=" + raceNumber +
                ", horseIdx=" + horseIdx +
                ", horseAgility=" + horseAgility +
                ", horsePosition=" + horsePosition +
                ", horseStep=" + horseStep +
                ", spectatorBet=" + spectatorBet +
                ", spectatorBettedHorse=" + spectatorBettedHorse +
                ", standings=" + Arrays.toString(standings) +
                ", horsesOdd=" + Arrays.toString(horsesOdd) +
                ", entityId=" + entityId +
                '}';
    }
}
