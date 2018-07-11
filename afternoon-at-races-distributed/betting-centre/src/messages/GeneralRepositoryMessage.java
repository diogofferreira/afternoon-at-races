package messages;

import messageTypes.GeneralRepositoryMessageTypes;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This data type defines a message sent to and from the General Repository server.
 * It contains a variety of fields and constructors to adapt to the different
 * entities using it.
 */
public class GeneralRepositoryMessage implements Serializable {

    /**
     * Serial version of the message.
     */
    private static final long serialVersionUID = 1003L;

    // method type
    /**
     * Identifier of the method that the entity pretends to invoke in the shared
     * region server.
     */
    private int method;

    // arguments
    /**
     * Current Broker's state.
     */
    private int brokerState;

    /**
     * Current Spectator's state.
     */
    private int spectatorState;

    /**
     * Current Horse's state.
     */
    private int horseState;

    /**
     * Number of the race.
     */
    private int raceNumber;

    /**
     * Horse's index in the race.
     */
    private int horseIdx;

    /**
     * Horse's agility value.
     */
    private int horseAgility;

    /**
     * Horse final race position.
     */
    private int horsePosition;

    /**
     * Current Horses' position in the track.
     */
    private int horseStep;

    /**
     * Value of the Spectators' current bets.
     */
    private int spectatorBet;

    /**
     * RaceIdx of the Horse the Spectator bet on the current race.
     */
    private int spectatorBettedHorse;

    /**
     * Spectator's winning value.
     */
    private int spectatorGains;

    /**
     * Array that indicates if the Horse/Jockey pair has already crossed the
     * finish line (0 - false / 1 - true).
     */
    private int[] standings;

    /**
     * Horses' odds of winning.
     */
    private double[] horsesOdd;

    // entity id
    /**
     * Identifier of the entity that is sending the message.
     */
    private int entityId;

    /**
     * Textual representation of an error message;
     */
    private String errorMessage;

    /**
     * Constructor (for error messages).
     * @param inMessage Message which contains an error.
     * @param errorMessage Textual representation of the error.
     */
    public GeneralRepositoryMessage(GeneralRepositoryMessage inMessage,
                                    String errorMessage) {
        if (inMessage == null)
            throw new IllegalArgumentException("Invalid inMessage");
        if (errorMessage == null)
            throw new IllegalArgumentException("Invalid error description");

        this.method = GeneralRepositoryMessageTypes.ERROR.getId();
        this.errorMessage = errorMessage;
        this.brokerState = inMessage.getBrokerState();
        this.spectatorState = inMessage.getSpectatorState();
        this.horseState = inMessage.getHorseState();
        this.raceNumber = inMessage.getRaceNumber();
        this.horseIdx = inMessage.getHorseIdx();
        this.horseAgility = inMessage.getHorseAgility();
        this.horsePosition = inMessage.getHorsePosition();
        this.horseStep = inMessage.getHorseStep();
        this.spectatorBet = inMessage.getSpectatorBet();
        this.spectatorBettedHorse = inMessage.getSpectatorBettedHorse();
        this.spectatorGains = inMessage.getSpectatorGains();
        this.standings = inMessage.getStandings();
        this.horsesOdd = inMessage.getHorsesOdd();
        this.entityId = inMessage.getEntityId();
    }

