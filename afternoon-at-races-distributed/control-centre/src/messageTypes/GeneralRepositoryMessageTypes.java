package messageTypes;

public enum GeneralRepositoryMessageTypes {

    SET_BROKER_STATE(0),

    SET_SPECTATOR_STATE(1),

    SET_HORSE_STATE(2),

    SET_SPECTATOR_GAINS(3),

    SET_SPECTATORS_BET(4),

    SET_HORSE_AGILITY(5),

    SET_HORSES_ODD(6),

    SET_HORSE_POSITION(7),

    SET_HORSES_STANDING(8),

    INIT_RACE(9),

    ERROR(10);

    /**
     * ID of the message type;
     */
    private final int id;

    /**
     * Constructor to add a new message type.
     * @param id ID of the message type.
     */
    private GeneralRepositoryMessageTypes(int id) {
        this.id = id;
    }

    /**
     * Method that returns an integer id of the message type.
     * @return Integer representation of the message type;
     */
    public int getId() {
        return this.id;
    }
}
