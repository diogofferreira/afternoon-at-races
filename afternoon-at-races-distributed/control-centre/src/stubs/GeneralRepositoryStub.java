package stubs;


import communication.ClientCom;
import entities.Broker;
import main.EventVariables;
import messageTypes.ControlCentreMessageTypes;
import messageTypes.GeneralRepositoryMessageTypes;
import messages.ControlCentreMessage;
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

    public void initRace(int raceNumber) {
        GeneralRepositoryMessage inMessage;

        if (raceNumber < 0 || raceNumber > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.INIT_RACE));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    GeneralRepositoryMessageTypes.INIT_RACE);
            System.exit(1);
        }
    }

    public void setBrokerState(BrokerState brokerState) {
        GeneralRepositoryMessage inMessage;

        if (brokerState == null)
            throw new IllegalArgumentException("Invalid broker state");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_BROKER_STATE,
                brokerState.getId(), 0));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_BROKER_STATE);
            System.exit(1);
        }
    }

    public void setHorseAgility(int raceID, int horseIdx, int horseAgility) {
        GeneralRepositoryMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");
        if (horseIdx < 0 || horseIdx > EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse idx");
        if (horseAgility < 1 || horseAgility > EventVariables.HORSE_MAX_STEP)
            throw new IllegalArgumentException("Invalid horse agility");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSE_AGILITY, raceID,
                horseIdx, horseAgility, horseIdx));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_HORSE_AGILITY);
            System.exit(1);
        }
    }

    public void setHorsePosition(int horseIdx, int horsePosition, int horseStep) {
        GeneralRepositoryMessage inMessage;

        if (horseIdx < 0 || horseIdx > EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse idx");
        if (horsePosition < 0)
            throw new IllegalArgumentException("Invalid horse position");
        if (horseStep < 0)
            throw new IllegalArgumentException("Invalid horse step");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSE_POSITION, horseIdx,
                horsePosition, horseStep, horseIdx));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_HORSE_POSITION);
            System.exit(1);
        }
    }

    public void setHorseOdd(int raceID, double[] horsesOdd) {
        GeneralRepositoryMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");
        if (horsesOdd == null ||
                horsesOdd.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse odds");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSES_ODD, raceID,
                horsesOdd, 0));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_HORSES_ODD);
            System.exit(1);
        }
    }

    public void setHorsesStanding(int[] standings) {
        GeneralRepositoryMessage inMessage;

        if (standings == null ||
                standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse standings");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSES_STANDING, standings, 0));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_HORSES_STANDING);
            System.exit(1);
        }
    }

    public void setHorseState(int raceID, int horseIdx, HorseState horseState) {
        GeneralRepositoryMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");
        if (horseIdx < 0 || horseIdx > EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse idx");
        if (horseState == null)
            throw new IllegalArgumentException("Invalid horse state");

        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSE_STATE, raceID,
                horseIdx, horseState.getId(), horseIdx));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_HORSE_STATE);
            System.exit(1);
        }
    }

    public void setSpectatorsGains(int spectatorID, int amount) {
        GeneralRepositoryMessage inMessage;

        if (spectatorID < 0 || spectatorID > EventVariables.NUMBER_OF_SPECTATORS)
            throw new IllegalArgumentException("Invalid spectator ID");

        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_SPECTATOR_GAINS, amount,
                spectatorID));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_SPECTATOR_GAINS);
            System.exit(1);
        }
    }

    public void setSpectatorsBet(int spectatorID, int spectatorBet,
                                int spectatorBettedHorse) {
        GeneralRepositoryMessage inMessage;

        if (spectatorID < 0 || spectatorID > EventVariables.NUMBER_OF_SPECTATORS)
            throw new IllegalArgumentException("Invalid spectator ID");
        if (spectatorBet < 0)
            throw new IllegalArgumentException("Invalid spectator bet amount");
        if (spectatorBettedHorse < 0 ||
                spectatorBettedHorse > EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse idx");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_SPECTATORS_BET, spectatorID,
                spectatorBet, spectatorBettedHorse, 0));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_SPECTATORS_BET);
            System.exit(1);
        }
    }

    public void setSpectatorState(int spectatorID, SpectatorState spectatorState) {
        GeneralRepositoryMessage inMessage;

        if (spectatorID < 0 || spectatorID > EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid spectator ID");
        if (spectatorState == null)
            throw new IllegalArgumentException("Invalid spectator state");
        
        inMessage = exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_SPECTATOR_STATE, spectatorID,
                spectatorState.getId(), spectatorID));

        if (inMessage.getMethod() == GeneralRepositoryMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    GeneralRepositoryMessageTypes.SET_SPECTATOR_STATE);
            System.exit(1);
        }
    }
}
