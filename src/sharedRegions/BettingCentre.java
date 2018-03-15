package sharedRegions;

import entities.Broker;
import entities.Spectator;
import main.EventVariables;
import states.BrokerState;
import states.SpectatorState;
import utils.Bet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BettingCentre {

    private Lock mutex;
    private Condition waitingForBet, waitingForValidation, waitingForHonours, waitingForCash;
    private Stable stable;
    private RacingTrack racingTrack;

    private ConcurrentHashMap<Integer, Double> raceOdds;

    // queue with pending bets
    // queue with accepted bets
    private ConcurrentLinkedQueue<Bet> pendingBets, acceptedBets, rejectedBets;

    private int currentRaceID;
    private int[] winners;
    private List<Bet> winningBets;

    // queue with pending honours
    // queue with accepted honours
    private ConcurrentLinkedQueue<Integer> pendingHonours;
    private ConcurrentHashMap<Integer, Double> acceptedHonours;

    public BettingCentre(Stable s, RacingTrack r) {
        if (s == null)
            throw new IllegalArgumentException("Invalid Stable.");
        if (r == null)
            throw new IllegalArgumentException("Invalid Racing Track.");

        this.mutex = new ReentrantLock();
        this.waitingForBet = this.mutex.newCondition();
        this.waitingForValidation = this.mutex.newCondition();
        this.waitingForHonours = this.mutex.newCondition();
        this.waitingForCash = this.mutex.newCondition();

        this.raceOdds = new ConcurrentHashMap<>();
        this.pendingBets = new ConcurrentLinkedQueue<>();
        this.acceptedBets = new ConcurrentLinkedQueue<>();
        this.rejectedBets = new ConcurrentLinkedQueue<>();
        this.pendingHonours = new ConcurrentLinkedQueue<>();
        this.acceptedHonours = new ConcurrentHashMap<>();
        this.stable = s;
        this.racingTrack = r;
    }

    private void getRaceOdds() {
        double oddSum = 0.0;
        int[][] horsesAgility = stable.getHorsesAgility();

        for (int i = 0; i < horsesAgility[currentRaceID].length; i++)
            oddSum += horsesAgility[currentRaceID][i];

        // clear odds registry
        raceOdds.clear();

        for (int i = 0; i < horsesAgility[currentRaceID].length; i++)
            raceOdds.put(i, horsesAgility[currentRaceID][i] / oddSum);
    }

    private void validatePendingBets() {
        // validate pending FIFO's bets */
        while (pendingBets.size() > 0) {
            Bet bet = pendingBets.peek();
            pendingBets.remove(bet);

            // Considering the bet value is valid since spectator cannot bet over a certain amount
            if (bet.getHorseID() < EventVariables.NUMBER_OF_HORSES_PER_RACE)
                rejectedBets.add(bet);
            else
                acceptedBets.add(bet);
        }
    }

    public void acceptTheBets(int raceID) {
        Broker b;
        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.WAITING_FOR_BETS);

        // Update raceID and start accepting bets
        currentRaceID = raceID;
        getRaceOdds();

        // broker wait */
        while (acceptedBets.size() < EventVariables.NUMBER_OF_SPECTATORS) {
            try {
                waitingForBet.await();
            } catch (InterruptedException ignored){}

            // validate all pending bet
            validatePendingBets();

            // notify spectator
            waitingForValidation.signalAll();
        }

        mutex.unlock();
    }

    public boolean placeABet(Bet bet) {
        Spectator s;
        boolean validBet;

        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.PLACING_A_BET);

        // add to waiting bets queue
        pendingBets.add(bet);

        // notify broker
        waitingForBet.signal();

        // spectator wait
        while (!(acceptedBets.contains(bet) || rejectedBets.contains(bet))) {
            try {
                waitingForValidation.await();
            } catch (InterruptedException ignored){}
        }

        validBet = acceptedBets.contains(bet);

        mutex.unlock();

        return validBet;
    }

    public boolean areThereAnyWinners() {
        Broker b;
        boolean areThereWinners;

        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);

        // save horses winners
        winners = racingTrack.getWinners();

        // create queue for the winning bets
        winningBets = acceptedBets.stream().filter(bet ->
                IntStream.of(winners).anyMatch(x -> x == bet.getHorseID()))
                .collect(Collectors.toList());

        // clear accepted and rejected bets list
        acceptedBets.clear();
        rejectedBets.clear();

        areThereWinners = !winningBets.isEmpty();

        mutex.unlock();

        return areThereWinners;
    }

    public void honourTheBets() {
        Broker b;
        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.SETTLING_ACCOUNTS);

        while (acceptedHonours.size() < winningBets.size()) {
            try {
                waitingForHonours.await();
            } catch (InterruptedException ignored) {}

            while (pendingHonours.size() > 0) {
                int spectatorID = pendingHonours.peek();
                Bet bet = (Bet) winningBets.stream().filter(
                        bt -> bt.getSpectatorID() == spectatorID).toArray()[0];

                acceptedHonours.put(spectatorID,
                        bet.getValue() * raceOdds.get(bet.getHorseID()));
            }

        }

        mutex.unlock();
    }

    public double goCollectTheGains(int spectatorID) {
        Spectator s;
        double winningValue;

        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.COLLECT_THE_GAINS);

        // add to pending collections queue
        pendingHonours.add(spectatorID);

        // notify broker queue
        waitingForHonours.signal();

        // spectator waits queue
        while (!acceptedHonours.containsKey(spectatorID)) {
            try {
                waitingForCash.await();
            } catch (InterruptedException ignored) {}
        }

        // get winning value
        winningValue = acceptedHonours.get(spectatorID);
        // clean honour entry
        acceptedHonours.remove(spectatorID);

        mutex.unlock();

        return winningValue;
    }
}
