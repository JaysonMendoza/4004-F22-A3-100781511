package ca.jkmconsulting.crazyEightsCountdown;

import com.sun.security.auth.UserPrincipal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.Map;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
public class PlayerHandshakeHandler extends DefaultHandshakeHandler {
    private final Logger LOG = LoggerFactory.getLogger(PlayerHandshakeHandler.class);

    @Autowired
    private PlayerManager pManager;


    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String playerName = UriComponentsBuilder.newInstance().query(request.getURI().getQuery()).build().getQueryParams().getFirst("name");
        Player player = pManager.registerPlayer(playerName);
        LOG.info("User named '{}' with ID '{}' connected", player.getName(), player.getPlayerID());
        return new UserPrincipal(player.getPlayerID().toString());
    }
}
