package stubs;

import communication.ClientCom;
import entities.HorseInt;
import main.EventVariables;
import messageTypes.StableMessageTypes;
import messages.StableMessage;
import states.HorseState;


/**
 * This data type defines the communication stub of Stable.
 */
public class StableStub {
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
    public StableStub(String hostName, int port) {
        serverHostName = hostName;
        serverPortNumb = port;
    }

    /**
     * Message exchanged with Stable server.
     */
    private StableMessage exchange(StableMessage outMessage) {
        ClientCom com = new ClientCom(serverHostName, serverPortNumb);
        StableMessage inMessage;

        while (!com.open()) {
            try {
                Thread.currentThread().sleep((long)10);
            } catch (InterruptedException e) {
            }
        }

        com.writeObject(outMessage);
        inMessage = (StableMessage) com.readObject();
        com.close();

        return inMessage;
    }

    /**
     * Method invoked by the Broker to signal all horses to wake up, ending the
     * event.
     */
    public void entertainTheGuests() {
        StableMessage inMessage = exchange(new StableMessage(
                StableMessageTypes.ENTERTAIN_THE_GUESTS, 0));

        if (inMessage.getMethod() == StableMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    StableMessageTypes.ENTERTAIN_THE_GUESTS + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method that returns the odd of all horses running on the race with ID
     * passed as argument..
     * @param raceID The ID of the race of the odds.
     * @return Array with horses' odds.
     */
    public double[] getRaceOdds(int raceID) {
        double[] raceOdds;
        StableMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");

        inMessage = exchange(new StableMessage(
                StableMessageTypes.GET_RACE_ODDS, raceID, 0));

        if (inMessage.getMethod() == StableMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    StableMessageTypes.GET_RACE_ODDS + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        raceOdds = inMessage.getRaceOdds();
        if (raceOdds == null ||
                raceOdds.length != EventVariables.NUMBER_OF_HORSES_PER_RACE) {
            System.out.println(Thread.currentThread().getName() +
                    " - Invalid race odds array - " +
                    StableMessageTypes.GET_RACE_ODDS);
            System.exit(1);
        }

        return raceOdds;
    }

    /**
     * Method invoked by an Horse, where usually it gets blocked.
     * It sets the its state to AT_THE_STABLE.
     * It is waken up by the Broker to proceed to Paddock or when the event ends.
     */
    public void proceedToStable() {
        HorseInt h;
        StableMessage inMessage;

        h = (HorseInt) Thread.currentThread();
        inMessage = exchange(new StableMessage(
                StableMessageTypes.PROCEED_TO_STABLE, h.getAgility(), h.getID()));

        if (inMessage.getMethod() == StableMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    StableMessageTypes.PROCEED_TO_STABLE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        if (inMessage.getRaceId() < 0 ||
                inMessage.getRaceId() > EventVariables.NUMBER_OF_RACES) {
            System.out.println(Thread.currentThread().getName() +
                    " - Invalid race id in " +
                    StableMessageTypes.PROCEED_TO_STABLE);
            System.exit(1);
        }

        if (inMessage.getRaceIdx() < 0 ||
                inMessage.getRaceIdx() > EventVariables.NUMBER_OF_HORSES_PER_RACE) {
            System.out.println(Thread.currentThread().getName() +
                    " - Invalid race idx in " +
                    StableMessageTypes.PROCEED_TO_STABLE);
            System.exit(1);
        }

        h.setHorseState(HorseState.AT_THE_STABLE);
        h.setRaceID(inMessage.getRaceId());
        h.setRaceIdx(inMessage.getRaceIdx());
    }

    /**
     * Method invoked by the Broker to notify all horses of the current race
     * to proceed to Paddock.
     * @param raceID The ID of the current race; it determines which horses will
     *               be waken up.
     */
    public void summonHorsesToPaddock(int raceID) {
        StableMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");

        inMessage = exchange(new StableMessage(
                StableMessageTypes.SUMMON_HORSES_TO_PADDOCK, raceID, 0));

        if (inMessage.getMethod() == StableMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    StableMessageTypes.SUMMON_HORSES_TO_PADDOCK + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

}
