package ca.jkmconsulting.crazyEightsCountdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;

import java.security.Principal;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class PlayerManager {
    static private final HashSet<UUID> assignedUUIDs= new HashSet<>();
    private final Logger LOG = LoggerFactory.getLogger(PlayerManager.class);
    private ConcurrentHashMap<String,Player> playerIDsToPlayer;

    public PlayerManager() {
        this.playerIDsToPlayer = new ConcurrentHashMap<>();
    }

    @Autowired
    SimpMessagingTemplate message;

    @MessageMapping("/joinGame")
    @SendToUser("/queue/playerRegistered")
    synchronized public PlayerUpdate registerPlayer(Principal principal,@RequestParam() String name,@Header("simpSessionId") String sessionId) {
        LOG.info(String.format("Registration request for player %s received. Session: %s, PlayerID: %s",name,sessionId,principal.getName()));

        Player player = this.playerIDsToPlayer.computeIfAbsent(principal.getName(),k -> new Player(name,sessionId,principal.getName()));
        //todo: register observer on player for updates
        //todo: register to game

        LOG.info("Player '{}' registered successfully!",name);
        return new PlayerUpdate(player.getPlayerID(),player.getName(),null);
    }


    synchronized void handlePlayerUpdated(Player player) {
        if(player==null) {
            this.LOG.error("handlePlayerUpdated() Invocation Error: Player cannot be null");
            return;
        }

        this.LOG.info("Message: updatePlayer from player '{}' received.", player.getName());

        message.convertAndSendToUser(player.getSessionID(),"/queue/playerUpdated",new PlayerUpdate(player.getPlayerID(),player.getName(),null)); //TODO: When cards implemented change this line to use it
    }

    static UUID getNewPlayerID() {
        UUID playerID = UUID.randomUUID();
        while(assignedUUIDs.contains(playerID)) {
            playerID = UUID.randomUUID();
        }
        return playerID;
    }

}
