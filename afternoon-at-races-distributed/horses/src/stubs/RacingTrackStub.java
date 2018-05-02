package stubs;

import communication.ClientCom;
import entities.BrokerInt;
import entities.HorseInt;
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

    /**
     * Method invoked by each one of the participating Horses checking they have
     * already crossed the finish line.
     * If true, they check if they have won the race and add their ID to the
     * corresponding list of winners.
     * If it's the last horse crossing the finish line, it wakes up the Broker
     * at the Control Centre and provides the list of winners.
     * Otherwise, just wakes up the next horse that still hasn't finish the race.
     * @return Boolean indicating whether the Horse/Jockey pair that invoked the
     * method has already crossed the finish line or not.
     */
    public boolean hasFinishLineBeenCrossed() {
        HorseInt h;
        RacingTrackMessage inMessage;

        h = (HorseInt) Thread.currentThread();
        inMessage = exchange(new RacingTrackMessage(
                RacingTrackMessageTypes.HAS_FINISH_LINE_BEEN_CROSSED,
                h.getRaceID(), h.getRaceIdx(), h.getAgility(),
                h.getCurrentPosition(), h.getCurrentStep(), h.getID()));

        if (inMessage.getMethod() == RacingTrackMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    RacingTrackMessageTypes.HAS_FINISH_LINE_BEEN_CROSSED + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        if (inMessage.hasFinishLineBeenCrossed())
            h.setHorseState(HorseState.AT_THE_FINISH_LINE);

        return inMessage.hasFinishLineBeenCrossed();
    }

    /**
     * Method invoked by every Horse that hasn't still crossed the finish line.
     * It generates a new step and updates its position.
     * Finally, it wakes up the next horse in the arrival order to the Racing
     * Track that hasn't finished the race.
     * @param step The distance of the next step the Horse will take.
     */
    public void makeAMove(int step) {
        HorseInt h;
        RacingTrackMessage inMessage;

        h = (HorseInt) Thread.currentThread();

        if (step < 1 || step > h.getAgility())
            throw new IllegalArgumentException("Invalid horse step");

        inMessage = exchange(new RacingTrackMessage(
                RacingTrackMessageTypes.MAKE_A_MOVE, step, h.getRaceID(),
                h.getRaceIdx(), h.getAgility(), h.getCurrentPosition(),
                h.getCurrentStep(), h.getID()));

        if (inMessage.getMethod() == RacingTrackMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    RacingTrackMessageTypes.MAKE_A_MOVE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        h.setHorseState(HorseState.RUNNING);
        h.updateCurrentPosition(step);
    }

    /**
     * Method invoked by each one the Horses coming from the Paddock.
     * They will update their state to AT_THE_STARTING_LINE and will block
     * accordingly to the raceIdx of each one of them in the correspondent
     * condition variable.
     * The last Horse/Jockey pair to arrive also wakes up the Spectators so
     * then can place their bets.
     * After being waken up by the Broker to start the race, they update their
     * state to RUNNING.
     */
    public void proceedToStartLine() {
        HorseInt h;
        RacingTrackMessage inMessage;

        h = (HorseInt) Thread.currentThread();
        inMessage = exchange(new RacingTrackMessage(
                RacingTrackMessageTypes.PROCEED_TO_START_LINE,
                h.getRaceID(), h.getRaceIdx(), h.getAgility(),
                h.getCurrentPosition(), h.getCurrentStep(), h.getID()));

        if (inMessage.getMethod() == RacingTrackMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    RacingTrackMessageTypes.PROCEED_TO_START_LINE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        h.setHorseState(HorseState.AT_THE_STARTING_LINE);
    }

    /**
     * Method invoked by the Broker to signal the Horses to start running.
     */
    public void startTheRace() {
        BrokerInt b;
        RacingTrackMessage inMessage;

        b = (BrokerInt) Thread.currentThread();
        inMessage = exchange(new RacingTrackMessage(
                RacingTrackMessageTypes.START_THE_RACE, 0));

        if (inMessage.getMethod() == RacingTrackMessageTypes.ERROR.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    RacingTrackMessageTypes.START_THE_RACE + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);
    }
}
