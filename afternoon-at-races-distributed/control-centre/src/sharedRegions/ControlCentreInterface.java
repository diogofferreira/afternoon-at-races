package sharedRegions;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

import communication.HostsInfo;
import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.ControlCentreMessageTypes;
import messages.ControlCentreMessage;
import serverStates.ControlCentreState;

/**
 * Interface of the Control Centre server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class ControlCentreInterface {

    /**
     * Counter that registers the number of Spectators that have already invoked
     * the RELAX_A_BIT method. It is useful to close the server socket.
     */
    private int requests;

    /**
     * Instance of the Control Centre shared region.
     */
    private ControlCentre controlCentre;

    /**
     * Array with the states of active entities.
     */
    private ControlCentreState[] ccStates;

    /**
     * Creates a new instance of an interface of the Control Centre shared region, as well as the associated status file.
     * @param controlCentre Instance of the Control Centre shared region.
     */
    public ControlCentreInterface(ControlCentre controlCentre) {
        if (controlCentre == null)
            throw new IllegalArgumentException("Invalid Control Centre.");

        this.requests = 0;
        this.controlCentre = controlCentre;
        this.ccStates = new ControlCentreState[2 + EventVariables.NUMBER_OF_SPECTATORS];

        /* Check if status file exists, if so, load previous state */
        File statusFile = new File(HostsInfo.CONTROL_CENTRE_STATUS_PATH);
        BufferedReader br = null;

        if (statusFile.isFile()) {
            try {
                br = new BufferedReader(new FileReader(statusFile));
            } catch (FileNotFoundException e) {
                System.err.format("%s: no such" + " file or directory%n",
                        HostsInfo.CONTROL_CENTRE_STATUS_PATH);
                System.exit(1);
            }

            try {
                for (int i = 0; i < this.ccStates.length; i++)
                    this.ccStates[i] = new ControlCentreState(br.readLine().trim());

            } catch (Exception e) {
                System.err.println(e);
                System.err.println("Invalid Control Centre status file");
                System.exit(1);
            }
        } else {
            this.ccStates[0] = new ControlCentreState(0, null, -1);
            this.ccStates[1] = new ControlCentreState(0, null, -1);
            for (int i = 2; i < this.ccStates.length; i++)
                this.ccStates[i] = new ControlCentreState(i, null, -1);
        }
    }

    /**
     * Updates the file which stores all the entities last state on the current server.
     */
    private void updateStatusFile() {
        PrintWriter pw;

        try {
            pw = new PrintWriter(new FileWriter(HostsInfo.CONTROL_CENTRE_STATUS_PATH, false));
            for (ControlCentreState ccs : ccStates)
                pw.println(ccs);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the previously created status file.
     */
    public void deleteStatusFile() {
        try {
            Files.delete(Paths.get(HostsInfo.CONTROL_CENTRE_STATUS_PATH));
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n",
                    HostsInfo.CONTROL_CENTRE_STATUS_PATH);
            System.exit(1);
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
    public ControlCentreMessage processAndReply(ControlCentreMessage inMessage) {
        ControlCentreMessageTypes mType;

        if ((mType = ControlCentreMessageTypes.getType(inMessage.getMethod())) == null)
            return new ControlCentreMessage(
                    inMessage, "Invalid message type");

        if (inMessage.getEntityId() < 0)
            return new ControlCentreMessage(
                    inMessage, "Invalid entity ID");

        switch (mType) {
            case OPEN_THE_EVENT:
                if (ccStates[0].getmType() == null) {
                    controlCentre.openTheEvent();

                    // Update control centre status file
                    ccStates[0].setmType(ControlCentreMessageTypes.OPEN_THE_EVENT);
                    updateStatusFile();
                }
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.OPEN_THE_EVENT,
                        inMessage.getEntityId());

            case SUMMON_HORSES_TO_PADDOCK:
                int raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new ControlCentreMessage(
                            inMessage, "Invalid race ID");

                System.out.println(ccStates[0]);
                System.out.println(raceID);

                if ((ccStates[0].getmType() == ControlCentreMessageTypes.OPEN_THE_EVENT
                        || ccStates[0].getmType() == ControlCentreMessageTypes.REPORT_RESULTS)
                        && raceID == ccStates[0].getRaceNumber() + 1) {
                    controlCentre.summonHorsesToPaddock(inMessage.getRaceId());

                    // Update control centre status file
                    ccStates[0].setmType(
                            ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK);
                    ccStates[0].setRaceNumber(raceID);
                    // UPDATE ALL RACE IDS? IS IT WORTH IT?
                    updateStatusFile();
                }
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK,
                        inMessage.getEntityId());

            case WAIT_FOR_NEXT_RACE:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS)
                    return new ControlCentreMessage(
                            inMessage, "Invalid spectator ID");

                ((SpectatorInt) Thread.currentThread()).setID(
                        inMessage.getEntityId());

                boolean isThereARace = ccStates[2 + inMessage.getEntityId()].isHaveIWon();

                if (ccStates[2 + inMessage.getEntityId()].getmType() == null ||
                        ccStates[2 + inMessage.getEntityId()].getmType() ==
                                ControlCentreMessageTypes.HAVE_I_WON) {
                    isThereARace = controlCentre.waitForNextRace();

                    // Update control centre status file
                    ccStates[2 + inMessage.getEntityId()].setmType(
                            ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE);
                    ccStates[2 + inMessage.getEntityId()].setThereARace(
                            isThereARace);
                    updateStatusFile();
                }

                return new ControlCentreMessage(
                        ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE,
                        isThereARace, inMessage.getEntityId());

            case PROCEED_TO_PADDOCK:
                if (ccStates[1].getmType() == null ||
                        ccStates[1].getmType() == ControlCentreMessageTypes.FINISH_THE_RACE) {
                    controlCentre.proceedToPaddock();

                    // Update control centre status file
                    ccStates[1].setmType(ControlCentreMessageTypes.PROCEED_TO_PADDOCK);
                    ccStates[1].setEntityId(inMessage.getEntityId());
                    ccStates[1].setRaceNumber(inMessage.getRaceId());
                    updateStatusFile();
                }
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.PROCEED_TO_PADDOCK,
                        inMessage.getEntityId());

            case GO_CHECK_HORSES:
                if (ccStates[2 + inMessage.getEntityId()].getmType() ==
                        ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE) {
                    controlCentre.goCheckHorses();

                    // Update control centre status file
                    ccStates[2 + inMessage.getEntityId()].setmType(
                            ControlCentreMessageTypes.GO_CHECK_HORSES);
                    updateStatusFile();
                }
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.GO_CHECK_HORSES,
                        inMessage.getEntityId());

            case GO_WATCH_THE_RACE:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS)
                    return new ControlCentreMessage(
                            inMessage, "Invalid spectator ID");

                ((SpectatorInt) Thread.currentThread()).setID(
                        inMessage.getEntityId());

                if (ccStates[2 + inMessage.getEntityId()].getmType() ==
                        ControlCentreMessageTypes.GO_CHECK_HORSES ||
                        ccStates[2 + inMessage.getEntityId()].getmType() ==
                                ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE) {
                    controlCentre.goWatchTheRace();

                    // Update control centre status file
                    ccStates[2 + inMessage.getEntityId()].setmType(
                            ControlCentreMessageTypes.GO_WATCH_THE_RACE);
                    updateStatusFile();
                }

                return new ControlCentreMessage(
                        ControlCentreMessageTypes.GO_WATCH_THE_RACE,
                        inMessage.getEntityId());

            case START_THE_RACE:
                if (ccStates[0].getmType() ==
                        ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK) {
                    controlCentre.startTheRace();

                    // Update control centre status file
                    ccStates[0].setmType(ControlCentreMessageTypes.START_THE_RACE);
                    updateStatusFile();
                }

                return new ControlCentreMessage(
                        ControlCentreMessageTypes.START_THE_RACE,
                        inMessage.getEntityId());

            case FINISH_THE_RACE:
                int[] standings = inMessage.getStandings();

                if (standings == null ||
                        standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new ControlCentreMessage(
                            inMessage, "Invalid standings array");

                if (ccStates[1].getmType() ==
                        ControlCentreMessageTypes.PROCEED_TO_PADDOCK) {
                    controlCentre.finishTheRace(standings);

                    // Update control centre status file
                    ccStates[1].setmType(ControlCentreMessageTypes.FINISH_THE_RACE);
                    ccStates[1].setEntityId(inMessage.getEntityId());
                    updateStatusFile();
                }

                return new ControlCentreMessage(
                        ControlCentreMessageTypes.FINISH_THE_RACE,
                        inMessage.getEntityId());

            case REPORT_RESULTS:
                int[] winners = ccStates[0].getWinners();

                if (ccStates[0].getmType() ==
                        ControlCentreMessageTypes.START_THE_RACE) {
                    winners = controlCentre.reportResults();

                    // Update control centre status file
                    ccStates[0].setmType(ControlCentreMessageTypes.REPORT_RESULTS);
                    ccStates[0].setWinners(winners);
                    updateStatusFile();
                }
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.REPORT_RESULTS, winners,
                        inMessage.getEntityId());

            case HAVE_I_WON:
                boolean haveIWon = ccStates[2 + inMessage.getEntityId()].isHaveIWon();
                int horseIdx = inMessage.getHorseIdx();

                if (horseIdx < 0 ||
                        horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    return new ControlCentreMessage(
                            inMessage, "Invalid horse Idx");

                if (ccStates[2 + inMessage.getEntityId()].getmType() ==
                        ControlCentreMessageTypes.GO_WATCH_THE_RACE) {
                    haveIWon = controlCentre.haveIWon(horseIdx);

                    // Update control centre status file
                    ccStates[2 + inMessage.getEntityId()].setmType(
                            ControlCentreMessageTypes.HAVE_I_WON);
                    ccStates[2 + inMessage.getEntityId()].setHaveIWon(haveIWon);
                    updateStatusFile();
                }

                return new ControlCentreMessage(
                        ControlCentreMessageTypes.HAVE_I_WON, haveIWon,
                        inMessage.getEntityId());

            case CELEBRATE:
                if (ccStates[0].getmType() ==
                        ControlCentreMessageTypes.REPORT_RESULTS) {
                    controlCentre.celebrate();

                    // Update control centre status file
                    ccStates[0].setmType(ControlCentreMessageTypes.CELEBRATE);
                    updateStatusFile();
                }

                return new ControlCentreMessage(
                        ControlCentreMessageTypes.CELEBRATE,
                        inMessage.getEntityId());

            case RELAX_A_BIT:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS)
                    return new ControlCentreMessage(
                            inMessage, "Invalid spectator ID");

                ((SpectatorInt) Thread.currentThread()).setID(
                        inMessage.getEntityId());

                if (ccStates[2 + inMessage.getEntityId()].getmType() ==
                        ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE) {
                    controlCentre.relaxABit();

                    // Update control centre status file
                    ccStates[2 + inMessage.getEntityId()].setmType(
                            ControlCentreMessageTypes.RELAX_A_BIT);
                    updateStatusFile();

                    requests++;
                }
                return new ControlCentreMessage(
                        ControlCentreMessageTypes.RELAX_A_BIT,
                        inMessage.getEntityId());

            default:
                return new ControlCentreMessage(
                        inMessage, "Invalid message type");
        }
    }

    /**
     * Method that returns the counter that registers the number of Spectators
     * that have already invoked the RELAX_A_BIT method.
     * @return The counter that registers the number of Spectators
     * that have already invoked the RELAX_A_BIT method.
     */
    public int getRequests() {
        return requests;
    }
}
