package stubs;

import communication.ClientCom;
import entities.Horse;
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

    public void entertainTheGuests() {
        StableMessage inMessage = exchange(new StableMessage(
                StableMessageTypes.ENTERTAIN_THE_GUESTS, 0));

        if (inMessage.getMethod() == StableMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    StableMessageTypes.ENTERTAIN_THE_GUESTS);
            System.exit(1);
        }
    }

    public double[] getRaceOdds(int raceID) {
        double[] raceOdds;
        StableMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");

        inMessage = exchange(new StableMessage(
                StableMessageTypes.GET_RACE_ODDS, raceID, 0));

        if (inMessage.getMethod() == StableMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    StableMessageTypes.GET_RACE_ODDS);
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

    public void proceedToStable() {
        Horse h;
        StableMessage inMessage;

        h = (Horse) Thread.currentThread();
        inMessage = exchange(new StableMessage(
                StableMessageTypes.PROCEED_TO_STABLE, 0));

        if (inMessage.getMethod() == StableMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    StableMessageTypes.PROCEED_TO_STABLE);
            System.exit(1);
        }

        h.setHorseState(HorseState.AT_THE_STABLE);
    }

    public void summonHorsesToPaddock(int raceID) {
        StableMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");

        inMessage = exchange(new StableMessage(
                StableMessageTypes.SUMMON_HORSES_TO_PADDOCK, raceID, 0));

        if (inMessage.getMethod() == StableMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    StableMessageTypes.SUMMON_HORSES_TO_PADDOCK);
            System.exit(1);
        }
    }

}
