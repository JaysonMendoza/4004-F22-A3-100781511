package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.Enums.AlertTypes;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import ca.jkmconsulting.crazyEightsCountdown.Exceptions.CrazyEightsIllegalCardException;
import ca.jkmconsulting.crazyEightsCountdown.Exceptions.CrazyEightsInvalidPlayerException;
import ca.jkmconsulting.crazyEightsCountdown.Exceptions.CrazyEightsJoinFailureException;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.AlertData;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.OtherPlayerHandUpdate;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.PlayerUpdate;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
        this.LOG.info("GameController constructed.");
    }

    @Autowired
    SimpMessagingTemplate message;

    @Autowired
    GameController game;

    @MessageMapping("/joinGame")
    @SendToUser("/queue/playerRegistered")
    synchronized public PlayerUpdate registerPlayer(Principal principal, @RequestParam() String name, @Header("simpSessionId") String sessionId) throws CrazyEightsJoinFailureException {
        LOG.info(String.format("Registration request for player %s received. Session: %s, PlayerID: %s",name,sessionId,principal.getName()));
        Player player = this.playerPrincipalToPlayer.computeIfAbsent(principal.getName(), k -> new Player(name,sessionId,principal.getName()));
        player.subscribeHandUpdates(this);
        try {
            game.joinGame(player);
        } catch (CrazyEightsJoinFailureException e) {
            LOG.info("Rejection message sent to player: '{}': '{}'",player.getName(),e.getMessage());
            AlertData payload = new AlertData(AlertTypes.BAD,"Registration Rejection",e.getMessage(),false);
            message.convertAndSendToUser(principal.getName(),"/queue/failedJoin",payload);
            throw e;
        }


        LOG.info("Player '{}' registered successfully!",name);
        return new PlayerUpdate(player.getPlayerID(),player.getName(),null);
    }

    @MessageMapping("/playCard")
    synchronized public void handleActionPlayCard(Principal principal,@RequestParam() Card cardEnum) throws CrazyEightsIllegalCardException, CrazyEightsInvalidPlayerException {
        Player p = this.playerPrincipalToPlayer.get(principal.getName());
        LOG.info("handleActionPlayCard invoked by playerID '{}' for cardEnum '{}'.",p.getPlayerID(),cardEnum);
        game.actionPlayerPlayCard(p,cardEnum);
    }

    @MessageMapping("/DrawCard")
    synchronized public void handleActionDrawCard(Principal principal) throws CrazyEightsIllegalCardException, CrazyEightsInvalidPlayerException {
        Player p = this.playerPrincipalToPlayer.get(principal.getName());
        LOG.info("handleActionDrawCard invoked by playerID '{}'.",p.getPlayerID());
        game.actionDrawCard(p);
    }

    @MessageMapping("/suitSelected")
    synchronized public void handleActionSuitSelected(Principal principal,@RequestParam() Card card) throws CrazyEightsInvalidPlayerException {
        Player p = this.playerPrincipalToPlayer.get(principal.getName());
        LOG.info("handleActionSuitSelected invoked by playerID '{}' for cardEnum '{}'.",p.getPlayerID(),card);
        game.actionSelectSuit(p,card.suit);
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
