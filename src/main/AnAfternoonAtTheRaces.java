package main;

import entities.Broker;
import entities.Horse;
import entities.Spectator;
import sharedRegions.*;

import java.util.Arrays;
import java.util.Random;

public class AnAfternoonAtTheRaces {

    public static void main (String [] args) {
        Random rnd;
        int agility;
        int[] horsesIdx;
        Stable stable;
        Paddock paddock;
        RacingTrack racingTrack;
        ControlCentre controlCentre;
        BettingCentre bettingCentre;
        GeneralRepository generalRepository;

        rnd = new Random();

        // generate races lineup
        horsesIdx = new int[EventVariables.NUMBER_OF_HORSES];
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++)
            horsesIdx[i] = i;

        // shared regions initialization
        generalRepository = new GeneralRepository();
        stable = new Stable(generalRepository, horsesIdx);
        controlCentre = new ControlCentre(generalRepository, stable);
        paddock = new Paddock(generalRepository, controlCentre);
        racingTrack = new RacingTrack(generalRepository, controlCentre, paddock);
        bettingCentre = new BettingCentre(generalRepository, stable);

        // entities initialization
        Broker broker = new Broker(stable, racingTrack, controlCentre, bettingCentre);
        Horse [] horses = new Horse[EventVariables.NUMBER_OF_HORSES];
        Spectator[] spectators = new Spectator[EventVariables.NUMBER_OF_SPECTATORS];

        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++) {
            agility = rnd.nextInt(EventVariables.HORSE_MAX_STEP) + 1;
            horses[i] = new Horse(i, agility, stable, paddock, racingTrack);
        }

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++) {
            spectators[i] = new Spectator(i, EventVariables.INITIAL_WALLET, i,
                    paddock, controlCentre, bettingCentre, generalRepository);
        }

        // start of the simulation

        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++)
            horses[i].start();

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
            spectators[i].start();

        broker.start();

        // end of the simulation
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++) {
            try {
                horses[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++) {
            try {
                spectators[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            broker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
