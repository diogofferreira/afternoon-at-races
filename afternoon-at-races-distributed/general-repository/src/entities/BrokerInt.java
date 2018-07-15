package entities;

import states.BrokerState;

/**
 * Interface that defines the entity Broker.
 */
public interface BrokerInt {
    /**
     * Method that returns the current Broker state.
     * @return Current Broker state.
     */
    BrokerState getBrokerState();

    /**
     * Updates the current Broker state.
     * @param state The new Broker state.
     */
    void setBrokerState(BrokerState state);

    /**
     * Method that returns the current race identifier.
     * @return Current race id.
     */
    public int getRaceNumber();

    /**
     * Updates the current race identifier.
     * @param raceNumber The new race number.
     */
    public void setRaceNumber(int raceNumber);
}
