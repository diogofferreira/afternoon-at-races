package sharedRegions;

import communication.HostsInfo;
import entities.HorseInt;
import main.EventVariables;
import messageTypes.StableMessageTypes;
import messages.StableMessage;
import serverStates.StableClientsState;
import sharedRegions.Stable;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

/**
 * Interface of the Stable server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class StableInterface {

    /**
     * Counter that registers the number of Horse/Jockey pairs that have already
     * invoked the PROCEED_TO_STABLE method (the total number will be twice the
     * number of pairs) and if the Broker has already invoked the CELEBRATE method.
     * It is useful to close the server socket.
     */
    private int requests;

    /**
     * Instance of the Stable shared region.
     */
    private Stable stable;

    /**
     * Array with the states of active entities.
     */
    private StableClientsState[] stStates;

    /**
     * Creates a new instance of an interface of the Stable shared region.
     * @param stable Instance of the Stable shared region.
     */
    public StableInterface(Stable stable) {
        if (stable == null)
            throw new IllegalArgumentException("Invalid Stable.");

        this.stable = stable;
        this.stStates =
                new StableClientsState[1 + EventVariables.NUMBER_OF_HORSES];

        /* Check if status file exists, if so, load previous state */
        File statusFile = new File(HostsInfo.STABLE_STATUS_PATH.replace("{}", "0"));
        BufferedReader br = null;

        if (statusFile.isFile()) {
            for (int i = 0; i < stStates.length; i++) {
                try {
                    br = new BufferedReader(new FileReader(
                            new File(HostsInfo.STABLE_STATUS_PATH.replace(
                                    "{}", Integer.toString(i)))));
                } catch (FileNotFoundException e) {
                    System.err.format("%s: no such" + " file or directory%n",
                            HostsInfo.STABLE_STATUS_PATH.replace(
                                    "{}", Integer.toString(i)));
                    System.exit(1);
                }

                try {
                    this.stStates[i] = new StableClientsState(br.readLine().trim());
                } catch (Exception e) {
                    System.err.println(e);
                    System.err.println("Invalid Stable status file");
                    System.exit(1);
                }
            }
        } else {
            this.stStates[0] = new StableClientsState(0, null, -1);
            for (int i = 1; i < this.stStates.length; i++)
                this.stStates[i] = new StableClientsState(i, null, -1);
        }
    }

    /**
     * Updates the file which stores all the entities last state on the current server.
     */
    private void updateStatusFile(int id) {
        PrintWriter pw;

        try {
            pw = new PrintWriter(new FileWriter(
                    HostsInfo.STABLE_STATUS_PATH.replace(
                            "{}", Integer.toString(id)), false));
            pw.println(stStates[id]);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the previously created status file.
     */
    public void deleteStatusFiles() {
        for (int i = 0; i < stStates.length; i++) {
            try {
                Files.delete(Paths.get(HostsInfo.STABLE_STATUS_PATH.replace(
                        "{}", Integer.toString(i))));
            } catch (NoSuchFileException x) {
                System.err.format("%s: no such" + " file or directory%n",
                        HostsInfo.CONTROL_CENTRE_STATUS_PATH);
                //System.exit(1);
            } catch (DirectoryNotEmptyException x) {
                System.err.format("%s not empty%n",
                        HostsInfo.CONTROL_CENTRE_STATUS_PATH);
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
    public StableMessage processAndReply(StableMessage inMessage) {
        StableMessageTypes mType;
        StableMessage reply = null;
        int raceID;
        double[] odds;
        HorseInt h;

        if ((mType = StableMessageTypes.getType(inMessage.getMethod())) == null)
            return new StableMessage(
                    inMessage, "Invalid message type");

        switch (mType) {
            case GET_RACE_ODDS:
                raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    reply = new StableMessage(
                            inMessage, "Invalid race ID");
                else {
                    stStates[0].enterMonitor();

                    odds = stStates[0].getOdds();

                    if (stStates[0].getmType() == StableMessageTypes.SUMMON_HORSES_TO_PADDOCK) {
                        odds = stable.getRaceOdds(raceID);

                        // Update control centre status file
                        stStates[0].setmType(
                                StableMessageTypes.GET_RACE_ODDS);
                        updateStatusFile(0);
                    }

                    stStates[0].exitMonitor();
                    reply = new StableMessage(StableMessageTypes.GET_RACE_ODDS,
                            odds, inMessage.getEntityId());
                }
                break;

            case SUMMON_HORSES_TO_PADDOCK:
                raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    reply = new StableMessage(
                            inMessage, "Invalid race ID");
                else {
                    stStates[0].enterMonitor();
                    if (stStates[0].getmType() == null ||
                            stStates[0].getmType() == StableMessageTypes.GET_RACE_ODDS) {
                        stable.summonHorsesToPaddock(raceID);

                        // Update control centre status file
                        stStates[0].setmType(StableMessageTypes.SUMMON_HORSES_TO_PADDOCK);
                        stStates[0].setRaceNumber(raceID);
                        updateStatusFile(0);
                    }
                    stStates[0].exitMonitor();
                    reply = new StableMessage(
                            StableMessageTypes.SUMMON_HORSES_TO_PADDOCK,
                            inMessage.getEntityId());

                }
                break;

            case PROCEED_TO_STABLE:
                h = (HorseInt) Thread.currentThread();

                if (inMessage.getAgility() < 0 ||
                        inMessage.getAgility() > EventVariables.HORSE_MAX_STEP)
                    reply = new StableMessage(
                            inMessage, "Invalid horse agility");
                else if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    reply = new StableMessage(
                            inMessage, "Invalid horse ID");
                else {
                    h.setID(inMessage.getEntityId());
                    h.setAgility(inMessage.getAgility());

                    stStates[1 + inMessage.getEntityId()].enterMonitor();
                    if (stStates[1 + inMessage.getEntityId()].getmType() == null ||
                            (stStates[1 + inMessage.getEntityId()].getmType() ==
                                    StableMessageTypes.PROCEED_TO_STABLE &&
                                    inMessage.isAlreadyRun())) {
                        stable.proceedToStable();
                        requests++;

                        updateStatusFile(1 + inMessage.getEntityId());
                    }

                    stStates[1 + inMessage.getEntityId()].exitMonitor();
                    reply = new StableMessage(StableMessageTypes.PROCEED_TO_STABLE,
                            h.getRaceID(), h.getRaceIdx(), inMessage.getEntityId());
                }
                break;

            case ENTERTAIN_THE_GUESTS:
                stStates[0].enterMonitor();

                if (stStates[0].getmType() == StableMessageTypes.GET_RACE_ODDS) {
                    stable.entertainTheGuests();
                    requests++;

                    // Update control centre status file
                    stStates[0].setmType(StableMessageTypes.ENTERTAIN_THE_GUESTS);
                    updateStatusFile(0);
                }

                stStates[0].exitMonitor();
                reply = new StableMessage(StableMessageTypes.ENTERTAIN_THE_GUESTS,
                        inMessage.getEntityId());

                break;

            default:
                reply = new StableMessage(
                        inMessage, "Invalid message type");
                break;
        }

        return reply;
    }

    /**
     * Method that returns the counter that registers the number of Horse/Jockey
     * pairs that have already invoked the PROCEED_TO_STABLE method (the total
     * number will be twice the number of pairs) and if the Broker has already
     * invoked the CELEBRATE method.
     * @return The counter that registers the number of Horse/Jockey pairs that
     * have already invoked the PROCEED_TO_STABLE method (the totalnumber will
     * be twice the number of pairs) and if the Broker has alreadyinvoked
     * the CELEBRATE method.
     */
    public int getRequests() {
        return requests;
    }
}
