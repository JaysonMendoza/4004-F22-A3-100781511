package ca.jkmconsulting.crazyEightsCountdown;

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
    public void handlePlayerHandUpdate(ArrayList<Card> newHand) {
        this.LOG.info("Hand Update Received for playerID '{}' with size '{}'",player.getPlayerID(),newHand.size());
        updatesRecieved.add(newHand);
    }

    public ArrayList<Card> getNextHandUpdate() {
        return updatesRecieved.remove();
    }

    public boolean hasNext() {
        return updatesRecieved.size() > 0;
    }
}
