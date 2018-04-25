package stubs;

import communication.ClientCom;
import entities.Horse;
import entities.Spectator;
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

    public void goCheckHorses() {
        Spectator s;
        PaddockMessage inMessage;

        s = (Spectator) Thread.currentThread();
        inMessage = exchange(new PaddockMessage(
                PaddockMessageTypes.GO_CHECK_HORSES, s.getID()));

        if (inMessage.getMethod() == PaddockMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    PaddockMessageTypes.GO_CHECK_HORSES);
            System.exit(1);
        }

        s.setSpectatorState(SpectatorState.APPRAISING_THE_HORSES);
    }

    public void proceedToPaddock() {
        Horse h;
        PaddockMessage inMessage;

        h = (Horse) Thread.currentThread();
        inMessage = exchange(new PaddockMessage(
                PaddockMessageTypes.PROCEED_TO_PADDOCK, h.getRaceID(),
                h.getRaceIdx(), h.getID()));

        if (inMessage.getMethod() == PaddockMessageTypes.ERROR.getId()) {
            System.out.println(Thread.currentThread().getName() +
                    " - An unknown error ocurred in " +
                    PaddockMessageTypes.PROCEED_TO_PADDOCK);
            System.exit(1);
        }

        h.setHorseState(HorseState.AT_THE_PADDOCK);
    }


}
