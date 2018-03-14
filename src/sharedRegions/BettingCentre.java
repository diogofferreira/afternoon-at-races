package sharedRegions;

import entities.Horse;
import main.EventVariables;
import states.State;
import utils.Bet;
import utils.Racer;

import java.util.HashMap;
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

    /* FIFO with pending bets */
    /* FIFO with accepted bets */
    private ConcurrentLinkedQueue<Bet> pendingBets, acceptedBets, rejectedBets;

    private int currentRaceID;
    private List<Integer> winners;
    private List<Bet> winningBets;

    /* FIFO with pending collections */
    /* FIFO with accepted collections */
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
        HashMap<Integer, Integer> horseAgilities = stable.getHorseAgilities();

        for (Integer horseID : stable.getRaceLineups().get(currentRaceID))
            oddSum += horseAgilities.get(horseID);

        for (Integer horseID : stable.getRaceLineups().get(currentRaceID))
            raceOdds.put(horseID, oddSum / horseAgilities.get(horseID));
    }

    public void acceptTheBets(int raceID) {
        mutex.lock();

        // Update raceID and start accepting bets
        currentRaceID = raceID;
        getRaceOdds();

        /* broker wait */
        while (acceptedBets.size() < EventVariables.NUMBER_OF_SPECTATORS) {
            try {
                waitingForBet.await();
            } catch (InterruptedException ignored){}

            /* validate all pending bet */
            validatePendingBets();

            /* notify spectator */
            waitingForValidation.signalAll();
        }

        mutex.unlock();
    }

    public void validatePendingBets() {
        /* validate pending FIFO's bets */

        while (pendingBets.size() > 0) {
            Bet bet = pendingBets.peek();
            pendingBets.remove(bet);

            // Considering the bet value is valid since spectator cannot bet over a certain ammount
            if (!stable.getRaceLineups().get(currentRaceID).contains(bet.getHorseID()))
                rejectedBets.add(bet);
            else
                acceptedBets.add(bet);
        }
    }

    public boolean placeABet(Bet bet) {
        boolean validBet;

        mutex.lock();

        /* add to waiting FIFO */
        pendingBets.add(bet);

        /* notify broker */
        waitingForBet.signal();

        /* spectator wait */
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

        /* save horses winners */
        winners = racingTrack.getWinners();

        /* creates FIFO for the number of winning bets */
        winningBets = acceptedBets.stream().filter(b -> winners.contains(
                b.getHorseID())).collect(Collectors.toList());

        areThereWinners = !winningBets.isEmpty();
        mutex.unlock();

        return areThereWinners;
    }

    public void honourTheBets() {
        mutex.lock();

        while (acceptedHonours.size() < winningBets.size()) {
            try {
                waitingForHonours.await();
            } catch (InterruptedException e) {
            }


            while (pendingHonours.size() > 0) {
                int spectatorID
                        = pendingHonours.peek();
                Bet bet = (Bet) winningBets.stream().filter(
                        b -> b.getSpectatorID() == spectatorID).toArray()[0];

                acceptedHonours.put(spectatorID,
                        bet.getValue() * raceOdds.get(bet.getHorseID()));
            }

        }

        mutex.unlock();
    }

    public void goCollectTheGains(int spectatorID) {
        mutex.lock();

        /* add to pending collections FIFO */
        pendingHonours.add(spectatorID);

        /* notify broker */
        waitingForHonours.signal();

        /* spectator waits */
        while (!acceptedHonours.containsKey(spectatorID)) {
            try {
                waitingForCash.await();
            } catch (InterruptedException e) {
            }
        }

        mutex.unlock();
    }
}
