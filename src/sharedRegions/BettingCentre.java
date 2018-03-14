package sharedRegions;

import main.EventVariables;
import utils.Bet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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
    private List<Integer> winners;
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
        Map<Integer, Integer> horsesAgility = stable.getHorsesAgility();

        for (Integer horseID : stable.getRaceLineups().get(currentRaceID))
            oddSum += horsesAgility.get(horseID);

        // clear odds registry
        raceOdds.clear();

        for (Integer horseID : stable.getRaceLineups().get(currentRaceID))
            raceOdds.put(horseID, oddSum / horsesAgility.get(horseID));
    }

    public void acceptTheBets(int raceID) {
        mutex.lock();

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

    private void validatePendingBets() {

        // validate pending FIFO's bets */

        while (pendingBets.size() > 0) {
            Bet bet = pendingBets.peek();
            pendingBets.remove(bet);

            // Considering the bet value is valid since spectator cannot bet over a certain amount
            if (!stable.getRaceLineups().get(currentRaceID).contains(bet.getHorseID()))
                rejectedBets.add(bet);
            else
                acceptedBets.add(bet);
        }
    }

    public boolean placeABet(Bet bet) {
        boolean validBet;

        mutex.lock();

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
        boolean areThereWinners;

        mutex.lock();

        // save horses winners
        winners = racingTrack.getWinners();

        // create queue for the winning bets
        winningBets = acceptedBets.stream().filter(b -> winners.contains(
                b.getHorseID())).collect(Collectors.toList());

        // clear accepted and rejected bets list
        acceptedBets.clear();
        rejectedBets.clear();

        areThereWinners = !winningBets.isEmpty();

        mutex.unlock();

        return areThereWinners;
    }

    public void honourTheBets() {
        mutex.lock();

        while (acceptedHonours.size() < winningBets.size()) {
            try {
                waitingForHonours.await();
            } catch (InterruptedException ignored) {}

            while (pendingHonours.size() > 0) {
                int spectatorID
                        = pendingHonours.poll();
                Bet bet = (Bet) winningBets.stream().filter(
                        b -> b.getSpectatorID() == spectatorID).toArray()[0];

                acceptedHonours.put(spectatorID,
                        bet.getValue() * raceOdds.get(bet.getHorseID()));
            }

        }

        mutex.unlock();
    }

    public double goCollectTheGains(int spectatorID) {
        double winningValue;

        mutex.lock();

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
