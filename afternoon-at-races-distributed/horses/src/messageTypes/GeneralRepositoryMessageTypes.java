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

    /**
     * Method that returns an enum given its id.
     * @param id The id correspondent to the enum type.
     * @return The correspondent enum.
     */
    public static GeneralRepositoryMessageTypes getType(int id) {
        switch (id) {
            case 0: return SET_BROKER_STATE;
            case 1: return SET_SPECTATOR_STATE;
            case 2: return SET_HORSE_STATE;
            case 3: return SET_SPECTATOR_GAINS;
            case 4: return SET_SPECTATORS_BET;
            case 5: return SET_HORSE_AGILITY;
            case 6: return SET_HORSES_ODD;
            case 7: return SET_HORSE_POSITION;
            case 8: return SET_HORSES_STANDING;
            case 9: return INIT_RACE;
            case 10: return ERROR;
            default: return null;
        }
    }
}
