package sharedRegions;

import communication.HostsInfo;
import entities.HorseInt;
import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.PaddockMessageTypes;
import messages.PaddockMessage;
import serverStates.PaddockClientsState;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Interface of the Paddock server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class PaddockInterface {

    /**
     * Instance of the Paddock shared region.
     */
    private Paddock paddock;

    /**
     * Array with the states of active entities.
     */
    private PaddockClientsState[] pdStates;

    /**
     * Creates a new instance of an interface of the Paddock shared region.
     * @param paddock Instance of the Paddock shared region.
     */
    public PaddockInterface(Paddock paddock) {
        if (paddock == null)
            throw new IllegalArgumentException("Invalid Paddock.");

        this.paddock = paddock;
        this.pdStates = new PaddockClientsState[EventVariables.NUMBER_OF_SPECTATORS
                + EventVariables.NUMBER_OF_HORSES_PER_RACE];

        /* Check if status file exists, if so, load previous state */
        File statusFile = new File(HostsInfo.PADDOCK_STATUS_PATH.replace("{}", "0"));
        BufferedReader br = null;

        if (statusFile.isFile()) {
            for (int i = 0; i < pdStates.length; i++) {
                try {
                    br = new BufferedReader(new FileReader(
                            new File(HostsInfo.PADDOCK_STATUS_PATH.replace(
                                    "{}", Integer.toString(i)))));
                } catch (FileNotFoundException e) {
                    System.err.format("%s: no such" + " file or directory%n",
                            HostsInfo.PADDOCK_STATUS_PATH.replace(
                                    "{}", Integer.toString(i)));
                    System.exit(1);
                }

                try {
                    this.pdStates[i] = new PaddockClientsState(br.readLine().trim());
                } catch (Exception e) {
                    System.err.println(e);
                    System.err.println("Invalid Paddock client status file");
                    System.exit(1);
                }
            }
        } else {
            for (int i = 0; i < this.pdStates.length; i++) {
                this.pdStates[i] = new PaddockClientsState(i, null, -1);
                updateStatusFile(i);
            }
        }
    }

    /**
     * Updates the file which stores all the entities last state on the current server.
     * @param id Id of the entity writing in its log.
     */
    private void updateStatusFile(int id) {
        PrintWriter pw;

        try {
            pw = new PrintWriter(new FileWriter(
                    HostsInfo.PADDOCK_STATUS_PATH.replace(
                            "{}", Integer.toString(id)), false));
            pw.println(pdStates[id]);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the previously created status file.
     */
    public void deleteStatusFiles() {
        for (int i = 0; i < pdStates.length; i++) {
            try {
                Files.delete(Paths.get(HostsInfo.PADDOCK_STATUS_PATH.replace(
                        "{}", Integer.toString(i))));
            } catch (NoSuchFileException x) {
                System.err.format("%s: no such" + " file or directory%n",
                        HostsInfo.PADDOCK_STATUS_PATH.replace(
                                "{}", Integer.toString(i)));
                //System.exit(1);
            } catch (DirectoryNotEmptyException x) {
                System.err.format("%s not empty%n",
                        HostsInfo.PADDOCK_STATUS_PATH.replace(
                                "{}", Integer.toString(i)));
                System.exit(1);
            } catch (IOException x) {
                // File permission problems are caught here.
                System.err.println(x);
                System.exit(1);
            }
        }
    }

    /**
     * Method that processes a request coming from the APS, interacts with the
     * shared region and returns a the response to the method invoked in the
     * shared region.
     * @param inMessage The client's incoming message, which contains the
     *                  information and arguments necessary to invoke the
     *                  corresponding method in the shared region.
     * @return The server's outgoing message, with all the information that
     * the invoked method returned and other entity attributes updates.
     */
    public PaddockMessage processAndReply(PaddockMessage inMessage) {
        PaddockMessageTypes mType;
        PaddockMessage reply = null;

        if ((mType = PaddockMessageTypes.getType(inMessage.getMethod())) == null)
            return new PaddockMessage(
                    inMessage, "Invalid message type");

        switch (mType) {
            case GO_CHECK_HORSES:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS)
                    reply = new PaddockMessage(
                            inMessage, "Invalid spectator ID");
                else {
                    ((SpectatorInt) Thread.currentThread()).setID(inMessage.getEntityId());

                    pdStates[inMessage.getEntityId()].enterMonitor();

                    if (pdStates[inMessage.getEntityId()].getmType() == null ||
                            (pdStates[inMessage.getEntityId()].getmType() == PaddockMessageTypes.GO_CHECK_HORSES &&
                                    pdStates[inMessage.getEntityId()].getRaceNumber() + 1 == inMessage.getRaceID())) {

                        paddock.goCheckHorses();

                        // Update paddock status file
                        pdStates[inMessage.getEntityId()].setmType(
                                PaddockMessageTypes.GO_CHECK_HORSES);
                        pdStates[inMessage.getEntityId()].setRaceNumber(
                                inMessage.getRaceID());
                        pdStates[inMessage.getEntityId()].increaseRequests();
                        updateStatusFile(inMessage.getEntityId());
                    }
                    pdStates[inMessage.getEntityId()].exitMonitor();
                    reply = new PaddockMessage(PaddockMessageTypes.GO_CHECK_HORSES,
                            inMessage.getEntityId());
                }
                break;

            case PROCEED_TO_PADDOCK:
                if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES)
                    reply = new PaddockMessage(
                            inMessage, "Invalid race ID");
                else if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    reply = new PaddockMessage(
                            inMessage, "Invalid horse Idx");
                else if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    reply = new PaddockMessage(
                            inMessage, "Invalid horse ID");
                else {
                    ((HorseInt) Thread.currentThread()).setRaceID(inMessage.getRaceID());
                    ((HorseInt) Thread.currentThread()).setRaceIdx(inMessage.getRaceIdx());
                    ((HorseInt) Thread.currentThread()).setID(inMessage.getEntityId());

                    pdStates[EventVariables.NUMBER_OF_SPECTATORS +
                            inMessage.getRaceIdx()].enterMonitor();

                    if (pdStates[EventVariables.NUMBER_OF_SPECTATORS +
                            inMessage.getRaceIdx()].getmType() == null ||
                            (pdStates[EventVariables.NUMBER_OF_SPECTATORS +
                                    inMessage.getRaceIdx()].getmType() == PaddockMessageTypes.PROCEED_TO_PADDOCK &&
                                    pdStates[EventVariables.NUMBER_OF_SPECTATORS +
                                            inMessage.getRaceIdx()].getRaceNumber() + 1 == inMessage.getRaceID())) {

                        paddock.proceedToPaddock();

                        // Update paddock status file
                        pdStates[EventVariables.NUMBER_OF_SPECTATORS +
                                inMessage.getRaceIdx()].setmType(
                                PaddockMessageTypes.PROCEED_TO_PADDOCK);
                        pdStates[EventVariables.NUMBER_OF_SPECTATORS +
                                inMessage.getRaceIdx()].setRaceNumber(
                                inMessage.getRaceID());
                        pdStates[EventVariables.NUMBER_OF_SPECTATORS +
                                inMessage.getRaceIdx()].increaseRequests();
                        updateStatusFile(EventVariables.NUMBER_OF_SPECTATORS +
                                inMessage.getRaceIdx());
                    }

                    pdStates[EventVariables.NUMBER_OF_SPECTATORS +
                            inMessage.getRaceIdx()].exitMonitor();
                    reply = new PaddockMessage(PaddockMessageTypes.PROCEED_TO_PADDOCK,
                            inMessage.getEntityId());
                }
                break;

            default:
                reply = new PaddockMessage(
                        inMessage, "Invalid message type");
                break;
        }

        return reply;
    }

    /**
     * Method that returns the counter that registers the number of Spectators
     * that have already invoked the PROCEED_TO_PADDOCK method.
     * @return The counter that registers the number of Spectators
     * that have already invoked the PROCEED_TO_PADDOCK method.
     */
    public int getRequests() {
        return Arrays.stream(pdStates).mapToInt(x -> x.getRequests()).sum();
    }
}
