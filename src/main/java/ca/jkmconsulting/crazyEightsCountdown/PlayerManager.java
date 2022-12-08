package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.Enums.AlertTypes;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Suit;
import ca.jkmconsulting.crazyEightsCountdown.Exceptions.CrazyEightsIllegalCardException;
import ca.jkmconsulting.crazyEightsCountdown.Exceptions.CrazyEightsInvalidPlayerException;
import ca.jkmconsulting.crazyEightsCountdown.Exceptions.CrazyEightsJoinFailureException;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.AlertData;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.OtherPlayerHandUpdate;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.PlayerUpdate;
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
    private final ConcurrentHashMap<String,Player> playerIdToPlayer;
    @Autowired
    SimpMessagingTemplate message;

    @Autowired
    GameController game;

    public PlayerManager() {
        this.playerIdToPlayer = new ConcurrentHashMap<>();
        this.LOG.info("GameController constructed.");

    }

    @MessageMapping("/joinGame")
    @SendToUser("/queue/playerRegistered")
    synchronized public PlayerUpdate registerPlayer(Principal principal, @RequestParam() String name, @Header("simpSessionId") String sessionId) throws CrazyEightsJoinFailureException {
        LOG.info(String.format("Registration request for player %s received. Session: %s, PlayerID: %s",name,sessionId,principal.getName()));
        Player player = this.playerIdToPlayer.computeIfAbsent(principal.getName(), k -> new Player(name,sessionId,principal.getName()));
        player.subscribeHandUpdates(this);
        try {
            game.joinGame(player);
        } catch (CrazyEightsJoinFailureException e) {
            LOG.info("Rejection message sent to player: '{}': '{}'",player.getName(),e.getMessage());
            AlertData payload = new AlertData(AlertTypes.BAD,"Registration Rejection",e.getMessage(),false);
            message.convertAndSendToUser(principal.getName(),"/queue/failedJoin",payload);
            throw e;
        }

        LOG.info("PlayerID '{}' registered successfully! Game now has '{}' players",player.getPlayerID(),playerIdToPlayer.size());
        return new PlayerUpdate(player.getPlayerID(),player.getName(),null);
    }

    @MessageMapping("/playCard")
    synchronized public void handleActionPlayCard(Principal principal,@RequestParam() String cardEnum) throws CrazyEightsIllegalCardException, CrazyEightsInvalidPlayerException {
        Card card = Card.valueOf(cardEnum);
        Player p = this.playerIdToPlayer.get(principal.getName());
        LOG.info("handleActionPlayCard invoked by playerID '{}' for cardEnum '{}' -> Card.'{}'.",p.getPlayerID(),cardEnum,card);
        game.actionPlayerPlayCard(p,card);
    }

    @MessageMapping("/DrawCard")
    synchronized public void handleActionDrawCard(Principal principal) throws CrazyEightsIllegalCardException, CrazyEightsInvalidPlayerException {
        Player p = this.playerIdToPlayer.get(principal.getName());
        LOG.info("handleActionDrawCard invoked by playerID '{}'.",p.getPlayerID());
        game.actionDrawCard(p);
    }

    @MessageMapping("/suitSelected")
    synchronized public void handleActionSuitSelected(Principal principal,@RequestParam() String cardEnum) throws CrazyEightsInvalidPlayerException {
        Player p = this.playerIdToPlayer.get(principal.getName());
        Card card = Card.valueOf(cardEnum);
        LOG.info("handleActionSuitSelected invoked by playerID '{}' for cardEnum '{}'.",p.getPlayerID(),card);

        game.actionSelectSuit(p,card.suit);
    }

    @Override
    synchronized public void handlePlayerHandUpdate(Player player,PlayerUpdate updatePlayer, OtherPlayerHandUpdate updateOther) {
        final String stompEndpoint = "/queue/playerUpdated";
        this.LOG.info("Dispatch: updatePlayer from playerID '{}' received and sent to all players.", player.getPlayerID());
        for(Player p: playerIdToPlayer.values()) {
            if(player==p) {
                this.LOG.info("Updated player '{}' sent to endpoint '{}' with '{}' cards",p.getPlayerID(),stompEndpoint,updatePlayer.cards().size());
                message.convertAndSendToUser(p.getPlayerID(),stompEndpoint,updatePlayer);
            }
            else {
                this.LOG.info("Updated player '{}' sent to endpoint '{}' '{}' cards",p.getPlayerID(),stompEndpoint,updateOther.numCards());
                message.convertAndSendToUser(p.getPlayerID(),stompEndpoint,updateOther);
            }
        }
    }
}
