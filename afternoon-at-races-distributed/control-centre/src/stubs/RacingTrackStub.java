package stubs;

import communication.ClientCom;
import entities.Broker;
import entities.Horse;
import messageTypes.RacingTrackMessageTypes;
import messages.RacingTrackMessage;
import states.BrokerState;
import states.HorseState;

/**
 * This data type defines the communication stub of Racing Track.
 */
public class RacingTrackStub {
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
    public RacingTrackStub(String hostName, int port) {
        serverHostName = hostName;
        serverPortNumb = port;
    }

    /**
     * Message exchanged with Racing Track server.
     */
    private RacingTrackMessage exchange(RacingTrackMessage outMessage) {
        ClientCom com = new ClientCom(serverHostName, serverPortNumb);
        RacingTrackMessage inMessage;

        while (!com.open()) {
            try {
                Thread.currentThread().sleep((long)10);
            } catch (InterruptedException e) {
            }
        }

        com.writeObject(outMessage);
        inMessage = (RacingTrackMessage)com.readObject();
        com.close();

        return inMessage;
    }

    public boolean hasFinishLineBeenCrossed() {
        Horse h;
        RacingTrackMessage inMessage;

        h = (Horse) Thread.currentThread();
        inMessage = exchange(new RacingTrackMessage(
                RacingTrackMessageTypes.HAS_FINISH_LINE_BEEN_CROSSED,
                h.getRaceID(), h.getRaceIdx(), h.getAgility(),
                h.getCurrentPosition(), h.getCurrentStep(), h.getID()));

        if (inMessage.getMethod() == RacingTrackMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    RacingTrackMessageTypes.HAS_FINISH_LINE_BEEN_CROSSED);
            System.exit(1);
        }

        if (inMessage.hasFinishLineBeenCrossed())
            h.setHorseState(HorseState.AT_THE_FINISH_LINE);

        return inMessage.hasFinishLineBeenCrossed();
    }

    public void makeAMove(int step) {
        Horse h;
        RacingTrackMessage inMessage;

        h = (Horse) Thread.currentThread();

        if (step < 1 || step > h.getAgility())
            throw new IllegalArgumentException("Invalid horse step");

        inMessage = exchange(new RacingTrackMessage(
                RacingTrackMessageTypes.MAKE_A_MOVE, step, h.getRaceID(),
                h.getRaceIdx(), h.getAgility(), h.getCurrentPosition(),
                h.getCurrentStep(), h.getID()));

        if (inMessage.getMethod() == RacingTrackMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    RacingTrackMessageTypes.MAKE_A_MOVE);
            System.exit(1);
        }

        h.setHorseState(HorseState.RUNNING);
        h.updateCurrentPosition(step);
    }

    public void proceedToStartLine() {
        Horse h;
        RacingTrackMessage inMessage;

        h = (Horse) Thread.currentThread();
        inMessage = exchange(new RacingTrackMessage(
                RacingTrackMessageTypes.PROCEED_TO_START_LINE,
                h.getRaceID(), h.getRaceIdx(), h.getAgility(),
                h.getCurrentPosition(), h.getCurrentStep(), h.getID()));

        if (inMessage.getMethod() == RacingTrackMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    RacingTrackMessageTypes.PROCEED_TO_START_LINE);
            System.exit(1);
        }

        h.setHorseState(HorseState.AT_THE_STARTING_LINE);
    }

    public void startTheRace() {
        Broker b;
        RacingTrackMessage inMessage;

        b = (Broker) Thread.currentThread();
        inMessage = exchange(new RacingTrackMessage(
                RacingTrackMessageTypes.START_THE_RACE, 0));

        if (inMessage.getMethod() == RacingTrackMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    RacingTrackMessageTypes.START_THE_RACE);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);
    }
}