    /**
     * Constructor (type 1).
     * @param method Method an entity invokes on the shared region server.
     * @param entityId Id of the entity sending the message.
     */
    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int entityId) {
        this.method = method.getId();
        this.entityId = entityId;
    }

    /**
     * Constructor (type 2).
     * @param method Method an entity invokes on the shared region server.
     * @param stateOrAmountOrGainsOrRaceNumber Field that represents either
     *                                         the entity state, the Spectators'
     *                                         bet on the current race, the Spectators'
     *                                         winning value or the current race
     *                                         number.
     * @param entityId Id of the entity sending the message.
     */
    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int stateOrAmountOrGainsOrRaceNumber, int entityId) {
        this.method = method.getId();
        switch (method) {
            case SET_BROKER_STATE:
                this.brokerState = stateOrAmountOrGainsOrRaceNumber;
                break;
            case SET_SPECTATOR_STATE:
                this.spectatorState = stateOrAmountOrGainsOrRaceNumber;
                break;
            case SET_SPECTATORS_BET:
                this.spectatorBet = stateOrAmountOrGainsOrRaceNumber;
                break;
            case SET_SPECTATOR_GAINS:
                this.spectatorGains = stateOrAmountOrGainsOrRaceNumber;
                break;
            case INIT_RACE:
                this.raceNumber = stateOrAmountOrGainsOrRaceNumber;
                break;
        }
        this.entityId = entityId;
    }


    /**
     * Constructor (type 3).
     * @param method Method an entity invokes on the shared region server.
     * @param raceId Field that represents the current race id.
     * @param horseIdx Horse's index in the race.
     * @param stateOrAgility Field that represents either the current entity
     *                             state or the Horses' agility value.
     * @param entityId Id of the entity sending the message.
     */
    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int raceId, int horseIdx,
                                    int stateOrAgility, int entityId) {
        this.method = method.getId();
        this.horseIdx = horseIdx;
        if (method == GeneralRepositoryMessageTypes.SET_HORSE_STATE)
            this.horseState = stateOrAgility;
        else
            this.horseAgility = stateOrAgility;

        this.raceNumber = raceId;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 4).
     * @param method Method an entity invokes on the shared region server.
     * @param raceId Field that represents the current race id.
     * @param horsePosition Field that represents the final Horses' position in the race.
     * @param horseIdx Horse's index in the race.
     * @param horseStep Field that represents the Horses' position in the track.
     * @param entityId Id of the entity sending the message.
     */
    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int raceId, int horsePosition, int horseIdx,
                                    int horseStep, int entityId) {
        this.method = method.getId();
        this.raceNumber = raceId;
        this.horsePosition = horsePosition;
        this.horseIdx = horseIdx;
        this.horseStep = horseStep;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 5).
     * @param method Method an entity invokes on the shared region server.
     * @param spectatorBet Value of the Spectators' current bet.
     * @param bettedHorse RaceIdx of the Horse the Spectator bet on the current race.
     * @param entityId Id of the entity sending the message.
     */
    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int spectatorBet, int bettedHorse, int entityId) {
        this.method = method.getId();
        this.spectatorBettedHorse = bettedHorse;
        this.spectatorBet = spectatorBet;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 6).
     * @param method Method an entity invokes on the shared region server.
     * @param raceId Current race id.
     * @param horsesOdd Array containing the Horse odds of winning.
     * @param entityId Id of the entity sending the message.
     */
    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int raceId, double[] horsesOdd, int entityId) {
        this.method = method.getId();
        this.raceNumber = raceId;
        this.horsesOdd = horsesOdd;
        this.entityId = entityId;
    }

    /**
     * Constructor (type 7).
     * @param method Method an entity invokes on the shared region server.
     * @param standings Array that indicates if the Horse/Jockey pair has already
     *                 crossed the finish line (0 - false / 1 - true).
     * @param entityId Id of the entity sending the message.
     */
    public GeneralRepositoryMessage(GeneralRepositoryMessageTypes method,
                                    int[] standings, int entityId) {
        this.method = method.getId();
        this.standings = standings;
        this.entityId = entityId;
    }

    /**
     * Method that returns the current Broker's state.
     * @return The current Broker's state.
     */
    public int getBrokerState() {
        return brokerState;
    }

    /**
     * Method that returns the current Spectators' state.
     * @return The current Spectator's state.
     */
    public int getSpectatorState() {
        return spectatorState;
    }

    /**
     * Method that returns the current Horses' state.
     * @return The current Horses' state.
     */
    public int getHorseState() {
        return horseState;
    }

    /**
     * Method that returns an integer identifier of the method invoked on the
     * shared region.
     * @return The identifier of the method invoked on the shared region.
     */
    public int getMethod() {
        return method;
    }

    /**
     * Method that returns an integer identifier of the method invoked on the
     * shared region.
     * @param method The method invoked on the shared region.
     */
    public void setMethod(GeneralRepositoryMessageTypes method) {
        this.method = method.getId();
    }

    /**
     * Method that returns the race number which the current
     * message corresponds.
     * @return The race number which the current message corresponds.
     */
    public int getRaceNumber() {
        return raceNumber;
    }

    /**
     * Method that returns the Horses' index in the race which the current
     * message corresponds.
     * @return The Horses' index in the race which the current message corresponds.
     */
    public int getHorseIdx() {
        return horseIdx;
    }

    /**
     * Method that returns the Horses' agility value in the race which the current
     * message corresponds.
     * @return The Horses' agility value in the race which the current message
     * corresponds.
     */
    public int getHorseAgility() {
        return horseAgility;
    }

    /**
     * Method that returns the Horses' final position in the race which the current
     * message corresponds.
     * @return The Horses' final position in the race which the current message
     * corresponds.
     */
    public int getHorsePosition() {
        return horsePosition;
    }

    /**
     * Method that returns the Horses' current position in the track which the current
     * message corresponds.
     * @return The Horses' current position in the track which the current message
     * corresponds.
     */
    public int getHorseStep() {
        return horseStep;
    }

    /**
     * Method that returns the Spectators' betted value.
     * @return The Spectators' betted value.
     */
    public int getSpectatorBet() {
        return spectatorBet;
    }

    /**
     * Method that returns the RaceIdx of the Horse the Spectator bet on the
     * race which the current message corresponds.
     * @return The RaceIdx of the Horse the Spectator bet on the
     * race which the current message corresponds.
     */
    public int getSpectatorBettedHorse() {
        return spectatorBettedHorse;
    }

    /**
     * Method that returns the Spectators' winning value.
     * @return The Spectators' winning value.
     */
    public int getSpectatorGains() {
        return spectatorGains;
    }

    /**
     * Method that returns the array that indicates if the Horse/Jockey pair
     * has already crossed the finish line (0 - false / 1 - true).
     * @return The array that indicates if the Horse/Jockey pair
     * has already crossed the finish line (0 - false / 1 - true).
     */
    public int[] getStandings() {
        return standings;
    }

    /**
     * Method that returns the Horses' odds of winning.
     * @return The Horses' odds of winning.
     */
    public double[] getHorsesOdd() {
        return horsesOdd;
    }

    /**
     * Method that returns the identifier of the entity that intends to invoke
     * a method in the shared region.
     * @return The identifier of the entity that intends to invoke a method
     * in the shared region.
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * Method that returns the textual representation of an error message.
     * @return The textual representation of an error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Method that sets the textual representation of an error message.
     * @param errorMessage  The textual representation of an error message.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Method that returns a textual representation of the message.
     * @return A textual representation of the message.
     */
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
                ", spectatorGains=" + spectatorGains +
                ", standings=" + Arrays.toString(standings) +
                ", horsesOdd=" + Arrays.toString(horsesOdd) +
                ", entityId=" + entityId +
                '}';
    }
}
