package main;

import entities.Broker;
import entities.Horse;
import entities.Spectator;
import sharedRegions.*;

import java.util.Arrays;

public class AnAfternoonAtTheRaces {

    public static void main (String [] args)
    {

        Stable stable = null;
        Paddock paddock = null;
        RacingTrack racingTrack = null;
        ControlCentre controlCentre = null;
        BettingCentre bettingCentre = null;

        controlCentre = new ControlCentre();

        Broker broker = new Broker();
        Horse [] horses = new Horse[EventVariables.NUMBER_OF_HORSES];
        Spectator[] spectators = new Spectator[EventVariables.NUMBER_OF_SPECTATORS];

        /* problem initialization */

        for (int i = 0; i < EventVariables.NUMBER_OF_HORSES; i++)
        {
            horses[i] = new Horse();
        }

        for (int i = 0; i < EventVariables.NUMBER_OF_SPECTATORS; i++)
        {
            spectators[i] = new Spectator();
        }

        int [] horseIdx = Arrays.stream(horses).mapToInt(Horse::getID).toArray();

        bettingCentre = new BettingCentre(stable, racingTrack);
        stable = new Stable(horseIdx, controlCentre);
        paddock = new Paddock(stable, controlCentre);
        racingTrack = new RacingTrack(controlCentre, paddock);

        controlCentre.setRacingTrack(racingTrack);

        /* start of the simulation */

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
