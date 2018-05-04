package stubs;

import communication.ClientCom;
import entities.HorseInt;
import entities.SpectatorInt;
import messageTypes.PaddockMessageTypes;
import messages.PaddockMessage;
import states.HorseState;
import states.SpectatorState;

/**
 * This data type defines the communication stub of Paddock.
 */
public class PaddockStub {
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
    public PaddockStub(String hostName, int port) {
        serverHostName = hostName;
        serverPortNumb = port;
    }

    /**
     * Message exchanged with Paddock server.
     */
    private PaddockMessage exchange(PaddockMessage outMessage) {
        ClientCom com = new ClientCom(serverHostName, serverPortNumb);
        PaddockMessage inMessage;

        while (!com.open()) {
            try {
                Thread.currentThread().sleep((long)10);
            } catch (InterruptedException e) {
            }
        }

        com.writeObject(outMessage);
        inMessage = (PaddockMessage)com.readObject();
        com.close();

        return inMessage;
    }

    /**
     * Method invoked by each one of the Spectators where they will update their
     * state to APPRAISING_THE_HORSES and will block waiting
     */
    public void goCheckHorses() {
        SpectatorInt s;
        PaddockMessage inMessage;

        s = (SpectatorInt) Thread.currentThread();
        inMessage = exchange(new PaddockMessage(
                PaddockMessageTypes.GO_CHECK_HORSES, s.getID()));

        if (inMessage.getMethod() !=
                PaddockMessageTypes.GO_CHECK_HORSES.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    PaddockMessageTypes.GO_CHECK_HORSES + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.APPRAISING_THE_HORSES);
    }

    /**
     * Method invoked by each one of the Horses. They will change their state
     * to AT_THE_PADDOCK and wait until all Spectators arrive to the Paddock.
     */
    public void proceedToPaddock() {
        HorseInt h;
        PaddockMessage inMessage;

        h = (HorseInt) Thread.currentThread();
        inMessage = exchange(new PaddockMessage(
                PaddockMessageTypes.PROCEED_TO_PADDOCK, h.getRaceID(),
                h.getRaceIdx(), h.getID()));

        if (inMessage.getMethod() !=
                PaddockMessageTypes.PROCEED_TO_PADDOCK.getId()) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    " An error ocurred in " +
                    PaddockMessageTypes.PROCEED_TO_PADDOCK + ": " +
                    inMessage.getErrorMessage());
            System.out.println(inMessage);
            System.exit(1);
        }

        h.setHorseState(HorseState.AT_THE_PADDOCK);
    }


}
