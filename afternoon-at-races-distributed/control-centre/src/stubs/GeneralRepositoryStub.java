package stubs;


import communication.ClientCom;
import entities.HorseInt;
import main.EventVariables;
import messageTypes.GeneralRepositoryMessageTypes;
import messages.GeneralRepositoryMessage;
import states.BrokerState;
import states.HorseState;
import states.SpectatorState;

/**
 * This data type defines the communication stub of General Repository.
 */
public class GeneralRepositoryStub {
    /**
     * Host name of the computational system where the server is located.
     */
    private String serverHostName;

    /**
     * Port number where the server is listening.
     */
    private int serverPortNumb;

    /**
     * Instantiation of the stub.
     *
     * @param hostName host name of the computational system where the server
     *                 is located.
     * @param port Port number where the server is listening.
     */
    public GeneralRepositoryStub(String hostName, int port) {
        serverHostName = hostName;
        serverPortNumb = port;
    }

    /**
     * Message exchanged with Control Centre server.
     */
    private GeneralRepositoryMessage exchange(GeneralRepositoryMessage outMessage) {
        ClientCom com = new ClientCom(serverHostName, serverPortNumb);
        GeneralRepositoryMessage inMessage;

        while (!com.open()) {
            try {
                Thread.currentThread().sleep((long)10);
            } catch (InterruptedException e) {
            }
        }

        com.writeObject(outMessage);
        inMessage = (GeneralRepositoryMessage)com.readObject();
        com.close();

        return inMessage;
    }

    /**
     * Method that resets all the race related variables, such as the
     * raceNumber (ID), the spectators bets, the horses odds and
     * travelled distances.
     * @param raceNumber The updated race number.
     */
    public void initRace(int raceNumber) {
        GeneralRepositoryMessage inMessage;

        if (raceNumber < 0 || raceNumber >= EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.INIT_RACE, raceNumber, 0));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    GeneralRepositoryMessageTypes.INIT_RACE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method that updates the Broker state.
     * @param brokerState The new Broker state.
     */
    public void setBrokerState(BrokerState brokerState) {
        GeneralRepositoryMessage inMessage;

        if (brokerState == null)
            throw new IllegalArgumentException("Invalid broker state");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_BROKER_STATE,
                brokerState.getId(), 0));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_BROKER_STATE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method that sets an Horse agility, i.e., the maximum step per iteration
     * it can takes.
     * @param raceID The raceID where the Horse will participate.
     * @param horseIdx The raceIdx of the reference Horse.
     * @param horseAgility The agility of the referenced Horse.
     */
    public void setHorseAgility(int raceID, int horseIdx, int horseAgility) {
        GeneralRepositoryMessage inMessage;

        if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");
        if (horseIdx < 0 || horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse idx");
        if (horseAgility < 1 || horseAgility > EventVariables.HORSE_MAX_STEP)
            throw new IllegalArgumentException("Invalid horse agility");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSE_AGILITY, raceID,
                horseIdx, horseAgility, ((HorseInt)Thread.currentThread()).getID()));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_HORSE_AGILITY + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Updates the Horse/Jockey pair current position (i.e., the travelled
     * distance).
     * @param horseIdx The raceIdx of the Horse whose position is being updated.
     * @param horsePosition The new Horse position.
     * @param horseStep The number of steps the Horse has already taken.
     */
    public void setHorsePosition(int horseIdx, int horsePosition, int horseStep) {
        GeneralRepositoryMessage inMessage;

        if (horseIdx < 0 || horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse idx");
        if (horsePosition < 0)
            throw new IllegalArgumentException("Invalid horse position");
        if (horseStep < 0)
            throw new IllegalArgumentException("Invalid horse step");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSE_POSITION, horsePosition,
                horseIdx, horseStep, ((HorseInt)Thread.currentThread()).getID()));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_HORSE_POSITION + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method that sets the odds of the Horses running on the current race.
     * @param raceID The ID of the race where these odds are applied.
     * @param horsesOdd Array of horses odds, indexed by their raceIdx.
     */
    public void setHorsesOdd(int raceID, double[] horsesOdd) {
        GeneralRepositoryMessage inMessage;

        if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");
        if (horsesOdd == null ||
                horsesOdd.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse odds");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSES_ODD, raceID,
                horsesOdd, 0));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_HORSES_ODD + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method that signals the horses' position in the race.
     * @param standings horses' position in the race.
     */
    public void setHorsesStanding(int[] standings) {
        GeneralRepositoryMessage inMessage;

        if (standings == null ||
                standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse standings");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSES_STANDING,
                standings, 0));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_HORSES_STANDING + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method that sets the reference Horse/Jockey pair state.
     * @param raceID The ID of the race which the pair will run.
     * @param horseIdx The raceIdx of the Horse whose state will be updated.
     * @param horseState The next Horse state.
     */
    public void setHorseState(int raceID, int horseIdx, HorseState horseState) {
        GeneralRepositoryMessage inMessage;

        if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");
        if (horseIdx < 0 || horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse idx");
        if (horseState == null)
            throw new IllegalArgumentException("Invalid horse state");

        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSE_STATE, raceID,
                horseIdx, horseState.getId(),
                ((HorseInt)Thread.currentThread()).getID()));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_HORSE_STATE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method that updates the referenced Spectator's wallet by adding the amount
     * the amount passed as argument.
     * @param spectatorID The ID of the Spectator whose wallet will be updated.
     * @param amount The amount of money to add to the wallet.
     */
    public void setSpectatorsGains(int spectatorID, int amount) {
        GeneralRepositoryMessage inMessage;

        if (spectatorID < 0 || spectatorID >= EventVariables.NUMBER_OF_SPECTATORS)
            throw new IllegalArgumentException("Invalid spectator ID");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_SPECTATOR_GAINS, amount,
                spectatorID));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_SPECTATOR_GAINS + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method that sets the bet of the referenced Spectator on the current race.
     * @param spectatorID The ID of the Spectator placing the bet.
     * @param spectatorBet The value of the bet placed.
     * @param spectatorBettedHorse The raceIdx of the Horse the Spectator bet on.
     */
    public void setSpectatorsBet(int spectatorID, int spectatorBet,
                                int spectatorBettedHorse) {
        GeneralRepositoryMessage inMessage;

        if (spectatorID < 0 || spectatorID >= EventVariables.NUMBER_OF_SPECTATORS)
            throw new IllegalArgumentException("Invalid spectator ID");
        if (spectatorBet < 0)
            throw new IllegalArgumentException("Invalid spectator bet amount");
        if (spectatorBettedHorse < 0 ||
                spectatorBettedHorse >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse idx");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_SPECTATORS_BET,
                spectatorBet, spectatorBettedHorse, spectatorID));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_SPECTATORS_BET + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method that updates the state of a Spectator.
     * @param spectatorID The ID of the Spectator whose the state is updated.
     * @param spectatorState The new state of the referenced Spectator.
     */
    public void setSpectatorState(int spectatorID, SpectatorState spectatorState) {
        GeneralRepositoryMessage inMessage;

        if (spectatorID < 0 || spectatorID >= EventVariables.NUMBER_OF_SPECTATORS)
            throw new IllegalArgumentException("Invalid spectator ID");
        if (spectatorState == null)
            throw new IllegalArgumentException("Invalid spectator state");

        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_SPECTATOR_STATE,
                spectatorState.getId(), spectatorID));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_SPECTATOR_STATE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }
}
