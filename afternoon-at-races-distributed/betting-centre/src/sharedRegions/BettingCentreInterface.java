package sharedRegions;

import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.BettingCentreMessageTypes;
import messages.BettingCentreMessage;

/**
 * Interface of the Betting Centre server that processes the received messages,
 * communicating with shared region, and replies to the thread that requests
 * for the service (APS).
 */
public class BettingCentreInterface {

    /**
     * Counter that registers the number of Spectators that have already
     * collected their gains in the last race. It is useful to close the
     * server socket.
     */
    private int requests;

    /**
     * Current race identifier.
     */
    private int raceNumber;

    /**
     * Number of winning bets in the current race.
     */
    private int numberOfWinners;

    /**
     * Array of counters that register the number of Spectators that bet in
     * the Horse that is indexed by its race index.
     */
    private int[] bettedHorses;

    /**
     * Instance of the Betting Centre shared region.
     */
    private BettingCentre bettingCentre;

    /**
     * Creates a new instance of an interface of the Betting Centre shared region.
     * @param bettingCentre Instance of the Betting Centre shared region.
     */
    public BettingCentreInterface(BettingCentre bettingCentre) {
        if (bettingCentre == null)
            throw new IllegalArgumentException("Invalid Betting Centre.");

        this.bettingCentre = bettingCentre;
        this.requests = 0;
        this.raceNumber = -1;
        this.numberOfWinners = -1;
        this.bettedHorses = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];
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
    public BettingCentreMessage processAndReply(BettingCentreMessage inMessage) {
        BettingCentreMessageTypes mType;
        int raceID, spectatorID, horseIdx;
        int[] winners;
        boolean areThereAnyWinners;
        double amount;

        if ((mType = BettingCentreMessageTypes.getType(inMessage.getMethod())) == null)
            return new BettingCentreMessage(BettingCentreMessageTypes.ERROR);

        switch (mType) {
            case ACCEPT_THE_BETS:
                raceID = inMessage.getRaceId();
                raceNumber = raceID;

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES) {
                    inMessage.setErrorMessage("Invalid raceID");
                    inMessage.setMethod(BettingCentreMessageTypes.ERROR);
                    return inMessage;
                }

                bettingCentre.acceptTheBets(raceID);
                return new BettingCentreMessage(
                        BettingCentreMessageTypes.ACCEPT_THE_BETS,
                        raceID, inMessage.getEntityId());

            case ARE_THERE_ANY_WINNERS:
                winners = inMessage.getWinners();
                if (winners == null || winners.length == 0 ||
                        winners.length > EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                    inMessage.setErrorMessage("Invalid winners array");
                    inMessage.setMethod(BettingCentreMessageTypes.ERROR);
                    return inMessage;
                }

                if (raceNumber == EventVariables.NUMBER_OF_RACES - 1) {
                    numberOfWinners = 0;
                    // Set number of spectator winners
                    for (int winner : winners)
                        numberOfWinners += bettedHorses[winner];
                }

                areThereAnyWinners = bettingCentre.areThereAnyWinners(winners);
                return new BettingCentreMessage(
                        BettingCentreMessageTypes.ACCEPT_THE_BETS,
                        areThereAnyWinners, inMessage.getEntityId());

            case GO_COLLECT_THE_GAINS:
                spectatorID = inMessage.getEntityId();

                if (spectatorID < 0 ||
                        spectatorID >= EventVariables.NUMBER_OF_SPECTATORS) {
                    inMessage.setErrorMessage("Invalid spectatorID");
                    inMessage.setMethod(BettingCentreMessageTypes.ERROR);
                    return inMessage;
                }

                ((SpectatorInt) Thread.currentThread()).setID(spectatorID);

                amount = bettingCentre.goCollectTheGains();

                if (raceNumber == EventVariables.NUMBER_OF_RACES - 1)
                    requests++;

                return new BettingCentreMessage(
                        BettingCentreMessageTypes.GO_COLLECT_THE_GAINS,
                        amount, inMessage.getEntityId());

            case HONOUR_THE_BETS:
                bettingCentre.honourTheBets();
                return new BettingCentreMessage(
                        BettingCentreMessageTypes.HONOUR_THE_BETS,
                        inMessage.getEntityId());

            case PLACE_A_BET:
                spectatorID = inMessage.getEntityId();
                if (spectatorID < 0 ||
                        spectatorID >= EventVariables.NUMBER_OF_SPECTATORS) {
                    inMessage.setErrorMessage("Invalid spectator ID");
                    inMessage.setMethod(BettingCentreMessageTypes.ERROR);
                    return inMessage;
                }

                ((SpectatorInt) Thread.currentThread()).setID(spectatorID);
                ((SpectatorInt) Thread.currentThread()).setStrategy(inMessage.getStrategy());
                ((SpectatorInt) Thread.currentThread()).setWallet(inMessage.getWallet());

                horseIdx = bettingCentre.placeABet();

                // Set betted horse
                if (raceNumber == EventVariables.NUMBER_OF_RACES - 1)
                    bettedHorses[horseIdx] += 1;

                return new BettingCentreMessage(
                        BettingCentreMessageTypes.PLACE_A_BET, true,
                        ((SpectatorInt) Thread.currentThread()).getWallet(),
                        horseIdx, inMessage.getEntityId());

            default:
                return new BettingCentreMessage(BettingCentreMessageTypes.ERROR);
        }
    }

    /**
     * Method that returns the counter that registers the number of Spectators
     * that have already collected their gains in the last race.
     * @return The counter that registers the number of Spectators
     * that have already collected their gains in the last race.
     */
    public int getRequests() {
        return requests;
    }

    /**
     * Method that returns the number of winning bets in the current race.
     * @return The number of winning bets in the current race.
     */
    public int getNumberOfWinners() { return numberOfWinners; }
}
