package messages;

import java.io.Serializable;

public class GeneralRepositoryMessage implements Serializable {
    // method type
    private int method;
    // arguments (step)
    private double[] args;
    // entity id
    private int entityId;
    // entity state
    private int entityState;
}
