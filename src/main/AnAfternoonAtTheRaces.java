package main;

import entities.Broker;
import entities.Horse;
import entities.Spectator;
import sharedRegions.*;

import java.util.Arrays;
import java.util.Random;

public class AnAfternoonAtTheRaces {

    public static void main (String [] args)
    {
        Random rnd = new Random();
        int agility;
        Stable stable = null;
        Paddock paddock = null;
        RacingTrack racingTrack = null;
        ControlCentre controlCentre = null;
        BettingCentre bettingCentre = null;
        GeneralRepository generalRepository = new GeneralRepository();

        controlCentre = new ControlCentre(generalRepository);
        stable = new Stable(generalRepository);
        paddock = new Paddock(generalRepository, controlCentre);
        racingTrack = new RacingTrack(generalRepository, controlCentre, paddock);
        bettingCentre = new BettingCentre(generalRepository, stable, racingTrack);




        Broker broker = new Broker(stable, racingTrack, controlCentre, bettingCentre);
        Horse [] horses = new Horse[EventVariables.NUMBER_OF_HORSES];
        Spectator[] spectators = new Spectator[EventVariables.NUMBER_OF_SPECTATORS];

        /* problem initialization */

        /* Initialize horses with lineups */
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++)
        {
            agility = rnd.nextInt(EventVariables.HORSE_MAX_STEP) + 1;
            horses[i] = new Horse(i, agility, stable, paddock, racingTrack);
        }

        int[] horsesIdx = Arrays.stream(horses).mapToInt(Horse::getID).toArray();
        int[][] raceLineups = Stable.generateLineup(horsesIdx);

        for (int i = 0; i < EventVariables.NUMBER_OF_RACES; i++) {
            for (int j = 0; j < EventVariables.NUMBER_OF_HORSES_PER_RACE; j++) {
                horses[raceLineups[i][j]].setRaceID(i);
                horses[raceLineups[i][j]].setRaceIdx(j);
            }
        }

        /* Initialize spectators */
        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
        {
            spectators[i] = new Spectator(i, EventVariables.INITIAL_WALLET,
                    paddock, controlCentre, bettingCentre, generalRepository);
        }


        /* start of the simulation */
        broker.start();

        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++)
        {
            horses[i].start();
        }

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
        {
            spectators[i].start();
        }

        /* wait for the end of the simulation */
        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++)
        {
            try {
                horses[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
        {
            try {
                spectators[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
