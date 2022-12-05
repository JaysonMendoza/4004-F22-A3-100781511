package ca.jkmconsulting.crazyEightsCountdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class PlayerManager implements PlayerHandObserver {
    private final Logger LOG = LoggerFactory.getLogger(PlayerManager.class);
    private final ConcurrentHashMap<String,Player> playerPrincipalToPlayer;

    public PlayerManager() {
        this.playerPrincipalToPlayer = new ConcurrentHashMap<>();
    }

    @Autowired
    SimpMessagingTemplate message;

    @MessageMapping("/joinGame")
    @SendToUser("/queue/playerRegistered")
    synchronized public PlayerUpdate registerPlayer(Principal principal,@RequestParam() String name,@Header("simpSessionId") String sessionId) {
        LOG.info(String.format("Registration request for player %s received. Session: %s, PlayerID: %s",name,sessionId,principal.getName()));

        Player player = this.playerPrincipalToPlayer.computeIfAbsent(principal.getName(), k -> new Player(name,sessionId,principal.getName()));
        //todo: register observer on player for updates
        //todo: register to game

        LOG.info("Player '{}' registered successfully!",name);
        return new PlayerUpdate(player.getPlayerID(),player.getName(),null);
    }

    @Override
    synchronized public void handlePlayerHandUpdate(Player player,PlayerUpdate updatePlayer, OtherPlayerHandUpdate updateOther) {
        this.LOG.info("Dispatch: updatePlayer from player '{}' received and sent to all players.", player.getName());
        for(Player p: playerPrincipalToPlayer.values()) {
            if(player==p) {
                this.LOG.info("Updated player '{}' sent to endpoint /user/queue/playerUpdated with '{}' cards",p.getPlayerID(),updatePlayer.cards().size());
                message.convertAndSendToUser(p.getSessionID(),"/queue/playerUpdated",updatePlayer);
            }
            else {
                this.LOG.info("Updated player '{}' sent to endpoint /user/queue/OtherPlayerUpdated with '{}' cards",p.getPlayerID(),updateOther.numCards());
                message.convertAndSendToUser(p.getSessionID(),"/queue/OtherPlayerUpdated",updateOther);
            }
        }
    }
}
