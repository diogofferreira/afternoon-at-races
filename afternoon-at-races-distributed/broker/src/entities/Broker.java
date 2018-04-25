package entities;

import main.EventVariables;
import states.BrokerState;
import stubs.BettingCentreStub;
import stubs.ControlCentreStub;
import stubs.RacingTrackStub;
import stubs.StableStub;

/**
 * The Broker is the entity that controls the event, regulates the bets
 * and supervises the races.
 */
public class Broker extends Thread implements BrokerInt {
    /**
     * Current state of the broker lifecycle.
     */
    private BrokerState state;

    /**
     * Instance of the shared region Stable.
     */
    private StableStub stable;

    /**
     * Instance of the shared region Racing Track.
     */
    private RacingTrackStub racingTrack;

    /**
     * Instance of the shared region Control Centre.
     */
    private ControlCentreStub controlCentre;

    /**
     * Instance of the shared region Betting Centre.
     */
    private BettingCentreStub bettingCentre;

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
    public Broker(StableStub stable, RacingTrackStub racingTrack,
                  ControlCentreStub controlCentre, BettingCentreStub bettingCentre) {
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
        int[] winners;

        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++) {
            // summonHorsesToPaddock
            System.out.println("SUMMON HORSES");
            controlCentre.summonHorsesToPaddock(i);
            System.out.println("ACCEPT THE BETS");
            // acceptsBets
            bettingCentre.acceptTheBets(i);
            System.out.println("START THE RACE");

            // startTheRace
            racingTrack.startTheRace();
            controlCentre.startTheRace();

            // reportResults
            winners = controlCentre.reportResults();

            // if there are any winners, honour those bets
            if (bettingCentre.areThereAnyWinners(winners))
                bettingCentre.honourTheBets();
        }

        controlCentre.celebrate();
        stable.entertainTheGuests();
    }

    /**
     * Method that returns the current Broker state.
     * @return Current Broker state.
     */
    @Override
    public BrokerState getBrokerState() {
        return this.state;
    }

    /**
     * Updates the current Broker state.
     * @param state The new Broker state.
     */
    @Override
    public void setBrokerState(BrokerState state) {
        this.state = state;
    }
}
