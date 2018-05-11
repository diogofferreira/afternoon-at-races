package entities;

import interfaces.BettingCentreInt;
import interfaces.ControlCentreInt;
import interfaces.RacingTrackInt;
import interfaces.StableInt;
import main.EventVariables;
import sharedRegions.BettingCentre;
import sharedRegions.ControlCentre;
import sharedRegions.RacingTrack;
import sharedRegions.Stable;
import states.BrokerState;

import java.rmi.RemoteException;

/**
 * The Broker is the entity that controls the event, regulates the bets
 * and supervises the races.
 */
public class Broker extends Thread {
    /**
     * Current state of the broker lifecycle.
     */
    private BrokerState state;

    /**
     * Instance of the shared region Stable.
     */
    private StableInt stable;

    /**
     * Instance of the shared region Racing Track.
     */
    private RacingTrackInt racingTrack;

    /**
     * Instance of the shared region Control Centre.
     */
    private ControlCentreInt controlCentre;

    /**
     * Instance of the shared region Betting Centre.
     */
    private BettingCentreInt bettingCentre;

    /**
     * Creates a new instance of Broker.
     * @param stable Reference to an instance of the shared region Stable.
     * @param racingTrack Reference to an instance of the shared region
     *                    Racing Track.
     * @param controlCentre Reference to an instance of the shared region
     *                     Control Centre.
     * @param bettingCentre Reference to an instance of the shared region
     *                      Betting Centre.
     */
    public Broker(StableInt stable, RacingTrackInt racingTrack,
                  ControlCentreInt controlCentre, BettingCentreInt bettingCentre) {
        if (stable == null || racingTrack == null ||
                controlCentre == null || bettingCentre == null)
            throw new IllegalArgumentException("Invalid shared region reference.");

        this.state = BrokerState.OPENING_THE_EVENT;
        this.stable = stable;
        this.controlCentre = controlCentre;
        this.bettingCentre = bettingCentre;
        this.racingTrack = racingTrack;
    }

    /**
     * Broker lifecycle.
     */
    public void run() {
        int[] winners = null;

        try {
            controlCentre.openTheEvent();
        } catch (RemoteException e) {
            System.out.println("ControlCentre remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        state = BrokerState.OPENING_THE_EVENT;

        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++) {
            // summonHorsesToPaddock
            try {
                controlCentre.summonHorsesToPaddock(i);
            } catch (RemoteException e) {
                System.out.println("ControlCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            state = BrokerState.ANNOUNCING_NEXT_RACE;


            // acceptsBets
            try {
                bettingCentre.acceptTheBets(i);
            } catch (RemoteException e) {
                System.out.println("BettingCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            state = BrokerState.WAITING_FOR_BETS;

            // startTheRace
            try {
                racingTrack.startTheRace();
            } catch (RemoteException e) {
                System.out.println("RacingTrack remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            try {
                controlCentre.startTheRace();
            } catch (RemoteException e) {
                System.out.println("ControlCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            state = BrokerState.SUPERVISING_THE_RACE;

            // reportResults
            try {
                winners = controlCentre.reportResults();
            } catch (RemoteException e) {
                System.out.println("ControlCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }

            // if there are any winners, honour those bets
            try {
                if (bettingCentre.areThereAnyWinners(winners)) {
                    bettingCentre.honourTheBets();
                    state = BrokerState.SETTLING_ACCOUNTS;
                }
            } catch (RemoteException e) {
                System.out.println("BettingCentre remote invocation exception: "
                        + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }

        try {
            controlCentre.celebrate();
        } catch (RemoteException e) {
            System.out.println("ControlCentre remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        state = BrokerState.PLAYING_HOST_AT_THE_BAR;
        try {
            stable.entertainTheGuests();
        } catch (RemoteException e) {
            System.out.println("Stable remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Method that returns the current Broker state.
     * @return Current Broker state.
     */
    public BrokerState getBrokerState() {
        return this.state;
    }

    /**
     * Updates the current Broker state.
     * @param state The new Broker state.
     */
    public void setBrokerState(BrokerState state) {
        this.state = state;
    }
}
