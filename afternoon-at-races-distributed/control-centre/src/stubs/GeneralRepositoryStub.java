package stubs;


import communication.ClientCom;
import entities.Broker;
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
        exchange(new GeneralRepositoryMessage(GeneralRepositoryMessageTypes.INIT_RACE));
    }

    public void setBrokerState(BrokerState brokerState) {
        exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_BROKER_STATE,
                brokerState.getId(), 0));
    }

    public void setHorseAgility(int raceID, int horseIdx, int horseAgility) {
        exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSE_AGILITY, raceID,
                horseIdx, horseAgility, horseIdx));
    }

    public void setHorsePosition(int horseIdx, int horsePosition, int horseStep) {
        exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSE_POSITION, horseIdx,
                horsePosition, horseStep, horseIdx));
    }

    public void setHorseOdd(int raceID, double[] horsesOdd) {
        exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSES_ODD, raceID,
                horsesOdd, 0));
    }

    public void setHorsesStanding(int[] standings) {
        exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSES_STANDING, standings, 0));
    }

    public void setHorseState(int raceID, int horseIdx, HorseState horseState) {
        exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_HORSE_STATE, raceID,
                horseIdx, horseState.getId(), horseIdx));
    }

    public void setSpectatorsGains(int spectatorID, int amount) {
        exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_SPECTATOR_GAINS, spectatorID,
                amount, 0));
        // REVER CONSTRUTOR
    }

    public void setSpectatorsBet(int spectatorID, int spectatorBet,
                                int spectatorBettedHorse) {
        exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_SPECTATORS_BET, spectatorID,
                spectatorBet, spectatorBettedHorse, 0));
    }

    public void setSpectatorState(int spectatorID, SpectatorState spectatorState) {
        exchange(new GeneralRepositoryMessage(
                GeneralRepositoryMessageTypes.SET_SPECTATOR_STATE, spectatorID,
                spectatorState.getId(), spectatorID));
    }
}
