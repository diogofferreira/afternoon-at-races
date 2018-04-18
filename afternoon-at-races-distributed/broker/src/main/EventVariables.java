package main;

/**
 * Class that contains all the relevant variables to the correct functioning
 * of the simulation.
 */
public class EventVariables {
    /**
     * Number of Spectators that will watch the race and place bets.
     */
    public static final int NUMBER_OF_SPECTATORS = 4;

    /**
     * Total number of races that will take place in the event.
     */
    public static final int NUMBER_OF_RACES = 5;

    /**
     * Number of Horses that will run in a race.
     */
    public static final int NUMBER_OF_HORSES_PER_RACE = 4;

    /**
     * Total number of Horses involved in the event.
     * This number is determined by NUMBER_OF_HORSES_PER_RACE * NUMBER_OF_RACES.
     */
    public static final int NUMBER_OF_HORSES =
            NUMBER_OF_HORSES_PER_RACE * NUMBER_OF_RACES;

    /**
     * Length of the Racing Track.
     */
    public static final int RACING_TRACK_LENGTH = 25;

    /**
     * Maximum step/travelled distance per iteration each Horse can take.
     */
    public static final int HORSE_MAX_STEP = 5;

    /**
     * Total amount of money that each Spectator will have in his/her wallet.
     */
    public static final int INITIAL_WALLET = 500;

    /**
     * Path to the file where the log will be stored.
     */
    public static final String LOG_FILEPATH = "logs/AfternoonAtRaces";
}
