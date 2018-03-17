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
        // validate pending FIFO's bets */
        while (pendingBets.size() > 0) {
            Bet bet = pendingBets.peek();
            pendingBets.remove(bet);

            // Considering the bet value is valid since spectator cannot bet over a certain amount
            if (bet.getHorseID() < EventVariables.NUMBER_OF_HORSES_PER_RACE) {
                acceptedBets.add(bet);
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
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.PLACING_A_BET);

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

        System.out.println("WINNING BETS: " + winningBets);

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
        generalRepository.setBrokerState(BrokerState.SETTLING_ACCOUNTS);

        while (true) {

            System.out.println("ACCEPT SIZE: " + acceptedHonours.size());
            System.out.println("WINNING SIZE: " + winningBets.size());

            System.out.println("WAITING FOR HONORS");

            if(!(acceptedHonours.size() < winningBets.size())) break;

            waitingForHonours.signalAll();

            try {
                System.out.println("BROKER BLOQUEIA");
                waitingForHonours.await();
            } catch (InterruptedException ignored) {}

            if (pendingHonours.size() > 0) {
                int spectatorID = pendingHonours.poll();
                Bet bet = (Bet) winningBets.stream().filter(
                        bt -> bt.getSpectatorID() == spectatorID).toArray()[0];


                acceptedHonours.put(spectatorID,
                        bet.getValue() * raceOdds[bet.getHorseID()]);

            }

            System.out.println("WAITING HONORS: " + pendingHonours);

        }

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

        // add to pending collections queue
        pendingHonours.add(spectatorID);



        // spectator waits queue
        do {
            // notify broker queue
            waitingForHonours.signal();

            try {
                System.out.println("SPEC " + s.getID() + " BLOQUEIA");
                waitingForCash.await();
            } catch (InterruptedException ignored) {}
        } while (!acceptedHonours.containsKey(spectatorID));

        System.out.println("SPEC " + s.getID() + " ACORDA");

        // get winning value
        winningValue = acceptedHonours.get(spectatorID);
        // clean honour entry
        acceptedHonours.remove(spectatorID);

        mutex.unlock();

        return winningValue;
    }
}
