package sharedRegions;

import entities.Broker;
import entities.Spectator;
import main.EventVariables;
import states.BrokerState;
import states.SpectatorState;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class ControlCentre {

    private GeneralRepository generalRepository;

    private Lock mutex;
    private Condition horsesInPaddock, waitForRace, watchingRace, startingRace,
            waitForCelebrate;

    private Stable stable;

    private int spectatorsReady;
    private int wantToCelebrate;

    private boolean spectatorsCanProceed;

    private boolean raceFinished, racesEnded;

    private int[] winners;

    public ControlCentre(GeneralRepository gr, Stable s) {
        if (gr == null)
            throw new IllegalArgumentException("Invalid General Repository.");
        if (s == null)
            throw new IllegalArgumentException("Invalid Stable.");

        this.generalRepository = gr;
        this.stable = s;
        this.mutex = new ReentrantLock();
        this.horsesInPaddock = this.mutex.newCondition();
        this.waitForRace = this.mutex.newCondition();
        this.watchingRace = this.mutex.newCondition();
        this.startingRace = this.mutex.newCondition();
        this.waitForCelebrate = this.mutex.newCondition();
        this.spectatorsReady = 0;
        this.wantToCelebrate = 0;
        this.spectatorsCanProceed = false;
        this.racesEnded = false;
        this.raceFinished = false;
    }

    public void summonHorsesToPaddock(int raceNumber) {
        Broker b;
        mutex.lock();

        generalRepository.setRaceNumber(raceNumber);

        b = (Broker)Thread.currentThread();
        b.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);
        generalRepository.setBrokerState(BrokerState.ANNOUNCING_NEXT_RACE);
        System.out.println("BROKER VAI DORMIR");

        stable.summonHorsesToPaddock(raceNumber);

        // broker wait
        try {
            horsesInPaddock.await();
        } catch (InterruptedException ignored) {}
        System.out.println("BROKER VOU-ME EMBORA");

        mutex.unlock();
    }

    public boolean waitForNextRace() {
        Spectator s;
        boolean continueRace;

        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.WAITING_FOR_A_RACE_TO_START);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.WAITING_FOR_A_RACE_TO_START);

        if (!spectatorsCanProceed) {
            // spectators wait
            try {
                System.out.println("SPEC " + s.getID() + " VAI DORMIR");
                //spectatorsReady++;
                waitForRace.await();
            } catch (InterruptedException ignored) {}
            System.out.println("SPEC " + s.getID() + " ACORDOU");
        }

        /*
        continueRace = !racesEnded;

        if (!continueRace && ++wantToCelebrate == EventVariables.NUMBER_OF_SPECTATORS)
            waitForCelebrate.signal();
        */
        mutex.unlock();

        return false;//continueRace;
    }

    public void proceedToPaddock() {
        mutex.lock();

        // wait for all spectators to arrive

        // notify all spectators
        spectatorsCanProceed = true;

        waitForRace.signalAll();

        mutex.unlock();
    }

    public void goCheckHorses() {
        mutex.lock();

        System.out.println("WAKING UP BROKER");
        // notify broker
        horsesInPaddock.signal();

        mutex.unlock();
    }

    public void goWatchTheRace() {
        Spectator s;
        mutex.lock();

        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.WATCHING_A_RACE);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.WATCHING_A_RACE);

        // spectators wait
        System.out.println("Spectator " + s.getID() + " watching a race and sleeps");
        try {
            watchingRace.await();
        } catch (InterruptedException ignored) {}

        System.out.println("Spectator " + s.getID() + " wakes up after watching a race");

        mutex.unlock();
    }

    public void startTheRace() {
        mutex.lock();

        spectatorsReady = 0;

        System.out.println("Broker supervises the race in control centre");

        // broker wait
        while (!raceFinished) {
            try {
                startingRace.await();
            } catch (InterruptedException ignored) { }
        }

        raceFinished = false;
        mutex.unlock();
    }

    public void finishTheRace(int[] winners) {
        mutex.lock();

        System.out.println("Updating winners");
        this.winners = winners;

        // Notify general repository to clear all horse related info
        generalRepository.resetRace();

        System.out.println("Waking up broker");
        this.spectatorsCanProceed = false;

        // notify broker
        raceFinished = true;
        startingRace.signal();

        mutex.unlock();
    }

    public int[] reportResults() {
        //Broker b;
        mutex.lock();

        //b = (Broker)Thread.currentThread();
        //b.setBrokerState(BrokerState.SUPERVISING_THE_RACE);
        //generalRepository.setBrokerState(BrokerState.SUPERVISING_THE_RACE);

        System.out.println("Waking up spectators");
        // notify all spectators
        watchingRace.signalAll();

        mutex.unlock();

        return this.winners;
    }

    public boolean haveIWon(int horseID) {
        boolean won;
        //Spectator s;
        mutex.lock();

        //s = (Spectator)Thread.currentThread();
        //s.setSpectatorState(SpectatorState.WATCHING_A_RACE);
        //generalRepository.setSpectatorState(s.getID(),
        //        SpectatorState.WATCHING_A_RACE);

        // checks if winner is the one he/she bet
        won = IntStream.of(winners).anyMatch(w -> w == horseID);

        mutex.unlock();

        return won;
    }

    public void entertainTheGuests() {
        Broker b;
        mutex.lock();

        // race is over
        racesEnded = true;

        // wait for all spectators want to celebrate
        do {
            waitForRace.signalAll();

            try {
                waitForCelebrate.await();
            } catch (InterruptedException ignored) {}
        } while (spectatorsReady != EventVariables.NUMBER_OF_SPECTATORS);

        // broker just playing host, end the afternoon
        //b = (Broker)Thread.currentThread();
        //b.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);
        //generalRepository.setBrokerState(BrokerState.PLAYING_HOST_AT_THE_BAR);

        mutex.unlock();
    }

    public void relaxABit() {
        Spectator s;
        mutex.lock();

        /// just relax, end the afternoon
        s = (Spectator)Thread.currentThread();
        s.setSpectatorState(SpectatorState.CELEBRATING);
        generalRepository.setSpectatorState(s.getID(),
                SpectatorState.CELEBRATING);

        mutex.unlock();
    }

    public int getSpectatorsReady() {
        return spectatorsReady;
    }
}
