package ca.jkmconsulting.crazyEightsCountdown;

import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerManager {

    private final Logger LOG = LoggerFactory.getLogger(PlayerManager.class);
    ConcurrentHashMap<UUID,Player> playerIDs;
    ConcurrentHashMap<String,Player> playerSessions;

    public PlayerManager() {
        this.playerIDs = new ConcurrentHashMap<>();
        this.playerSessions = new ConcurrentHashMap<>();
    }

    //    @RequestMapping(value = "/joinGame", method = RequestMethod.POST)
    @MessageMapping("/joinGame")
    synchronized public Player registerPlayer(@RequestParam("name") String name) {
        LOG.info(String.format("Registration request for player %s received.",name));
        String sessionID = RequestContextHolder.currentRequestAttributes().getSessionId();

        if(sessionID=="") {
            LOG.warn("User '{}' had an invalid session id",name);
            return null;
        }
        UUID playerID = UUID.randomUUID();
        while(playerIDs.containsKey(playerID)) {
            playerID = UUID.randomUUID();
        }

        Player player = new Player(name,sessionID,playerID);

        //todo: register to game

        playerSessions.put(sessionID,player);
        playerIDs.put(playerID,player);
        LOG.info("Player '{}' registered successfully!",name);
        return player;
    }

}
