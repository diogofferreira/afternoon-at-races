package entities;

import states.HorseState;

import java.util.Random;

/**
 * Interface that defines the entity Horse.
 */
public interface HorseInt {
    /**
     * Method that returns the current Horse/Jockey pair state.
     * @return Current Horse/Jockey pair state.
     */
    HorseState getHorseState();

    /**
     * Updates the current Horse/Jockey pair state.
     * @param state The new Horse/Jockey pair state.
     */
    void setHorseState(HorseState state);

    /**
     * Method that returns the ID of the Horse/Jockey pair.
     * @return The ID of the Horse/Jockey pair.
     */
    int getID();

    /**
     * Method that sets the ID of the Horse/Jockey Pair.
     * @param id The new ID of the Horse/Jockey Pair.
     */
    void setID(int id);

    /**
     * Method that returns the race ID in which the pair will participate.
     * @return ID of the race in which the pair will participate.
     */
    int getRaceID();

    /**
     * Sets the race ID in which the pair will participate.
     * @param raceID The race ID in which the horse will run.
     */
    void setRaceID(int raceID);

    /**
     * Method that returns the horse's index/position on the race.
     * @return Horse's index/position on the race.
     */
    int getRaceIdx();

    /**
     * Sets the horse's index/position on the race.
     * @param raceIdx The horse's index/position on the race.
     */
    void setRaceIdx(int raceIdx);

    /**
     * Method that returns the agility of each horse, which in practice corresponds
     * to the maximum step distance the horse can take in each iteration.
     * @return Agility/max step per iteration of the horse.
     */
    int getAgility();

    /**
     * Method that sets the agility of each horse, which in practice corresponds
     * to the maximum step distance the horse can take in each iteration.
     * @param agility The new value of agility/max step per iteration of the horse.
     */
    void setAgility(int agility);


    /**
     * Method that returns the horse's current position on the racing track, i.e.,
     * the current travelled distance.
     * @return Horse's current position.
     */
    int getCurrentPosition();

    /**
     * Method that sets the current position of Horse/Jockey pair.
     * @param currentPosition The current position of the Horse/Jockey pair in
     *                        the racing track.
     */
    void setCurrentPosition(int currentPosition);

    /**
     * Method that returns the number of steps the horse has already taken.
     * @return The number of steps/iterations of the horse during the race.
     */
    int getCurrentStep();

    /**
     * Method that sets the number of steps the horse has already taken.
     * @param currentStep The number of steps/iterations that the horse has
     *                    already taken.
     */
    void setCurrentStep(int currentStep);

    /**
     * Method that updates the current position of the pair, i.e., increases the
     * position with step steps and increments the current step by one.
     * @param step The distance of the increment/step.
     */
    void updateCurrentPosition(int step);
}
