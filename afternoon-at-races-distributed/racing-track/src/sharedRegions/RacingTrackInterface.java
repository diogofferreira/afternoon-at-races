package sharedRegions;

import communication.HostsInfo;
import entities.HorseInt;
import main.EventVariables;
import messageTypes.RacingTrackMessageTypes;
import messages.RacingTrackMessage;
import serverStates.RacingTrackClientsState;
import sharedRegions.RacingTrack;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Interface of the Racing Track server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class RacingTrackInterface {

    /**
     * Instance of the Racing Track shared region.
     */
    private RacingTrack racingTrack;

    /**
     * Array with the states of active entities.
     */
    private RacingTrackClientsState[] rtStates;

    /**
     * Creates a new instance of an interface of the Racing Track shared region.
     * @param racingTrack Instance of the Racing Track shared region.
     */
    public RacingTrackInterface(RacingTrack racingTrack) {
        if (racingTrack == null)
            throw new IllegalArgumentException("Invalid Racing Track.");

        this.racingTrack = racingTrack;
        this.rtStates = new RacingTrackClientsState[
                1 + EventVariables.NUMBER_OF_HORSES];

        /* Check if status file exists, if so, load previous state */
        File statusFile = new File(HostsInfo.RACING_TRACK_STATUS_PATH.replace("{}", "0"));
        BufferedReader br = null;

        if (statusFile.isFile()) {
            for (int i = 0; i < rtStates.length; i++) {
                try {
                    br = new BufferedReader(new FileReader(
                            new File(HostsInfo.RACING_TRACK_STATUS_PATH.replace(
                                    "{}", Integer.toString(i)))));
                } catch (FileNotFoundException e) {
                    System.err.format("%s: no such" + " file or directory%n",
                            HostsInfo.RACING_TRACK_STATUS_PATH.replace(
                                    "{}", Integer.toString(i)));
                    System.exit(1);
                }

                try {
                    this.rtStates[i] = new RacingTrackClientsState(br.readLine().trim());
                } catch (Exception e) {
                    System.err.println(e);
                    System.err.println("Invalid Racing Track client status file");
                    System.exit(1);
                }
            }
        } else {
            for (int i = 0; i < this.rtStates.length; i++) {
                this.rtStates[i] = new RacingTrackClientsState(i, null, -1);
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
                    HostsInfo.RACING_TRACK_STATUS_PATH.replace(
                            "{}", Integer.toString(id)), false));
            pw.println(rtStates[id]);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the previously created status file.
     */
    public void deleteStatusFiles() {
        for (int i = 0; i < rtStates.length; i++) {
            try {
                Files.delete(Paths.get(HostsInfo.RACING_TRACK_STATUS_PATH.replace(
                        "{}", Integer.toString(i))));
            } catch (NoSuchFileException x) {
                System.err.format("%s: no such" + " file or directory%n",
                        HostsInfo.RACING_TRACK_STATUS_PATH.replace(
                                "{}", Integer.toString(i)));
                //System.exit(1);
            } catch (DirectoryNotEmptyException x) {
                System.err.format("%s not empty%n",
                        HostsInfo.RACING_TRACK_STATUS_PATH.replace(
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
    public RacingTrackMessage processAndReply(RacingTrackMessage inMessage) {
        RacingTrackMessageTypes mType;
        RacingTrackMessage reply = null;
        int step;
        boolean hasFinishLineBeenCrossed;

        if ((mType = RacingTrackMessageTypes.getType(inMessage.getMethod())) == null)
            return new RacingTrackMessage(
                    inMessage, "Invalid message type");

        switch (mType) {
            case HAS_FINISH_LINE_BEEN_CROSSED:
                if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid race ID");
                else if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid horse Idx");
                else if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid horse ID");
                else if (inMessage.getCurrentPosition() < 0
                        || inMessage.getCurrentStep() < 0)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid horse position and/or step");
                else {

                    ((HorseInt) Thread.currentThread()).setRaceID(
                            inMessage.getRaceID());
                    ((HorseInt) Thread.currentThread()).setRaceIdx(
                            inMessage.getRaceIdx());
                    ((HorseInt) Thread.currentThread()).setID(
                            inMessage.getEntityId());
                    ((HorseInt) Thread.currentThread()).setCurrentPosition(
                            inMessage.getCurrentPosition());
                    ((HorseInt) Thread.currentThread()).setCurrentStep(
                            inMessage.getCurrentStep());

                    rtStates[1 + inMessage.getEntityId()].enterMonitor();
                    hasFinishLineBeenCrossed =
                            rtStates[1 + inMessage.getEntityId()].isHasFinishLineBeenCrossed();

                    //System.out.println(inMessage);

                    if (rtStates[1 + inMessage.getEntityId()].getmType() ==
                            RacingTrackMessageTypes.PROCEED_TO_START_LINE ||
                            rtStates[1 + inMessage.getEntityId()].getmType() ==
                                    RacingTrackMessageTypes.MAKE_A_MOVE) {

                        hasFinishLineBeenCrossed = racingTrack.hasFinishLineBeenCrossed();
                        if (hasFinishLineBeenCrossed)
                            rtStates[1 + inMessage.getEntityId()].increaseRequests();

                        // Update racing track status file
                        rtStates[1 + inMessage.getEntityId()].setmType(
                                RacingTrackMessageTypes.HAS_FINISH_LINE_BEEN_CROSSED);
                        rtStates[1 + inMessage.getEntityId()].setRaceNumber(
                                inMessage.getRaceID());
                        rtStates[1 + inMessage.getEntityId()].setHasFinishLineBeenCrossed(
                                hasFinishLineBeenCrossed);
                        updateStatusFile(1 + inMessage.getEntityId());
                    }

                    rtStates[1 + inMessage.getEntityId()].exitMonitor();
                    reply = new RacingTrackMessage(
                            RacingTrackMessageTypes.HAS_FINISH_LINE_BEEN_CROSSED,
                            hasFinishLineBeenCrossed, inMessage.getEntityId());
                }
                break;

            case MAKE_A_MOVE:
                step = inMessage.getStep();

                if (step < 1 || step > EventVariables.HORSE_MAX_STEP)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid step");
                else if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid race ID");
                else if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid horse Idx");
                else if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid horse ID");
                else if (inMessage.getCurrentPosition() < 0
                        || inMessage.getCurrentStep() < 0)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid horse position and/or step");
                else {

                    ((HorseInt) Thread.currentThread()).setRaceID(
                            inMessage.getRaceID());
                    ((HorseInt) Thread.currentThread()).setRaceIdx(
                            inMessage.getRaceIdx());
                    ((HorseInt) Thread.currentThread()).setID(
                            inMessage.getEntityId());
                    ((HorseInt) Thread.currentThread()).setCurrentPosition(
                            inMessage.getCurrentPosition());
                    ((HorseInt) Thread.currentThread()).setCurrentStep(
                            inMessage.getCurrentStep());

                    rtStates[1 + inMessage.getEntityId()].enterMonitor();

                    if (rtStates[1 + inMessage.getEntityId()].getmType() ==
                            RacingTrackMessageTypes.HAS_FINISH_LINE_BEEN_CROSSED) {
                        racingTrack.makeAMove(step);

                        // Update racing track status file
                        rtStates[1 + inMessage.getEntityId()].setmType(
                                RacingTrackMessageTypes.MAKE_A_MOVE);
                        updateStatusFile(1 + inMessage.getEntityId());
                    }

                    rtStates[1 + inMessage.getEntityId()].exitMonitor();

                    reply = new RacingTrackMessage(
                            RacingTrackMessageTypes.MAKE_A_MOVE, inMessage.getEntityId());
                }
                break;

            case PROCEED_TO_START_LINE:
                if (inMessage.getRaceID() < 0 ||
                        inMessage.getRaceID() >= EventVariables.NUMBER_OF_RACES)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid race ID");
                else if (inMessage.getRaceIdx() < 0 ||
                        inMessage.getRaceIdx() >= EventVariables.NUMBER_OF_HORSES_PER_RACE)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid horse Idx");
                else if (inMessage.getEntityId() < 0 ||
                        inMessage.getEntityId() >= EventVariables.NUMBER_OF_HORSES)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid horse ID");
                else if (inMessage.getCurrentPosition() < 0
                        || inMessage.getCurrentStep() < 0)
                    reply = new RacingTrackMessage(
                            inMessage, "Invalid horse position and/or step");
                else {

                    ((HorseInt) Thread.currentThread()).setRaceID(
                            inMessage.getRaceID());
                    ((HorseInt) Thread.currentThread()).setRaceIdx(
                            inMessage.getRaceIdx());
                    ((HorseInt) Thread.currentThread()).setID(
                            inMessage.getEntityId());
                    ((HorseInt) Thread.currentThread()).setCurrentPosition(
                            inMessage.getCurrentPosition());
                    ((HorseInt) Thread.currentThread()).setCurrentStep(
                            inMessage.getCurrentStep());

                    rtStates[1 + inMessage.getEntityId()].enterMonitor();

                    if (rtStates[1 + inMessage.getEntityId()].getmType() == null) {

                        racingTrack.proceedToStartLine();

                        // Update racing track status file
                        rtStates[1 + inMessage.getEntityId()].setmType(
                                RacingTrackMessageTypes.PROCEED_TO_START_LINE);
                        updateStatusFile(1 + inMessage.getEntityId());

                    }

                    rtStates[1 + inMessage.getEntityId()].exitMonitor();

                    reply = new RacingTrackMessage(
                            RacingTrackMessageTypes.PROCEED_TO_START_LINE,
                            inMessage.getEntityId());
                }
                break;

            case START_THE_RACE:
                rtStates[0].enterMonitor();

                if (rtStates[0].getmType() == null || (rtStates[0].getmType() ==
                        RacingTrackMessageTypes.START_THE_RACE &&
                        rtStates[0].getRaceNumber() + 1 == inMessage.getRaceID())) {

                    racingTrack.startTheRace();

                    // Update racing track status file
                    rtStates[0].setmType(
                            RacingTrackMessageTypes.START_THE_RACE);
                    rtStates[0].setRaceNumber(inMessage.getRaceID());
                    updateStatusFile(0);
                }
                rtStates[0].exitMonitor();

                reply = new RacingTrackMessage(
                        RacingTrackMessageTypes.START_THE_RACE,
                        inMessage.getEntityId());
                break;

            default:
                reply = new RacingTrackMessage(
                        inMessage, "Invalid message type");
                break;
        }

        return reply;
    }

    /**
     * Method that returns the counter that registers the number of Horse/Jockey
     * pairs that have already invoked the HAS_FINISH_LINE_BEEN_CROSSED method.
     * @return The counter that registers the number of Horse/Jockey pairs
     * that have already invoked the HAS_FINISH_LINE_BEEN_CROSSED method.
     */
    public int getRequests() {
        return Arrays.stream(rtStates).mapToInt(x -> x.getRequests()).sum();
    }
}
