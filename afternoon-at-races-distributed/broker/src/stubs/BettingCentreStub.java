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

    /**
     * Method invoked by the Broker.
     * The broker changes its state to WAITING_FOR_BETS, signals the Spectators
     * that is accepting bets.
     * It blocks each time it validates a new bet and wakes up the Spectators
     * that still have pending bets.
     * @param raceID The current raceID.
     */
    public void acceptTheBets(int raceID) {
        BrokerInt b;
        BettingCentreMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");

        b = (BrokerInt)Thread.currentThread();
        inMessage = exchange(new BettingCentreMessage(
                BettingCentreMessageTypes.ACCEPT_THE_BETS, raceID, 0));

        if (inMessage.getMethod() == BettingCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    BettingCentreMessageTypes.ACCEPT_THE_BETS + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.WAITING_FOR_BETS);
    }

    /**
     * Method invoked by the Broker to check if there are any winning bets.
     * @param standings An array of horseIdxs that contains the race winners.
     * @return True if there are any winners, false otherwise.
     */
    public boolean areThereAnyWinners(int[] standings) {
        BettingCentreMessage inMessage;

        if (standings == null || standings.length == 0 ||
                standings.length > EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid standings");

        inMessage = exchange(new BettingCentreMessage(
                BettingCentreMessageTypes.ARE_THERE_ANY_WINNERS, standings, 0));

        if (inMessage.getMethod() == BettingCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    BettingCentreMessageTypes.ARE_THERE_ANY_WINNERS + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        return inMessage.isAreThereAnyWinners();
    }

    /**
     * Method invoked by each one of the winning Spectators.
     * They change their state to COLLECTING_THE_GAINS and block in queue waiting
     * for their rewards.
     * @return The value the Spectator won.
     */
    public double goCollectTheGains() {
        SpectatorInt s;
        BettingCentreMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new BettingCentreMessage(
                BettingCentreMessageTypes.GO_COLLECT_THE_GAINS, s.getID()));

        if (inMessage.getMethod() == BettingCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    BettingCentreMessageTypes.GO_COLLECT_THE_GAINS + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.COLLECTING_THE_GAINS);
        return inMessage.getWinningValue();
    }

    /**
     * Method invoked by the Broker if there were any winning bets.
     * The Broker changes its state to SETTLING_ACCOUNTS and signals the
     * Spectators waiting for collecting their gains that it's open for settling
     * accounts.
     * He blocks each time he pays a winning bet and wakes up Spectators still
     * waiting to collect their rewards.
     */
    public void honourTheBets() {
        BrokerInt b;
        BettingCentreMessage inMessage;

        b = (BrokerInt)Thread.currentThread();
        inMessage = exchange(new BettingCentreMessage(
                BettingCentreMessageTypes.HONOUR_THE_BETS, 0));

        if (inMessage.getMethod() == BettingCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    BettingCentreMessageTypes.HONOUR_THE_BETS + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.SETTLING_ACCOUNTS);
    }

    /**
     * Method invoked by each one of the Spectators to place their bet, effectively
     * changing their state to PLACING_A_BET.
     * They blocked in queue waiting for the Broker to validate their bet.
     * If their bet is not accepted a new bet is generated.
     * @return The Horse's index on the current race that the Spectator bet on.
     */
    public int placeABet() {
        SpectatorInt s;
        BettingCentreMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new BettingCentreMessage(
                BettingCentreMessageTypes.PLACE_A_BET, false,
                s.getWallet(), s.getStrategy(), s.getID()));

        if (inMessage.getMethod() == BettingCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    BettingCentreMessageTypes.PLACE_A_BET + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
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
