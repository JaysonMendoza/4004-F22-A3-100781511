package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.OtherPlayerHandUpdate;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.PlayerUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class HandObserverMock implements PlayerHandObserver {
    private final Player player;
    private final Queue<ArrayList<Card>> updatesRecieved;
    private final Logger LOG = LoggerFactory.getLogger(HandObserverMock.class);
    public HandObserverMock(Player player) throws Exception {
        this.player = player;
        updatesRecieved = new ArrayDeque<>();
        if(!player.subscribeHandUpdates(this)) {
            throw new Exception("Failed to subscribe");
        }
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void handlePlayerHandUpdate(Player player, PlayerUpdate updatePlayer, OtherPlayerHandUpdate updateOther) {
        this.LOG.info("Hand Update Received for playerID '{}' with size '{}'",player.getPlayerID(),updatePlayer.cards().size());
        ArrayList<Card> cards = new ArrayList<>();
        updatePlayer.cards().forEach(c -> Card.valueOf(c));
        updatesRecieved .add(cards);
    }

    public ArrayList<Card> getNextHandUpdate() {
        return updatesRecieved.remove();
    }

    public boolean hasNext() {
        return updatesRecieved.size() > 0;
    }
}
