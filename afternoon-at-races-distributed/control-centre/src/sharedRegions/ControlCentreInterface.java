package sharedRegions;

import java.io.*;
import java.nio.file.*;

import communication.HostsInfo;
import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.ControlCentreMessageTypes;
import messages.ControlCentreMessage;
import serverStates.ControlCentreClientsState;

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
    private ControlCentreClientsState[] ccStates;


    /**
     * Creates a new instance of an interface of the Control Centre shared region,
     * as well as the associated status file.
     * @param controlCentre Instance of the Control Centre shared region.
     */
    public ControlCentreInterface(ControlCentre controlCentre) {
        if (controlCentre == null)
            throw new IllegalArgumentException("Invalid Control Centre.");

        this.requests = 0;
        this.controlCentre = controlCentre;
        this.ccStates =
                new ControlCentreClientsState[2 + EventVariables.NUMBER_OF_SPECTATORS];

        /* Check if status file exists, if so, load previous state */
        File statusFile = new File(HostsInfo.CONTROL_CENTRE_STATUS_PATH.replace("{}", "0"));
        BufferedReader br = null;

        if (statusFile.isFile()) {
            for (int i = 0; i < ccStates.length; i++) {
                try {
                    br = new BufferedReader(new FileReader(
                            new File(HostsInfo.CONTROL_CENTRE_STATUS_PATH.replace(
                                    "{}", Integer.toString(i)))));
                } catch (FileNotFoundException e) {
                    System.err.format("%s: no such" + " file or directory%n",
                            HostsInfo.CONTROL_CENTRE_STATUS_PATH.replace(
                                    "{}", Integer.toString(i)));
                    System.exit(1);
                }

                try {
                    this.ccStates[i] = new ControlCentreClientsState(br.readLine().trim());
                } catch (Exception e) {
                    System.err.println(e);
                    System.err.println("Invalid Control Centre status file");
                    System.exit(1);
                }
            }
        } else {
            this.ccStates[0] = new ControlCentreClientsState(0, null, -1);
            this.ccStates[1] = new ControlCentreClientsState(0, null, -1);
            for (int i = 2; i < this.ccStates.length; i++)
                this.ccStates[i] =
                        new ControlCentreClientsState(i, null, -1);
        }
    }

    /**
     * Updates the file which stores all the entities last state on the current server.
     */
    private void updateStatusFile(int id) {
        PrintWriter pw;

        try {
            pw = new PrintWriter(new FileWriter(
                    HostsInfo.CONTROL_CENTRE_STATUS_PATH.replace(
                            "{}", Integer.toString(id)), false));
            pw.println(ccStates[id]);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the previously created status file.
     */
    public void deleteStatusFiles() {
        for (int i = 0; i < ccStates.length; i++) {
            try {
                Files.delete(Paths.get(HostsInfo.CONTROL_CENTRE_STATUS_PATH.replace(
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
    public ControlCentreMessage processAndReply(ControlCentreMessage inMessage) {
        ControlCentreMessageTypes mType;
        ControlCentreMessage reply = null;

        if ((mType = ControlCentreMessageTypes.getType(
                inMessage.getMethod())) == null)
            return new ControlCentreMessage(
                    inMessage, "Invalid message type");


        if (inMessage.getEntityId() < 0)
            return new ControlCentreMessage(
                    inMessage, "Invalid entity ID");

        switch (mType) {
            case OPEN_THE_EVENT:
                ccStates[0].enterMonitor();
                if (ccStates[0].getmType() == null) {
                    controlCentre.openTheEvent();

                    // Update control centre status file
                    ccStates[0].setmType(ControlCentreMessageTypes.OPEN_THE_EVENT);
                    updateStatusFile(0);
                }
                ccStates[0].exitMonitor();
                reply = new ControlCentreMessage(
                        ControlCentreMessageTypes.OPEN_THE_EVENT,
                        inMessage.getEntityId());
                break;

            case SUMMON_HORSES_TO_PADDOCK:
                int raceID = inMessage.getRaceId();

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new ControlCentreMessage(
                            inMessage, "Invalid race ID");
                else {
                    ccStates[0].enterMonitor();
                    if ((ccStates[0].getmType() == ControlCentreMessageTypes.OPEN_THE_EVENT
                            || ccStates[0].getmType() == ControlCentreMessageTypes.REPORT_RESULTS)
                            && raceID == ccStates[0].getRaceNumber() + 1) {
                        controlCentre.summonHorsesToPaddock(inMessage.getRaceId());

                        // Update control centre status file
                        ccStates[0].setmType(
                                ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK);
                        ccStates[0].setRaceNumber(raceID);
                        // UPDATE ALL RACE IDS? IS IT WORTH IT?
                        updateStatusFile(0);
                    }
                    ccStates[0].exitMonitor();
                    reply = new ControlCentreMessage(
                            ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK,
                            inMessage.getEntityId());
                }
                break;

            case WAIT_FOR_NEXT_RACE:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS)
                    reply = new ControlCentreMessage(
                            inMessage, "Invalid spectator ID");
                else {
                    ((SpectatorInt) Thread.currentThread()).setID(
                            inMessage.getEntityId());

                    ccStates[2 + inMessage.getEntityId()].enterMonitor();

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
                        updateStatusFile(2 + inMessage.getEntityId());
                    }
                    ccStates[2 + inMessage.getEntityId()].exitMonitor();

                    reply = new ControlCentreMessage(
                            ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE,
                            isThereARace, inMessage.getEntityId());
                }
                break;

            case PROCEED_TO_PADDOCK:
                ccStates[1].enterMonitor();
                if (ccStates[1].getmType() == null ||
                        ccStates[1].getmType() == ControlCentreMessageTypes.FINISH_THE_RACE) {
                    controlCentre.proceedToPaddock();

                    // Update control centre status file
                    ccStates[1].setmType(ControlCentreMessageTypes.PROCEED_TO_PADDOCK);
                    ccStates[1].setEntityId(inMessage.getEntityId());
                    ccStates[1].setRaceNumber(inMessage.getRaceId());
                    updateStatusFile(1);
                }
                ccStates[1].exitMonitor();
                reply = new ControlCentreMessage(
                        ControlCentreMessageTypes.PROCEED_TO_PADDOCK,
                        inMessage.getEntityId());
                break;

            case GO_CHECK_HORSES:
                ccStates[2 + inMessage.getEntityId()].enterMonitor();
                if (ccStates[2 + inMessage.getEntityId()].getmType() ==
                        ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE) {
                    controlCentre.goCheckHorses();

                    // Update control centre status file
                    ccStates[2 + inMessage.getEntityId()].setmType(
                            ControlCentreMessageTypes.GO_CHECK_HORSES);
                    updateStatusFile(2 + inMessage.getEntityId());
                }
                ccStates[2 + inMessage.getEntityId()].exitMonitor();
                reply = new ControlCentreMessage(
                        ControlCentreMessageTypes.GO_CHECK_HORSES,
                        inMessage.getEntityId());
                break;

            case GO_WATCH_THE_RACE:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS)
                    reply = new ControlCentreMessage(
                            inMessage, "Invalid spectator ID");
                else {
                    ((SpectatorInt) Thread.currentThread()).setID(
                            inMessage.getEntityId());

                    ccStates[2 + inMessage.getEntityId()].enterMonitor();
                    if (ccStates[2 + inMessage.getEntityId()].getmType() ==
                            ControlCentreMessageTypes.GO_CHECK_HORSES ||
                            ccStates[2 + inMessage.getEntityId()].getmType() ==
                                    ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE) {
                        controlCentre.goWatchTheRace();

                        // Update control centre status file
                        ccStates[2 + inMessage.getEntityId()].setmType(
                                ControlCentreMessageTypes.GO_WATCH_THE_RACE);
                        updateStatusFile(2 + inMessage.getEntityId());
                    }
                    ccStates[2 + inMessage.getEntityId()].exitMonitor();
                    reply = new ControlCentreMessage(
                            ControlCentreMessageTypes.GO_WATCH_THE_RACE,
                            inMessage.getEntityId());
                }
                break;

            case START_THE_RACE:
                ccStates[0].enterMonitor();
                if (ccStates[0].getmType() ==
                        ControlCentreMessageTypes.SUMMON_HORSES_TO_PADDOCK) {
                    controlCentre.startTheRace();

                    // Update control centre status file
                    ccStates[0].setmType(ControlCentreMessageTypes.START_THE_RACE);
                    updateStatusFile(0);
                }
                ccStates[0].exitMonitor();
                reply = new ControlCentreMessage(
                        ControlCentreMessageTypes.START_THE_RACE,
                        inMessage.getEntityId());
                break;

            case FINISH_THE_RACE:
                int[] standings = inMessage.getStandings();

                if (standings == null ||
                        standings.length != EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    reply = new ControlCentreMessage(
                            inMessage, "Invalid standings array");
                else {
                    ccStates[1].enterMonitor();
                    if (ccStates[1].getmType() ==
                            ControlCentreMessageTypes.PROCEED_TO_PADDOCK) {
                        controlCentre.finishTheRace(standings);

                        // Update control centre status file
                        ccStates[1].setmType(ControlCentreMessageTypes.FINISH_THE_RACE);
                        ccStates[1].setEntityId(inMessage.getEntityId());
                        updateStatusFile(1);
                    }
                    ccStates[1].exitMonitor();
                    reply = new ControlCentreMessage(
                            ControlCentreMessageTypes.FINISH_THE_RACE,
                            inMessage.getEntityId());
                }
                break;

            case REPORT_RESULTS:
                int[] winners = ccStates[0].getWinners();

                ccStates[0].enterMonitor();
                if (ccStates[0].getmType() ==
                        ControlCentreMessageTypes.START_THE_RACE) {
                    winners = controlCentre.reportResults();

                    // Update control centre status file
                    ccStates[0].setmType(ControlCentreMessageTypes.REPORT_RESULTS);
                    ccStates[0].setWinners(winners);
                    updateStatusFile(0);
                }
                ccStates[0].exitMonitor();
                reply = new ControlCentreMessage(
                        ControlCentreMessageTypes.REPORT_RESULTS, winners,
                        inMessage.getEntityId());
                break;

            case HAVE_I_WON:
                boolean haveIWon = ccStates[2 + inMessage.getEntityId()].isHaveIWon();
                int horseIdx = inMessage.getHorseIdx();

                if (horseIdx < 0 ||
                        horseIdx >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    reply = new ControlCentreMessage(
                            inMessage, "Invalid horse Idx");
                else {
                    ccStates[2 + inMessage.getEntityId()].enterMonitor();
                    if (ccStates[2 + inMessage.getEntityId()].getmType() ==
                            ControlCentreMessageTypes.GO_WATCH_THE_RACE) {
                        haveIWon = controlCentre.haveIWon(horseIdx);

                        // Update control centre status file
                        ccStates[2 + inMessage.getEntityId()].setmType(
                                ControlCentreMessageTypes.HAVE_I_WON);
                        ccStates[2 + inMessage.getEntityId()].setHaveIWon(haveIWon);
                        updateStatusFile(2 + inMessage.getEntityId());
                    }
                    ccStates[2 + inMessage.getEntityId()].exitMonitor();
                    reply = new ControlCentreMessage(
                            ControlCentreMessageTypes.HAVE_I_WON, haveIWon,
                            inMessage.getEntityId());
                }
                break;

            case CELEBRATE:
                ccStates[0].enterMonitor();
                if (ccStates[0].getmType() ==
                        ControlCentreMessageTypes.REPORT_RESULTS) {
                    controlCentre.celebrate();

                    // Update control centre status file
                    ccStates[0].setmType(ControlCentreMessageTypes.CELEBRATE);
                    updateStatusFile(0);
                }
                ccStates[0].exitMonitor();
                reply = new ControlCentreMessage(
                        ControlCentreMessageTypes.CELEBRATE,
                        inMessage.getEntityId());
                break;

            case RELAX_A_BIT:
                if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_SPECTATORS)
                    reply = new ControlCentreMessage(
                            inMessage, "Invalid spectator ID");
                else {
                    ((SpectatorInt) Thread.currentThread()).setID(
                            inMessage.getEntityId());

                    ccStates[2 + inMessage.getEntityId()].enterMonitor();
                    if (ccStates[2 + inMessage.getEntityId()].getmType() ==
                            ControlCentreMessageTypes.WAIT_FOR_NEXT_RACE) {
                        controlCentre.relaxABit();

                        // Update control centre status file
                        ccStates[2 + inMessage.getEntityId()].setmType(
                                ControlCentreMessageTypes.RELAX_A_BIT);
                        updateStatusFile(2 + inMessage.getEntityId());

                        requests++;
                    }
                    ccStates[2 + inMessage.getEntityId()].exitMonitor();
                    reply = new ControlCentreMessage(
                            ControlCentreMessageTypes.RELAX_A_BIT,
                            inMessage.getEntityId());
                }
                break;

            default:
                reply = new ControlCentreMessage(
                        inMessage, "Invalid message type");
                break;
        }

        return reply;
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
