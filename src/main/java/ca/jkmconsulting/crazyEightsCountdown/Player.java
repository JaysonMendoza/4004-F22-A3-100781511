package ca.jkmconsulting.crazyEightsCountdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
public class Player {
    private final Logger LOG = LoggerFactory.getLogger(Player.class);
    private final String sessionID;
    private final String playerID;
    private String name;

    public Player(String name,String sessionID,String playerID) {
        this.sessionID = sessionID;
        this.playerID = playerID;
        this.name = name;
        LOG.info("Created Player {} with ID {} and session {}",name,playerID,sessionID);
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getName() {
        return name;
    }
}
