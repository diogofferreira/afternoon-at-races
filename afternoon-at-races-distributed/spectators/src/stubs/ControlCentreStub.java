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

    public void celebrate() {
        BrokerInt b;
        ControlCentreMessage inMessage;

        b = (BrokerInt)Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.CELEBRATE, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.CELEBRATE);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);
    }

    public void finishTheRace(int[] standings) {
        ControlCentreMessage inMessage;

        if (standings == null ||
                standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid standings");

        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.FINISH_THE_RACE, standings, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.FINISH_THE_RACE);
            System.exit(1);
        }
    }

    public void goCheckHorses() {
        ControlCentreMessage inMessage;

        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.GO_CHECK_HORSES, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.GO_CHECK_HORSES);
            System.exit(1);
        }
    }

    public void goWatchTheRace() {
        SpectatorInt s;
        ControlCentreMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.GO_WATCH_THE_RACE, s.getID()));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.GO_WATCH_THE_RACE);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.WATCHING_A_RACE);
    }

    public boolean haveIWon(int horseIdx) {
        SpectatorInt s;
        ControlCentreMessage inMessage;

        if (horseIdx < 0 || horseIdx > EventVariables.NUMBER_OF_HORSES_PER_RACE)
            throw new IllegalArgumentException("Invalid horse idx");

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.HAVE_I_WON, horseIdx, s.getID()));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.HAVE_I_WON);
            System.exit(1);
        }

        return inMessage.isHaveIWon();
    }

    public void proceedToPaddock() {
        HorseInt h;
        ControlCentreMessage inMessage;

        h = (HorseInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.PROCEED_TO_PADDOCK, h.getRaceIdx()));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.PROCEED_TO_PADDOCK);
            System.exit(1);
        }
    }

    public void relaxABit() {
        SpectatorInt s;
        ControlCentreMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.RELAX_A_BIT, s.getID()));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.RELAX_A_BIT);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.CELEBRATING);
    }

    public int[] reportResults() {
        int[] results;
        ControlCentreMessage inMessage;

        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.REPORT_RESULTS, 0));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.REPORT_RESULTS);
            System.exit(1);
        }

        results = inMessage.getWinners();

        if (results == null || results.length == 0) {
            System.out.println(Thread.currentThread().getName() +
                    " - Invalid results array - " +
                    ControlCentreMessageTypes.REPORT_RESULTS);
            System.exit(1);
        }

        return results;
    }

    public void startTheRace() {
        ControlCentreMessage inMessage;

        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.START_THE_RACE));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.START_THE_RACE);
            System.exit(1);
        }
    }

    public void summonHorsesToPaddock(int raceID) {
        BrokerInt b;
        ControlCentreMessage inMessage;

        if (raceID < 0 || raceID > EventVariables.NUMBER_OF_RACES)
            throw new IllegalArgumentException("Invalid race ID");

        b = (BrokerInt)Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK, raceID));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);
    }

    public boolean waitForNextRace() {
        SpectatorInt s;
        ControlCentreMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE));

        if (inMessage.getMethod() == ControlCentreMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.WAITING_FOR_A_RACE_TO_START);

        return inMessage.isThereARace();
    }
}
