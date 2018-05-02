package stubs;


import communication.ClientCom;
import entities.BrokerInt;
import entities.HorseInt;
import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.ControlCentreMessageTypes;
import messages.ControlCentreMessage;
import states.BrokerState;
import states.SpectatorState;

/**
 * This data type defines the communication stub of Control Centre.
 */
public class ControlCentreStub {
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
    public ControlCentreStub(String hostName, int port) {
        serverHostName = hostName;
        serverPortNumb = port;
    }

    /**
     * Message exchanged with Control Centre server.
     */
    private ControlCentreMessage exchange(ControlCentreMessage outMessage) {
        ClientCom com = new ClientCom(serverHostName, serverPortNumb);
        ControlCentreMessage inMessage;

        while (!com.open()) {
            try {
                Thread.currentThread().sleep((long)10);
            } catch (InterruptedException e) {
            }
        }

        com.writeObject(outMessage);
        inMessage = (ControlCentreMessage)com.readObject();
        com.close();

        return inMessage;
    }

    /**
     * Method invoked by the Broker in order to signal the spectators that the
     * event has ended.
     * Meanwhile, Broker also sets its state to PLAYING_HOST_AT_THE_BAR.
     */
    public void celebrate() {
        BrokerInt b;
        ControlCentreMessage inMessage;

        b = (BrokerInt)Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.CELEBRATE, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.CELEBRATE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);
    }

    /**
     * Method invoked by the last Horse/Jockey pair to cross the finish line.
     * The Broker will be notified to wake up and to report the results.
     * @param standings An array of standings of the Horses that in the race.
     */
    public void finishTheRace(int[] standings) {
        ControlCentreMessage inMessage;

        if (standings == null ||
                standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid standings");

        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.FINISH_THE_RACE, standings, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.FINISH_THE_RACE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method invoked by the last Horse/Jockey pair arriving to Paddock in order
     * to wake up the Broker.
     */
    public void goCheckHorses() {
        ControlCentreMessage inMessage;

        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.GO_CHECK_HORSES, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.GO_CHECK_HORSES + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method invoked by each Spectator before the start of each race.
     * They will block in WATCHING_A_RACE state until the Broker reports the
     * results of the race.
     */
    public void goWatchTheRace() {
        SpectatorInt s;
        ControlCentreMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.GO_WATCH_THE_RACE, s.getID()));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.GO_WATCH_THE_RACE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.WATCHING_A_RACE);
    }

    /**
     * Method invoked by each Spectator to verify if they betted on a winning
     * horse.
     * @param horseIdx The raceIdx of the horse they bet on.
     * @return A boolean indicating if the Spectator invoking the method won
     * his/her bet.
     */
    public boolean haveIWon(int horseIdx) {
        SpectatorInt s;
        ControlCentreMessage inMessage;

        if (horseIdx < 0 || horseIdx > EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse idx");

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.HAVE_I_WON, horseIdx, s.getID()));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.HAVE_I_WON + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        return inMessage.isHaveIWon();
    }

    /**
     * Method invoked by the Broker in order to start the event. It just simply
     * updates the Broker state and updates the General Repository.
     */
    public void openTheEvent() {
        BrokerInt b;
        ControlCentreMessage inMessage;

        b = (BrokerInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.OPEN_THE_EVENT, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.OPEN_THE_EVENT + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.OPENING_THE_EVENT);
    }

    /**
     * Method invoked by the last Horse/Jockey pair of the current race to arrive
     * to the Paddock, thus waking up all the Spectators to proceed to Paddock
     * and appraise the horses.
     */
    public void proceedToPaddock() {
        HorseInt h;
        ControlCentreMessage inMessage;

        h = (HorseInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.PROCEED_TO_PADDOCK, h.getRaceIdx()));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.PROCEED_TO_PADDOCK + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Last method invoked by the Spectators, changing their state to CELEBRATING.
     */
    public void relaxABit() {
        SpectatorInt s;
        ControlCentreMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.RELAX_A_BIT, s.getID()));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.RELAX_A_BIT + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.CELEBRATING);
    }

    /**
     * Method invoked by the Broker signalling all Spectators that the results
     * of the race have been reported.
     * @return An array of Horses' raceIdx that won the race.
     */
    public int[] reportResults() {
        int[] results;
        ControlCentreMessage inMessage;

        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.REPORT_RESULTS, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.REPORT_RESULTS + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        results = inMessage.getWinners();

        if (results == null || results.length == 0 ||
                results.length > EventVariables.NUMBER_OF_HORSES_PER_RACE) {
            System.out.println(Thread.currentThread().getName() +
                    " - Invalid results array - " +
                    ControlCentreMessageTypes.REPORT_RESULTS);
            System.exit(1);
        }

        return results;
    }

    /**
     * Method invoked by the Broker.
     * He'll wait here until the last Horse/Jockey pair to cross the finish line
     * wakes him up.
     */
    public void startTheRace() {
        ControlCentreMessage inMessage;

        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.START_THE_RACE, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.START_THE_RACE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }
    }

    /**
     * Method invoked by Broker, signaling the start of the event.
     * The Broker updates the current raceID and sets his state to
     * ANNOUNCING_NEXT_RACE, while signalling the Horses to proceed to Paddock.
     * @param raceID The ID of the race that will take place.
     */
    public void summonHorsesToPaddock(int raceID) {
        BrokerInt b;
        ControlCentreMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");

        b = (BrokerInt)Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK, raceID, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);
    }

    /**
     * This method is invoked by every Spectator while they're waiting for
     * a race to start.
     * While waiting here, they update their state to WAITING_FOR_A_RACE_TO_START.
     * @return True if there's still a race next.
     */
    public boolean waitForNextRace() {
        SpectatorInt s;
        ControlCentreMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE, s.getID()));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.WAITING_FOR_A_RACE_TO_START);

        return inMessage.isThereARace();
    }
}
