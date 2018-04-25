package sharedRegions;

import entities.SpectatorInt;
import main.EventVariables;
import messageTypes.BettingCentreMessageTypes;
import messages.BettingCentreMessage;
import sharedRegions.BettingCentre;


public class BettingCentreInterface {

    private int requests;

    private int raceNumber;

    private BettingCentre bettingCentre;

    public BettingCentreInterface(BettingCentre bettingCentre) {
        if (bettingCentre == null)
            throw new IllegalArgumentException("Invalid Betting Centre.");

        this.bettingCentre = bettingCentre;
        this.requests = 0;
        this.raceNumber = -1;
    }

    public BettingCentreMessage processAndReply(BettingCentreMessage inMessage) {
        BettingCentreMessageTypes mType;
        int raceID, spectatorID, horseIdx;
        int[] winners;
        boolean areThereAnyWinners;
        double amount;

        System.out.println(inMessage.toString());

        if ((mType = BettingCentreMessageTypes.getType(inMessage.getMethod())) == null)
            return new BettingCentreMessage(BettingCentreMessageTypes.ERROR);

        switch (mType) {
            case ACCEPT_THE_BETS:
                raceID = inMessage.getRaceId();
                raceNumber = raceID;

                if (raceID < 0 || raceID >= EventVariables.NUMBER_OF_RACES)
                    return new BettingCentreMessage(BettingCentreMessageTypes.ERROR);

                bettingCentre.acceptTheBets(raceID);
                return new BettingCentreMessage(
                        BettingCentreMessageTypes.ACCEPT_THE_BETS,
                        raceID, inMessage.getEntityId());

            case ARE_THERE_ANY_WINNERS:
                winners = inMessage.getWinners();
                if (winners == null || winners.length == 0)
                    return new BettingCentreMessage(BettingCentreMessageTypes.ERROR);

                areThereAnyWinners = bettingCentre.areThereAnyWinners(winners);
                return new BettingCentreMessage(
                        BettingCentreMessageTypes.ACCEPT_THE_BETS,
                        areThereAnyWinners, inMessage.getEntityId());

            case GO_COLLECT_THE_GAINS:
                spectatorID = inMessage.getEntityId();

                if (spectatorID < 0 ||
                        spectatorID >= EventVariables.NUMBER_OF_SPECTATORS)
                    return new BettingCentreMessage(BettingCentreMessageTypes.ERROR);

                ((SpectatorInt) Thread.currentThread()).setID(spectatorID);

                amount = bettingCentre.goCollectTheGains();

                if (raceNumber == EventVariables.NUMBER_OF_RACES -1)
                    requests++;

                return new BettingCentreMessage(
                        BettingCentreMessageTypes.GO_COLLECT_THE_GAINS,
                        amount, inMessage.getEntityId());

            case HONOUR_THE_BETS:
                bettingCentre.honourTheBets();
                return new BettingCentreMessage(
                        BettingCentreMessageTypes.HONOUR_THE_BETS,
                        inMessage.getEntityId());

            case PLACE_A_BET:
                spectatorID = inMessage.getEntityId();
                if (spectatorID < 0 ||
                        spectatorID >= EventVariables.NUMBER_OF_SPECTATORS)
                    return new BettingCentreMessage(BettingCentreMessageTypes.ERROR);

                ((SpectatorInt) Thread.currentThread()).setID(spectatorID);
                ((SpectatorInt) Thread.currentThread()).setStrategy(inMessage.getStrategy());
                ((SpectatorInt) Thread.currentThread()).setID(inMessage.getWallet());

                horseIdx = bettingCentre.placeABet();
                return new BettingCentreMessage(
                        BettingCentreMessageTypes.PLACE_A_BET, horseIdx,
                        inMessage.getEntityId());

            default:
                return new BettingCentreMessage(BettingCentreMessageTypes.ERROR);
        }
    }

    public int getRequests() {
        return requests;
    }
}
