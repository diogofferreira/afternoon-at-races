package sharedRegions;

import interfaces.BettingCentreInt;
import interfaces.GeneralRepositoryInt;
import interfaces.StableInt;
import main.EventVariables;
import registries.RegPlaceABet;
import states.BrokerState;
import states.SpectatorState;
import utils.Bet;
import utils.BetState;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The Betting Centre is a shared region where all the bets are registered,
 * verified and honoured.
 */
public class BettingCentre implements BettingCentreInt {

    /**
     * Instance of a monitor.
     */
    private Lock mutex;

    /**
     * Condition variable where the Broker will wait for the Spectators' bets.
     */
    private Condition waitingForBet;

    /**
     * Condition variable where the Spectators will wait for the Broker to
     * validate their bets.
     */
    private Condition waitingForValidation;

    /**
     * Condition variable where the Broker will wait for the Spectators that
     * won their bets and requested their gains.
     */
    private Condition waitingForHonours;

    /**
     * Condition variable where the Spectators will wait for the Broker to
     * honour their bets.
     */
    private Condition waitingForCash;

    /**
     * Identifier of the current race.
     */
    private int currentRaceID;

    /**
     * Array that stores the odds of each one of the Horses participating in the
     * current race, indexed by their raceIdx.
     */
    private double[] raceOdds;

    /**
     * Flag that signals if the Broker is accepting bets.
     */
    private boolean acceptingBets;

    /**
     * Flag that signals if the Broker is honouring bets.
     */
    private boolean acceptingHonours;

    /**
     * Queue that stores pending bets.
     */
    private Queue<Bet> pendingBets;

    /**
     * Queue that stores bets that were already validated by the Broker.
     * It stores both accepted and rejected bets.
     */
    private Queue<Bet> validatedBets;

    /**
     * Array that stores the horseIdx that won the race.
     */
    private int[] winners;

    /**
     * List that stores the winning bets.
     */
    private List<Bet> winningBets;

    /**
     * Queue that stores the Spectators' Ids of the ones who are waiting for
     * the Broker to honour their bets.
     */
    private Queue<Integer> pendingHonours;

    /**
     * HashMap that stores the value that will be paid to the Spectators,
     * mapped by their ID.
     */
    private HashMap<Integer, Integer> validatedHonours;

    /**
     * Instance of the shared region Stable.
     */
    private StableInt stable;

    /**
     * Instance of the shared region General Repository.
     */
    private GeneralRepositoryInt generalRepository;

    /**
     * Creates a new instance of Betting Centre.
     * @param generalRepository Reference to an instance of the shared region
     *                          General Repository.
     * @param stable Reference to an instance of the shared region Stable.
     */
    public BettingCentre(GeneralRepositoryInt generalRepository, StableInt stable) {
        if (generalRepository == null)
            throw new IllegalArgumentException("Invalid General Repository.");
        if (stable == null)
            throw new IllegalArgumentException("Invalid Stable.");

        this.mutex = new ReentrantLock();
        this.waitingForBet = this.mutex.newCondition();
        this.waitingForValidation = this.mutex.newCondition();
        this.waitingForHonours = this.mutex.newCondition();
        this.waitingForCash = this.mutex.newCondition();

        this.acceptingBets = false;
        this.acceptingHonours = false;

        this.pendingBets = new LinkedList<>();
        this.validatedBets = new LinkedList<>();
        this.pendingHonours = new LinkedList<>();
        this.validatedHonours = new HashMap<>();

        this.generalRepository = generalRepository;
        this.stable = stable;
    }

