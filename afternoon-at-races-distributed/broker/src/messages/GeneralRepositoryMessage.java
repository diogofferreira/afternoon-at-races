package messages;

import messageTypes.GeneralRepositoryMessageTypes;

import java.io.Serializable;

public class GeneralRepositoryMessage implements Serializable {
    // method type
    private int method;

    // arguments
    private int brokerState;
    private int spectatorState;
    private int horseState;
    private int raceId;
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

    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int stateOrAmountOrRaceNumber, int entityId) {
        this.method = method.getId();
        switch (this.method) {
            case 0:
                this.brokerState = stateOrAmountOrRaceNumber;
                break;
            case 1:
                this.spectatorState = stateOrAmountOrRaceNumber;
                break;
            case 3:
                this.spectatorBet = stateOrAmountOrRaceNumber;
                break;
            case 9:
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
        switch (this.method) {
            case 2:
                this.horseState = stateOrAgilityOrStep;
                this.raceId = raceIdOrHorsePosition;
                break;
            case 5:
                this.horseAgility = stateOrAgilityOrStep;
                this.raceId = raceIdOrHorsePosition;
                break;
            case 7:
                this.horseStep = stateOrAgilityOrStep;
                this.horsePosition = raceIdOrHorsePosition;
        }
        this.entityId = entityId;
    }

    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int bettedHorse, int spectatorBet, int entityId) {
        this.method = method.getId();
        this.spectatorBettedHorse = bettedHorse;
        this.spectatorBet = spectatorBet;
        this.entityId = entityId;
    }

    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int raceId, double[] horsesOdd, int entityId) {
        this.method = method.getId();
        this.raceId = raceId;
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

    public int getRaceId() {
        return raceId;
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
}
