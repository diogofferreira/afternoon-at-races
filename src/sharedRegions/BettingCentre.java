package sharedRegions;

import entities.Broker;
import entities.Spectator;
import main.EventVariables;
import states.BrokerState;
import states.SpectatorState;
import utils.Bet;
import utils.BetState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BettingCentre {

    private Lock mutex;
    private Condition waitingForBet;
    private Condition waitingForValidation;
    private Condition waitingForHonours;
    private Condition waitingForCash;
    private Stable stable;
    private GeneralRepository generalRepository;

    private int[][] horsesAgility;
    private int[] raceOdds;

    private boolean acceptingBets;
    private boolean acceptingHonours;

    // queue with pending bets
    // queue with accepted bets
    private Queue<Bet> pendingBets;
    private Queue<Bet> validatedBets;

    private int currentRaceID;
    private int[] winners;
    private List<Bet> winningBets;

    // queue with pending honours
    // queue with accepted honours
    private Queue<Integer> pendingHonours;
    private HashMap<Integer, Integer> validatedHonours;

    public BettingCentre(GeneralRepository generalRepository, Stable stable) {
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

    private void getRaceOdds() {
        int oddSum;

        raceOdds = new int[EventVariables.NUMBER_OF_HORSES_PER_RACE];

        if (horsesAgility == null)
            horsesAgility = stable.getHorsesAgility();

        oddSum = Arrays.stream(horsesAgility[currentRaceID]).reduce(
                Integer::sum).getAsInt();

        for (int i = 0; i < horsesAgility[currentRaceID].length; i++) {
            raceOdds[i] = oddSum / horsesAgility[currentRaceID][i];
        }

        generalRepository.setHorsesOdd(raceOdds);
    }

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
                generalRepository.setSpectatorsBet(bet.getSpectatorID(),
                        bet.getValue(), bet.getHorseIdx());
            } else
                bet.setState(BetState.REJECTED);
        }
    }

    private Bet getBet(int spectatorID, int strategy, int wallet) {
        int betValue = 0;
        int bettedHorse = 0;

        Random rnd = ThreadLocalRandom.current();

        switch (strategy) {
            // Half of money in smallest odd
            case 0:
                // pick random horse to bet
                try {
                    bettedHorse = IntStream.range(0, raceOdds.length).reduce(
                            (o1, o2) -> raceOdds[o1] < raceOdds[o2] ? o1 : o2).getAsInt();
                } catch (NoSuchElementException e) {
                    bettedHorse = rnd.nextInt(EventVariables.NUMBER_OF_HORSES_PER_RACE);
                }

                // pick a random bet value, with a max of (wallet * number_of_races)
                // to avoid bankruptcy
                betValue = wallet / 2;

                break;

            // number of races of money in smallest odd
            case 1:
                // pick random horse to bet
                try {
                    bettedHorse = IntStream.range(0, raceOdds.length).reduce(
                            (o1, o2) -> raceOdds[o1] < raceOdds[o2] ? o1 : o2).getAsInt();
                } catch (NoSuchElementException e) {
                    bettedHorse = rnd.nextInt(EventVariables.NUMBER_OF_HORSES_PER_RACE);
                }

                // pick a random bet value, with a max of (wallet * number_of_races)
                // to avoid bankruptcy
                betValue = rnd.nextInt((wallet / EventVariables.NUMBER_OF_RACES - 1)) + 1;

                break;

            // number of races of money in biggest odd
            case 2:
                // pick random horse to bet
                try {
                    bettedHorse = IntStream.range(0, raceOdds.length).reduce(
                            (o1, o2) -> raceOdds[o1] > raceOdds[o2] ? o1 : o2).getAsInt();
                } catch (NoSuchElementException e) {
                    bettedHorse = rnd.nextInt(EventVariables.NUMBER_OF_HORSES_PER_RACE);
                }

                // pick a random bet value, with a max of (wallet * number_of_races)
                // to avoid bankruptcy
                betValue = rnd.nextInt((wallet / EventVariables.NUMBER_OF_RACES - 1)) + 1;

                break;

            default:
                // pick random horse to bet
                bettedHorse = rnd.nextInt(EventVariables.NUMBER_OF_HORSES_PER_RACE);

                // pick a random bet value, with a max of (wallet * number_of_races)
                // to avoid bankruptcy
                betValue = rnd.nextInt((wallet / EventVariables.NUMBER_OF_RACES - 1)) + 1;

                break;
        }

        return new Bet(spectatorID, bettedHorse, betValue);
    }

    public void acceptTheBets(int raceID) {
        Broker b;
        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.WAITING_FOR_BETS);
        generalRepository.setBrokerState(BrokerState.WAITING_FOR_BETS);

        // clear accepted and rejected bets list
        validatedBets.clear();

        // clear betting queues
        pendingHonours.clear();
        validatedHonours.clear();

        // Update raceID and start accepting bets
        currentRaceID = raceID;
        getRaceOdds();

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

    public int placeABet() {
        Spectator s;
        Bet bet;

        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.PLACING_A_BET);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.PLACING_A_BET);

        // If it's still not accepting bets, wait
        while (!acceptingBets) {
            try {
                waitingForValidation.await();
            } catch (InterruptedException ignored){}
        }

        // add to waiting bets queue
        bet = getBet(s.getID(), s.getStrategy(), s.getWallet());
        pendingBets.add(bet);

        // spectator wait
        while (true) {
            // notify broker
            waitingForBet.signal();

            if (validatedBets.contains(bet))
                break;

            if (bet.getState() == BetState.REJECTED) {
                bet = getBet(s.getID(), s.getStrategy(), s.getWallet());
                pendingBets.add(bet);
                waitingForBet.signal();
            }

            try {
                waitingForValidation.await();
            } catch (InterruptedException ignored){}
        }

        s.updateWallet(-bet.getValue());

        mutex.unlock();

        return bet.getHorseIdx();
    }

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

    public void honourTheBets() {
        Broker b;
        Bet bet;
        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.SETTLING_ACCOUNTS);
        generalRepository.setBrokerState(BrokerState.SETTLING_ACCOUNTS);

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
                            bet.getValue() * raceOdds[bet.getHorseIdx()]);
                    generalRepository.setSpectatorGains(spectatorID,
                            bet.getValue() * raceOdds[bet.getHorseIdx()]);
                } catch (ArrayIndexOutOfBoundsException e) { }
            }
        }

        acceptingHonours = false;

        mutex.unlock();
    }

    public double goCollectTheGains(int spectatorID) {
        Spectator s;
        double winningValue;

        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.COLLECTING_THE_GAINS);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.COLLECTING_THE_GAINS);

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