    /**
     * Method that validates the next pending bet (if it exists) on the
     * pending bets queue.
     */
    private void validatePendingBets() {
        // validate pending FIFO's bets
        if (pendingBets.size() > 0) {
            Bet bet = pendingBets.poll();

            // Considering the bet value is valid since spectator cannot bet
            // over a certain amount
            if (bet.getHorseIdx() >= 0 &&
                    bet.getHorseIdx() < EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                bet.setState(BetState.ACCEPTED);
                validatedBets.add(bet);
                try {
                    generalRepository.setSpectatorsBet(bet.getSpectatorID(),
                            bet.getValue(), bet.getHorseIdx());
                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            } else
                bet.setState(BetState.REJECTED);
        }
    }

    /**
     * Method that generates a new instance of Bet.
     * @param spectatorID The ID of the Spectator performing the bet.
     * @param strategy The betting strategy used by that Spectator.
     * @param wallet The current amount of money in the Spectator's wallet.
     * @return Instance of a newly generated Bet.
     */
    private Bet getBet(int spectatorID, int strategy, int wallet) {
        int betValue = 0;
        int bettedHorse = 0;

        Random rnd = ThreadLocalRandom.current();

        switch (strategy) {
            // Half of money in smallest odd
            case 0:
                // pick random horse to bet
                try {
                    bettedHorse = IntStream.range(0, raceOdds.length)
                            .reduce((o1, o2) -> raceOdds[o1] < raceOdds[o2] ?
                                    o1 : o2).getAsInt();
                } catch (NoSuchElementException e) {
                    bettedHorse = rnd.nextInt(
                            EventVariables.NUMBER_OF_HORSES_PER_RACE);
                }

                // pick a random bet value, with a max of (wallet * number_of_races)
                // to avoid bankruptcy
                betValue = wallet / 2;

                break;

            // number of races of money in smallest odd
            case 1:
                // pick random horse to bet
                try {
                    bettedHorse = IntStream.range(0, raceOdds.length)
                            .reduce((o1, o2) -> raceOdds[o1] < raceOdds[o2] ?
                                    o1 : o2).getAsInt();
                } catch (NoSuchElementException e) {
                    bettedHorse = rnd.nextInt(
                            EventVariables.NUMBER_OF_HORSES_PER_RACE);
                }

                // pick a random bet value, with a max of (wallet * number_of_races)
                // to avoid bankruptcy
                betValue = rnd.nextInt(
                        (wallet / EventVariables.NUMBER_OF_RACES - 1)) + 1;

                break;

            // number of races of money in biggest odd
            case 2:
                // pick random horse to bet
                try {
                    bettedHorse = IntStream.range(0, raceOdds.length)
                            .reduce((o1, o2) -> raceOdds[o1] > raceOdds[o2] ?
                                    o1 : o2).getAsInt();
                } catch (NoSuchElementException e) {
                    bettedHorse = rnd.nextInt(
                            EventVariables.NUMBER_OF_HORSES_PER_RACE);
                }

                // pick a random bet value, with a max of (wallet * number_of_races)
                // to avoid bankruptcy
                betValue = rnd.nextInt(
                        (wallet / EventVariables.NUMBER_OF_RACES - 1)) + 1;

                break;

            default:
                // pick random horse to bet
                bettedHorse = rnd.nextInt(EventVariables.NUMBER_OF_HORSES_PER_RACE);

                // pick a random bet value, with a max of (wallet * number_of_races)
                // to avoid bankruptcy
                betValue = rnd.nextInt(
                        (wallet / EventVariables.NUMBER_OF_RACES - 1)) + 1;

                break;
        }

        return new Bet(spectatorID, bettedHorse, betValue);
    }

    /**
     * Method invoked by the Broker.
     * The broker changes its state to WAITING_FOR_BETS, signals the Spectators
     * that is accepting bets.
     * It blocks each time it validates a new bet and wakes up the Spectators
     * that still have pending bets.
     * @param raceID The current raceID.
     */
    @Override
    public void acceptTheBets(int raceID) {
        mutex.lock();

        try {
            generalRepository.setBrokerState(BrokerState.WAITING_FOR_BETS);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // clear accepted and rejected bets list
        validatedBets.clear();

        // clear betting queues
        pendingHonours.clear();
        validatedHonours.clear();

        // Update raceID and start accepting bets
        currentRaceID = raceID;
        try {
            raceOdds = stable.getRaceOdds(currentRaceID);
        } catch (RemoteException e) {
            System.out.println("Stable remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // Signals spectators that now broker is accepting bets
        acceptingBets = true;

        // broker wait
        while (true) {

            // validate all pending bet
            validatePendingBets();

            // notify spectators
            waitingForValidation.signalAll();

            if (validatedBets.size() == EventVariables.NUMBER_OF_SPECTATORS)
                break;

            try {
                waitingForBet.await();
            } catch (InterruptedException ignored){}
        }

        acceptingBets = false;

        mutex.unlock();
    }

    /**
     * Method invoked by each one of the Spectators to place their bet, effectively
     * changing their state to PLACING_A_BET.
     * They blocked in queue waiting for the Broker to validate their bet.
     * If their bet is not accepted a new bet is generated.
     * @param spectatorID ID of the Spectator placing a bet.
     * @param strategy Betting strategy used by the Spectator.
     * @param wallet Amount in Spectator's wallet.
     * @return A registry which contains the Horse's index on the current race
     * that the Spectator bet on and the updated value of his/her wallet after
     * placing the bet.
     */
    @Override
    public RegPlaceABet placeABet(int spectatorID, int strategy, int wallet) {
        Bet bet;
        RegPlaceABet reg;

        mutex.lock();

        try {
            generalRepository.setSpectatorState(spectatorID,
                    SpectatorState.PLACING_A_BET);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // If it's still not accepting bets, wait
        while (!acceptingBets) {
            try {
                waitingForValidation.await();
            } catch (InterruptedException ignored){}
        }

        // add to waiting bets queue
        bet = getBet(spectatorID, strategy, wallet);
        pendingBets.add(bet);

        // spectator wait
        while (true) {
            // notify broker
            waitingForBet.signal();

            if (validatedBets.contains(bet))
                break;

            if (bet.getState() == BetState.REJECTED) {
                bet = getBet(spectatorID, strategy, wallet);
                pendingBets.add(bet);
                waitingForBet.signal();
            }

            try {
                waitingForValidation.await();
            } catch (InterruptedException ignored){}
        }

        reg = new RegPlaceABet(bet.getHorseIdx(), wallet-bet.getValue());

        mutex.unlock();

        return reg;
    }

    /**
     * Method invoked by the Broker to check if there are any winning bets.
     * @param winners An array of horseIdxs that contains the race winners.
     * @return True if there are any winners, false otherwise.
     */
    @Override
    public boolean areThereAnyWinners(int[] winners) {
        boolean areThereWinners;

        mutex.lock();

        // save horses winners
        this.winners = winners;

        // create queue for the winning bets
        winningBets = validatedBets.stream().filter(bet ->
                IntStream.of(winners).anyMatch(x -> x == bet.getHorseIdx()))
                .collect(Collectors.toList());

        areThereWinners = !winningBets.isEmpty();

        mutex.unlock();

        return areThereWinners;
    }

    /**
     * Method invoked by the Broker if there were any winning bets.
     * The Broker changes its state to SETTLING_ACCOUNTS and signals the
     * Spectators waiting for collecting their gains that it's open for settling
     * accounts.
     * He blocks each time he pays a winning bet and wakes up Spectators still
     * waiting to collect their rewards.
     */
    @Override
    public void honourTheBets() {
        Bet bet;
        mutex.lock();

        try {
            generalRepository.setBrokerState(BrokerState.SETTLING_ACCOUNTS);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        }

        acceptingHonours = true;

        while (true) {

            waitingForCash.signalAll();

            if (validatedHonours.size() >= winningBets.size()) break;

            try {
                waitingForHonours.await();
            } catch (InterruptedException ignored) {}

            if (pendingHonours.size() > 0) {
                int spectatorID = pendingHonours.poll();

                try {
                    bet = (Bet) winningBets.stream().filter(
                            bt -> bt.getSpectatorID() == spectatorID).toArray()[0];

                    validatedHonours.put(spectatorID,
                            (int)(bet.getValue() * raceOdds[bet.getHorseIdx()]));
                    generalRepository.setSpectatorGains(spectatorID,
                            (int)(bet.getValue() * raceOdds[bet.getHorseIdx()]));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    System.exit(1);
                } catch (RemoteException e) {
                    System.out.println("GeneralRepository remote invocation exception: "
                            + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }

            }
        }

        acceptingHonours = false;

        mutex.unlock();
    }

    /**
     * Method invoked by each one of the winning Spectators.
     * They change their state to COLLECTING_THE_GAINS and block in queue waiting
     * for their rewards.
     * @param spectatorID ID of the Spectator that had a winning bet and now
     *                    requests his/her gains.
     * @return The value the Spectator won.
     */
    @Override
    public double goCollectTheGains(int spectatorID) {
        double winningValue;

        mutex.lock();

        try {
            generalRepository.setSpectatorState(spectatorID,
                    SpectatorState.COLLECTING_THE_GAINS);
        } catch (RemoteException e) {
            System.out.println("GeneralRepository remote invocation exception: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        while (!acceptingHonours) {
            try {
                waitingForCash.await();
            } catch (InterruptedException ignored) {}
        }

        // add to pending collections queue
        pendingHonours.add(spectatorID);

        // spectator waits queue
        while (true) {
            // notify broker queue
            waitingForHonours.signalAll();

            if (!pendingHonours.contains(spectatorID)) break;

            try {
                waitingForCash.await();
            } catch (InterruptedException ignored) {}

        }

        // get winning value
        winningValue = 0;
        if (validatedHonours.containsKey(spectatorID))
            winningValue = validatedHonours.get(spectatorID) / winners.length;

        mutex.unlock();

        return winningValue;
    }
}
