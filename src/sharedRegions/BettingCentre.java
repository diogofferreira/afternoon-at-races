package sharedRegions;

import entities.Broker;
import entities.Spectator;
import main.EventVariables;
import states.BrokerState;
import states.SpectatorState;
import utils.Bet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
    private GeneralRepository generalRepository;

    private double[] raceOdds;

    private boolean acceptingBets;
    private boolean acceptingHonours;

    // queue with pending bets
    // queue with accepted bets
    private Queue<Bet> pendingBets, acceptedBets, rejectedBets;

    private int currentRaceID;
    private int[] winners;
    private List<Bet> winningBets;

    // queue with pending honours
    // queue with accepted honours
    private Queue<Integer> pendingHonours;
    private HashMap<Integer, Double> acceptedHonours;

    public BettingCentre(GeneralRepository gr, Stable s, RacingTrack r) {
        if (gr == null)
            throw new IllegalArgumentException("Invalid General Repository.");
        if (s == null)
            throw new IllegalArgumentException("Invalid Stable.");
        if (r == null)
            throw new IllegalArgumentException("Invalid Racing Track.");

        this.mutex = new ReentrantLock();
        this.waitingForBet = this.mutex.newCondition();
        this.waitingForValidation = this.mutex.newCondition();
        this.waitingForHonours = this.mutex.newCondition();
        this.waitingForCash = this.mutex.newCondition();

        this.acceptingBets = false;
        this.acceptingHonours = false;

        this.pendingBets = new LinkedList<>();
        this.acceptedBets = new LinkedList<>();
        this.rejectedBets = new LinkedList<>();
        this.pendingHonours = new LinkedList<>();
        this.acceptedHonours = new HashMap<>();
        this.generalRepository = gr;
        this.stable = s;
        this.racingTrack = r;
    }

    private void getRaceOdds() {
        double oddSum = 0.0;
        raceOdds = new double[EventVariables.NUMBER_OF_HORSES_PER_RACE];
        int[][] horsesAgility = stable.getHorsesAgility();

        for (int i = 0; i < horsesAgility[currentRaceID].length; i++)
            oddSum += horsesAgility[currentRaceID][i];


        for (int i = 0; i < horsesAgility[currentRaceID].length; i++) {
            raceOdds[i] = horsesAgility[currentRaceID][i] / oddSum;
        }

        generalRepository.setHorsesOdd(raceOdds);
    }

    private void validatePendingBets() {
        // validate pending FIFO's bets
        if (pendingBets.size() > 0) {
            Bet bet = pendingBets.peek();
            pendingBets.remove(bet);

            // Considering the bet value is valid since spectator cannot bet over a certain amount
            if (bet.getHorseID() < EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                acceptedBets.add(bet);
                System.out.println("ACCEPTED " + bet.getSpectatorID());
                generalRepository.setSpectatorsBet(bet.getSpectatorID(),
                        bet.getValue(), bet.getHorseID());
            } else
                rejectedBets.add(bet);
        }
    }

    public void acceptTheBets(int raceID) {
        Broker b;
        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.WAITING_FOR_BETS);
        generalRepository.setBrokerState(BrokerState.WAITING_FOR_BETS);

        // clear accepted and rejected bets list
        acceptedBets.clear();
        rejectedBets.clear();

        // clear betting queues
        pendingHonours.clear();
        acceptedHonours.clear();

        // Update raceID and start accepting bets
        currentRaceID = raceID;
        getRaceOdds();

        acceptingBets = true;

        // broker wait
        while (true) {

            // validate all pending bet
            validatePendingBets();

            // notify spectators
            waitingForValidation.signalAll();

            if (acceptedBets.size() == EventVariables.NUMBER_OF_SPECTATORS)
                break;

            try {
                waitingForBet.await();
            } catch (InterruptedException ignored){}
        }

        acceptingBets = false;

        mutex.unlock();
    }

    public boolean placeABet(Bet bet) {
        Spectator s;
        boolean validBet;

        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.PLACING_A_BET);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.PLACING_A_BET);

        // If it's still not accepting bets, wait
        if (!acceptingBets) {
            try {
                waitingForValidation.await();
            } catch (InterruptedException ignored){}
        }

        System.out.println("Spectator " + s.getID() + " waiting for broker " +
                "to arrive to betting centre.");

        // add to waiting bets queue
        pendingBets.add(bet);

        // spectator wait
        while (true) {
            // notify broker
            waitingForBet.signal();

            if (acceptedBets.contains(bet) || rejectedBets.contains(bet)) break;

            try {
                waitingForValidation.await();
            } catch (InterruptedException ignored){}
        }

        validBet = acceptedBets.contains(bet);
        System.out.println(s.getID() + " BET = " + validBet);

        mutex.unlock();

        return validBet;
    }

    public boolean areThereAnyWinners(int[] winners) {
        //Broker b;
        boolean areThereWinners;

        mutex.lock();

        //b = (Broker)Thread.currentThread();
        //b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);

        // save horses winners
        this.winners = winners;

        // create queue for the winning bets
        winningBets = acceptedBets.stream().filter(bet ->
                IntStream.of(winners).anyMatch(x -> x == bet.getHorseID()))
                .collect(Collectors.toList());



        areThereWinners = !winningBets.isEmpty();

        mutex.unlock();

        return areThereWinners;
    }

    public void honourTheBets() {
        Broker b;
        mutex.lock();

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.SETTLING_ACCOUNTS);
        generalRepository.setBrokerState(BrokerState.SETTLING_ACCOUNTS);

        acceptingHonours = true;

        while (true) {

            waitingForCash.signalAll();

            if (acceptedHonours.size() >= winningBets.size()) break;

            try {
                System.out.println("Broker espera para pagar aos espetadores");
                waitingForHonours.await();
            } catch (InterruptedException ignored) {}

            System.out.println("Broker acorda para pagar a um espetador");

            if (pendingHonours.size() > 0) {
                int spectatorID = pendingHonours.poll();
                Bet bet = (Bet) winningBets.stream().filter(
                        bt -> bt.getSpectatorID() == spectatorID).toArray()[0];

                acceptedHonours.put(spectatorID,
                        bet.getValue() * raceOdds[bet.getHorseID()]);
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

        if (!acceptingHonours) {
            try {
                System.out.println("Espetador" + s.getID() + " espera para o" +
                        " broker chegar ao betting centre");
                waitingForCash.await();
            } catch (InterruptedException ignored) {}
        }

        // add to pending collections queue
        pendingHonours.add(spectatorID);

        // spectator waits queue
        while (true) {
            // notify broker queue
            waitingForHonours.signalAll();

            if (acceptedHonours.containsKey(spectatorID)) break;

            try {
                System.out.println("Espetador espera para receber dinheiro");
                waitingForCash.await();
            } catch (InterruptedException ignored) {}

            System.out.println("Espetador acorda para receber dinheiro");
        }

        // get winning value
        winningValue = acceptedHonours.get(spectatorID);

        // clean honour entry
        //acceptedHonours.remove(spectatorID);

        mutex.unlock();

        return winningValue;
    }
}
