import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import { useGameBoardStore,usePlayerStore,useScoreBoardStore,useMessageStore, useGameStateStore } from "./Stores"
const SOCK_SVR = "http://localhost:8080/ws"

const client = new Client(
    {
        webSocketFactory: () => {
            return new SockJS(`${SOCK_SVR}`);
        },
        reconnectDelay: 50000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: onWsConnected,
        debug : (out) => { console.log(out)},
        onUnhandledMessage: (out) => console.log("Unhandled: "+out)
    }
);
export const connect = async (name) => {
    client.activate();
}

function onWsConnected(frame) {

    console.log(`Connected to Websocket @ ${SOCK_SVR}`)
    console.log("Subscribing to topics.");
    useGameStateStore.getState().setIsConnected(true);
    
    client.subscribe("/topic/startGame",handleStartGame);
    client.subscribe("/user/queue/playerUpdated",handlePlayerUpdated);
    client.subscribe("/user/queue/messageReceived",handleMessageReceived);
    client.subscribe("/topic/messageReceived",handleMessageReceived);
    client.subscribe("/topic/updateScoreBoard",handleUpdateScoreBoard);
    client.subscribe("/topic/updateGameBoard",handleUpdateGameBoard);
    client.subscribe("/user/queue/OtherPlayerUpdated",handleOtherPlayerUpdated);   
}


/**
 * TOPIC HANDLERS
 */

function handlePlayerUpdated(response) {
    console.log(response.body)
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("playerID") && data.hasOwnProperty("cards")) ) {
        console.error("handleUpdatePlayer recieved malformed data.")
        return;
    }

    if(usePlayerStore.getState().playerID===null) {
        usePlayerStore.getState().setPlayerID(data.playerID);
    }
    else if(data.playerID!==usePlayerStore.getState().playerID) {
        console.error("Update recieved for invalid player. Player "+usePlayerStore.getState().playerID+" cannot be updated with playerID "+response.playerID);
        return;
    }
    usePlayerStore.getState().update(data.cards);
    console.log("PlayerID: "+usePlayerStore.getState().playerID+" hand updated!"); 
}

function handleStartGame(response) {
    console.log("Starting game!");
    useGameStateStore.setIsGameStarted(true);
}

function handleUpdateScoreBoard(response) {
    console.log("Score Board Update Recieved!");
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("playerID") && data.hasOwnProperty("playerName") && data.hasOwnProperty("score")) || isNaN(data.score) ) {
        console.error("handleMessageReceived recieved malformed data.")
        return;
    }
    let playerID = data.playerID;
    let scoreData = data;
    useScoreBoardStore.getState().update(playerID,scoreData);
}

function handleUpdateGameBoard(response) {
    console.log("Game Board Update Recieved!");
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("numCardsDrawPile") && data.hasOwnProperty("discardPile")) || isNaN(data.numCardsDrawPile)) {
        console.error("handleMessageReceived recieved malformed data.")
        return;
    }
    useGameBoardStore.getState().updateNumDrawPile(data.numCardsDrawPile)
    useGameBoardStore.getState().updateDiscardPile(data.discardPile);
}

function handleOtherPlayerUpdated(response) {
    console.log("Other Player Update Recieved!");
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("playerID") && data.hasOwnProperty("numCards")) || isNaN(data.numCards)) {
        console.error("handleMessageReceived recieved malformed data.")
        return;
    }
    useGameBoardStore.getState().updateOthPlayerHand(data.playerID,data.numCards);
}

function handleMessageReceived(response) {
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("senderName") && data.hasOwnProperty("message")) ) {
        console.error("handleMessageReceived recieved malformed data.")
        return;
    }
    let line = `${data.senderName}: ${data.message}`;
    useMessageStore.getState().add(line);
}

/**
 * ACTION HANDLERS (MUST EXPORT)
 */

export function actionRegisterPlayer(name) {
    client.publish({
        destination: "/app/joinGame",
        body : name
    });
}



