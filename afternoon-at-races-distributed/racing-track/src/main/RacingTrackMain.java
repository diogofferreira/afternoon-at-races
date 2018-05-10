package main;

import communication.HostsInfo;
import communication.RacingTrackAPS;
import communication.ServerCom;
import sharedRegions.RacingTrack;
import sharedRegions.RacingTrackInterface;
import stubs.ControlCentreStub;
import stubs.GeneralRepositoryStub;

/**
 * Main class that starts the Racing Track shared region server.
 */
public class RacingTrackMain {

    /**
     * Main method.
     * @param args Runtime arguments.
     */
    public static void main (String [] args) {
        RacingTrack racingTrack;
        RacingTrackInterface racingTrackInterface;
        RacingTrackAPS racingTrackAPS;

        GeneralRepositoryStub generalRepository;
        ControlCentreStub controlCentre;
        ServerCom scom, scomi;

        // shared regions stub initialization
        generalRepository = new GeneralRepositoryStub(
                HostsInfo.GENERAL_REPOSITORY_HOSTNAME,
                HostsInfo.GENERAL_REPOSITORY_PORT);
        controlCentre = new ControlCentreStub(
                HostsInfo.CONTROL_CENTRE_HOSTNAME,
                HostsInfo.CONTROL_CENTRE_PORT);

        /*generalRepository = new GeneralRepositoryStub("127.0.0.1",
                22401);
        controlCentre = new ControlCentreStub("127.0.0.1",
                22403);*/

        // service establishment
        scom = new ServerCom(HostsInfo.RACING_TRACK_PORT);
        scom.start();

        // shared region initialization
        racingTrack = new RacingTrack(generalRepository, controlCentre);
        racingTrackInterface = new RacingTrackInterface(racingTrack);

        // request processing
        while(racingTrackInterface.getRequests() != EventVariables.NUMBER_OF_HORSES) {
            scomi = scom.accept();
            if (scomi != null) {
                racingTrackAPS = new RacingTrackAPS(scomi, racingTrackInterface);
                racingTrackAPS.start();
            }
        }
    }
}