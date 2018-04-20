package stubs;


import communication.ClientCom;
import messageTypes.ControlCentreMessageTypes;
import entities.Broker;
import messages.ControlCentreMessage;
import states.BrokerState;

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

    public void summonHorsesToPaddock(int raceID) {
        Broker b = (Broker)Thread.currentThread();
        exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK, raceID));
        b.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);
    }

    public void startTheRace() {
        exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.START_THE_RACE));
    }

    public int[] reportResults() {
        return exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.START_THE_RACE)).getWinners();
    }

    public void celebrate() {
        Broker b = (Broker)Thread.currentThread();
        exchange(new ControlCentreMessage(
                ControlCentreMessageTypes.CELEBRATE));
        b.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);
    }
}
