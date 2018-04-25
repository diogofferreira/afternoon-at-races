package stubs;

import communication.ClientCom;
import entities.BrokerInt;
import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.BettingCentreMessageTypes;
import messages.BettingCentreMessage;
import states.BrokerState;
import states.SpectatorState;

/**
 * This data type defines the communication stub of Betting Centre.
 */
public class BettingCentreStub {
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
    public BettingCentreStub(String hostName, int port) {
        serverHostName = hostName;
        serverPortNumb = port;
    }

    /**
     * Message exchanged with Betting Centre server.
     */
    private BettingCentreMessage exchange(BettingCentreMessage outMessage) {
        ClientCom com = new ClientCom(serverHostName, serverPortNumb);
        BettingCentreMessage inMessage;

        while (!com.open()) {
            try {
                Thread.currentThread().sleep((long)10);
            } catch (InterruptedException e) {
            }
        }

        com.writeObject(outMessage);
        inMessage = (BettingCentreMessage)com.readObject();
        com.close();

        return inMessage;
    }

    public void acceptTheBets(int raceID) {
        BrokerInt b;
        BettingCentreMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");

        b = (BrokerInt)Thread.currentThread();
        inMessage = exchange(new BettingCentreMessage(
                BettingCentreMessageTypes.ACCEPT_THE_BETS, raceID, 0));

        if (inMessage.getMethod() == BettingCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    BettingCentreMessageTypes.ACCEPT_THE_BETS);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.WAITING_FOR_BETS);
    }

    public boolean areThereAnyWinners(int[] standings) {
        BettingCentreMessage inMessage;

        if (standings == null || standings.length == 0 ||
                standings.length > EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid standings");

        inMessage = exchange(new BettingCentreMessage(
                BettingCentreMessageTypes.ARE_THERE_ANY_WINNERS, standings, 0));

        if (inMessage.getMethod() == BettingCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    BettingCentreMessageTypes.ARE_THERE_ANY_WINNERS);
            System.exit(1);
        }

        return inMessage.isAreThereAnyWinners();
    }

    public double goCollectTheGains() {
        SpectatorInt s;
        BettingCentreMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new BettingCentreMessage(
                BettingCentreMessageTypes.GO_COLLECT_THE_GAINS, s.getID()));

        if (inMessage.getMethod() == BettingCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    BettingCentreMessageTypes.GO_COLLECT_THE_GAINS);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.COLLECTING_THE_GAINS);
        return inMessage.getWinningValue();
    }

    public void honourTheBets() {
        BrokerInt b;
        BettingCentreMessage inMessage;

        b = (BrokerInt)Thread.currentThread();
        inMessage = exchange(new BettingCentreMessage(
                BettingCentreMessageTypes.HONOUR_THE_BETS, 0));

        if (inMessage.getMethod() == BettingCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    BettingCentreMessageTypes.HONOUR_THE_BETS);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.SETTLING_ACCOUNTS);
    }

    public int placeABet() {
        SpectatorInt s;
        BettingCentreMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new BettingCentreMessage(
                BettingCentreMessageTypes.PLACE_A_BET, false,
                s.getWallet(), s.getStrategy(), s.getID()));

        if (inMessage.getMethod() == BettingCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    BettingCentreMessageTypes.PLACE_A_BET);
            System.exit(1);
        }

        if (inMessage.getWallet() > s.getWallet()) {
            System.out.println(Thread.currentThread().getName() +
                    " - Invalid wallet value in " +
                    BettingCentreMessageTypes.PLACE_A_BET);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.PLACING_A_BET);
        s.setWallet(inMessage.getWallet());
        return inMessage.getBettedHorse();
    }

}
